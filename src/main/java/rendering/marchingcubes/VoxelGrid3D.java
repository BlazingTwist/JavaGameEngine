package rendering.marchingcubes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.lwjgl.opengl.GL45;

public class VoxelGrid3D {

	public final int xDimension;
	public final int yDimension;
	public final int zDimension;
	public final float[][][] voxels;

	private final int glslVoxelSSBO;
	private final FloatBuffer voxelBuffer;

	// vao/vbo for dispatching draw calls on each grid cell
	private final int vertexArrayObjectID;
	private final int vertexBufferObjectID;

	private boolean voxelDataDirty = true;
	private boolean isDeleted = false;

	public VoxelGrid3D(int xDimension, int yDimension, int zDimension) {
		this.xDimension = xDimension;
		this.yDimension = yDimension;
		this.zDimension = zDimension;
		this.voxels = new float[xDimension][yDimension][zDimension];

		glslVoxelSSBO = GL45.glGenBuffers();
		GL45.glBindBuffer(GL45.GL_SHADER_STORAGE_BUFFER, glslVoxelSSBO);
		// why does this align to an offset of 12, unlike Light data which aligns to 16?
		//  might be because of OpenGL alignment rules regarding struct[] vs vec3[]
		GL45.glBufferData(GL45.GL_SHADER_STORAGE_BUFFER, ((long) xDimension * yDimension * zDimension * Float.BYTES) + 12, GL45.GL_DYNAMIC_DRAW);
		voxelBuffer = ByteBuffer.allocateDirect(xDimension * yDimension * zDimension * Float.BYTES)
				.order(ByteOrder.LITTLE_ENDIAN)
				.asFloatBuffer();
		GL45.glBufferSubData(GL45.GL_SHADER_STORAGE_BUFFER, 0, new int[]{xDimension, yDimension, zDimension, 0});

		vertexArrayObjectID = GL45.glGenVertexArrays();
		GL45.glBindVertexArray(vertexArrayObjectID);

		vertexBufferObjectID = GL45.glGenBuffers();
		GL45.glBindBuffer(GL45.GL_ARRAY_BUFFER, vertexBufferObjectID);

		GL45.glEnableVertexAttribArray(0);
		GL45.glVertexAttribIPointer(0, 3, GL45.GL_UNSIGNED_INT, 3 * Integer.BYTES, 0);

		IntBuffer intBuffer = ByteBuffer.allocateDirect((xDimension - 1) * (yDimension - 1) * (zDimension - 1) * Integer.BYTES * 3)
				.order(ByteOrder.LITTLE_ENDIAN)
				.asIntBuffer();
		for (int x = 0; x < (xDimension - 1); x++) {
			for (int y = 0; y < (yDimension - 1); y++) {
				for (int z = 0; z < (zDimension - 1); z++) {
					intBuffer.put(x);
					intBuffer.put(y);
					intBuffer.put(z);
				}
			}
		}
		intBuffer.rewind();
		GL45.glBufferData(GL45.GL_ARRAY_BUFFER, intBuffer, GL45.GL_STATIC_DRAW);
	}

	public void makeManifold() {
		for (int x = 0; x < xDimension; x++) {
			for (int y = 0; y < yDimension; y++) {
				voxels[x][y][0] = 1f;
				voxels[x][y][zDimension - 1] = 1f;
			}
			for (int z = 0; z < zDimension; z++) {
				voxels[x][0][z] = 1f;
				voxels[x][yDimension - 1][z] = 1f;
			}
		}
		for (int y = 0; y < yDimension; y++) {
			for (int z = 0; z < zDimension; z++) {
				voxels[0][y][z] = 1f;
				voxels[xDimension - 1][y][z] = 1f;
			}
		}
		setDirty();
	}

	public void setDirty() {
		this.voxelDataDirty = true;
	}

	public void forceBindVoxelData() {
		for (int x = 0; x < xDimension; x++) {
			float[][] voxelYZ = voxels[x];
			for (int y = 0; y < yDimension; y++) {
				voxelBuffer.put(voxelYZ[y]);
			}
		}
		voxelBuffer.rewind();
		GL45.glBufferSubData(GL45.GL_SHADER_STORAGE_BUFFER, 12, voxelBuffer);
		voxelDataDirty = false;
	}

	public void bindShaderData(int bindIndex) {
		GL45.glBindBuffer(GL45.GL_SHADER_STORAGE_BUFFER, glslVoxelSSBO);
		if (voxelDataDirty) {
			forceBindVoxelData();
		}
		GL45.glBindBufferBase(GL45.GL_SHADER_STORAGE_BUFFER, bindIndex, glslVoxelSSBO);
	}

	public void draw() {
		GL45.glBindVertexArray(vertexArrayObjectID);
		GL45.glDrawArrays(GL45.GL_POINTS, 0, (xDimension - 1) * (yDimension - 1) * (zDimension - 1));
	}

	public void delete() {
		if (isDeleted) {
			return;
		}
		isDeleted = true;
		GL45.glDeleteBuffers(vertexBufferObjectID);
		GL45.glDeleteVertexArrays(vertexArrayObjectID);
	}
}
