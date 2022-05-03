package rendering.mesh;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javacutils.Pair;
import org.lwjgl.opengl.GL45;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rendering.GeometryBuffer;
import utils.operator.Operator;
import utils.vector.Vec2f;
import utils.vector.Vec3f;

public class MeshData {
	private static final Logger logger = LoggerFactory.getLogger(MeshData.class);
	private static final Pattern objStringPattern = Pattern.compile("^f\\s+([0-9\\-/]+)\\s+([0-9\\-/]+)\\s+([0-9\\-/]+)(?:\\s+([0-9\\-/]+))?$");

	private static class FaceData {
		private static class VertexData {
			public int positionIdx;
			public int texCoordIdx = 0;
			public int normalIdx = 0;

			public static VertexData fromGlVertexString(String glVertexString) {
				String[] split = glVertexString.split("/", -1);
				if (split.length <= 0) {
					throw new IllegalArgumentException("Input vertex string '" + glVertexString + "' contains no vertex data");
				}
				if (split.length > 3) {
					logger.warn("vertex data string '{}' contains more than 3 values!", glVertexString);
				}
				if (split.length == 3) {
					return new VertexData(
							Integer.parseInt(split[0]),
							split[1].length() > 0 ? Integer.parseInt(split[1]) : 0,
							Integer.parseInt(split[2])
					);
				} else if (split.length == 2) {
					return new VertexData(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
				} else {
					return new VertexData(Integer.parseInt(split[0]));
				}
			}

			public VertexData(int positionIdx) {
				this.positionIdx = positionIdx;
			}

			public VertexData(int positionIdx, int texCoordIdx) {
				this.positionIdx = positionIdx;
				this.texCoordIdx = texCoordIdx;
			}

			public VertexData(int positionIdx, int texCoordIdx, int normalIdx) {
				this.positionIdx = positionIdx;
				this.texCoordIdx = texCoordIdx;
				this.normalIdx = normalIdx;
			}

			@Override
			public boolean equals(Object o) {
				if (this == o) return true;
				if (o == null || getClass() != o.getClass()) return false;
				VertexData that = (VertexData) o;
				return positionIdx == that.positionIdx && texCoordIdx == that.texCoordIdx && normalIdx == that.normalIdx;
			}

			@Override
			public int hashCode() {
				return Objects.hash(positionIdx, texCoordIdx, normalIdx);
			}
		}

		public final VertexData[] vertices = new VertexData[]{null, null, null};

		public FaceData(VertexData a, VertexData b, VertexData c) {
			vertices[0] = a;
			vertices[1] = b;
			vertices[2] = c;
		}
	}

	public static MeshData loadFromFile(String resourcePath, boolean invertNormals) {
		try {
			return new MeshData(resourcePath, invertNormals);
		} catch (URISyntaxException | IOException e) {
			logger.error("Failed to load mesh for resource: {}", resourcePath, e);
			return null;
		}
	}

	private static <T> T resolveOpenGLIndex(List<T> list, int glIndex, java.util.function.Supplier<T> fallbackConstructor) {
		if (glIndex < 0) {
			return list.get(list.size() - glIndex);
		} else if (glIndex == 0) {
			return fallbackConstructor.get();
		} else {
			return list.get(glIndex - 1);
		}
	}

	public final ArrayList<Vec3f> positions = new ArrayList<>();
	public final ArrayList<Vec2f> texCoords = new ArrayList<>();
	public final ArrayList<Vec3f> normals = new ArrayList<>();
	public final ArrayList<FaceData> faces = new ArrayList<>();

	private GeometryBuffer geometryBuffer = null;

	public GeometryBuffer getGeometryBuffer() {
		return computeMeshBuffer(false);
	}

	public GeometryBuffer computeMeshBuffer(boolean forceRecompute) {
		if (geometryBuffer != null && forceRecompute) {
			geometryBuffer.delete();
			geometryBuffer = null;
		}
		if (geometryBuffer == null) {
			Pair<ArrayList<FaceData.VertexData>, int[]> uniqueVertices = gatherUniqueVertices();
			float[] vertexData = resolveVertexData(uniqueVertices.first);
			if (geometryBuffer == null) {
				geometryBuffer = new GeometryBuffer(
						GL45.GL_TRIANGLES,
						vertexData.length, new int[]{3, 2, 3},
						true, uniqueVertices.second.length
				);
			}
			geometryBuffer.setIndexData(uniqueVertices.second);
			geometryBuffer.setData(vertexData);
		}
		return geometryBuffer;
	}

	public MeshData() {
	}

	public MeshData(String resourcePath, boolean invertNormals) throws URISyntaxException, IOException {
		URL resource = MeshData.class.getClassLoader().getResource(resourcePath);
		if (resource == null) {
			throw new IllegalArgumentException("could not find resource at " + resourcePath);
		}
		for (String line : Files.readAllLines(Paths.get(resource.toURI()), StandardCharsets.UTF_8)) {
			if (line.startsWith("v ")) {
				String[] split = line.split("\\s+", -1);
				if (split.length != 4) {
					throw new IllegalArgumentException("Vertex-Position line '" + line + "' must contain 3 components");
				}
				positions.add(new Vec3f(Float.parseFloat(split[1]), Float.parseFloat(split[2]), Float.parseFloat(split[3])));
			} else if (line.startsWith("vt ")) {
				String[] split = line.split("\\s+", -1);
				if (split.length != 3) {
					throw new IllegalArgumentException("Vertex-TexCoord line '" + line + "' must contain 2 components");
				}
				texCoords.add(new Vec2f(Float.parseFloat(split[1]), Float.parseFloat(split[2])));
			} else if (line.startsWith("vn ")) {
				String[] split = line.split("\\s+", -1);
				if (split.length != 4) {
					throw new IllegalArgumentException("Vertex-Normal line '" + line + "' must contain 3 components");
				}
				normals.add(new Vec3f(Float.parseFloat(split[1]), Float.parseFloat(split[2]), Float.parseFloat(split[3])));
			} else if (line.startsWith("f ")) {
				Matcher matcher = objStringPattern.matcher(line);
				if (!matcher.find() || matcher.groupCount() != 4) {
					throw new IllegalArgumentException("Input face data string '" + line + "' did not match pattern.");
				}
				FaceData.VertexData[] vertices = new FaceData.VertexData[3];
				for (int i = 0; i < 3; i++) {
					vertices[i] = FaceData.VertexData.fromGlVertexString(matcher.group(i + 1));
				}
				faces.add(new FaceData(vertices[0], vertices[1], vertices[2]));
				String group4 = matcher.group(4);
				if (group4 != null) {
					faces.add(new FaceData(vertices[0], vertices[2], FaceData.VertexData.fromGlVertexString(group4)));
				}
			}
		}
		if (invertNormals) {
			for (Vec3f normal : normals) {
				normal.apply(Operator.Mul, -1f);
			}
		}
	}

	public void delete() {
		if (geometryBuffer != null) {
			geometryBuffer.delete();
			geometryBuffer = null;
		}
	}

	/**
	 * @return a list of unique vertices + array of indices that map this MeshData::faces to the unique list.
	 */
	private Pair<ArrayList<FaceData.VertexData>, int[]> gatherUniqueVertices() {
		ArrayList<FaceData.VertexData> uniqueVertices = new ArrayList<>();
		int[] indexData = new int[faces.size() * 3];
		int indexDataIndex = 0;
		for (FaceData face : faces) {
			for (FaceData.VertexData vertex : face.vertices) {
				int uniqueIndex = uniqueVertices.indexOf(vertex);
				if (uniqueIndex < 0) {
					uniqueIndex = uniqueVertices.size();
					uniqueVertices.add(vertex);
				}
				indexData[indexDataIndex] = uniqueIndex;
				indexDataIndex++;
			}
		}
		return Pair.of(uniqueVertices, indexData);
	}

	private float[] resolveVertexData(ArrayList<FaceData.VertexData> vertexIndices) {
		float[] result = new float[(3 + 2 + 3) * vertexIndices.size()];
		int resultIndex = 0;
		for (FaceData.VertexData vertexIndex : vertexIndices) {
			Vec3f positionData = resolveOpenGLIndex(positions, vertexIndex.positionIdx, Vec3f::new);
			System.arraycopy(positionData.data, 0, result, resultIndex, 3);
			resultIndex += 3;

			Vec2f texCoordData = resolveOpenGLIndex(texCoords, vertexIndex.texCoordIdx, Vec2f::new);
			System.arraycopy(texCoordData.data, 0, result, resultIndex, 2);
			resultIndex += 2;

			Vec3f normalData = resolveOpenGLIndex(normals, vertexIndex.normalIdx, Vec3f::new);
			System.arraycopy(normalData.data, 0, result, resultIndex, 3);
			resultIndex += 3;
		}
		return result;
	}
}
