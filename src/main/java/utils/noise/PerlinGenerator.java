package utils.noise;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import rendering.marchingcubes.VoxelGrid3D;

public class PerlinGenerator {
	private static class LayoutVariables {
		public float startX;
		public float startY;
		public float startZ;
		public float xStep;
		public float yStep;
		public float zStep;
		public float minValue;
		public float outputDelta;
		public float valueOffset;

		public LayoutVariables(VoxelGrid3D grid, PerlinParameters parameters) {
			startX = parameters.xOffset;
			startY = parameters.yOffset;
			startZ = parameters.zOffset;
			xStep = parameters.scale / grid.xDimension;
			yStep = parameters.scale / grid.yDimension;
			zStep = parameters.scale / grid.zDimension;
			minValue = parameters.minValue;
			outputDelta = parameters.maxValue - parameters.minValue;
			valueOffset = parameters.valueOffset;
		}

		public float computePerlin(int x, int y, int z) {
			float perlin01 = Perlin.perlin01(
					startX + (xStep * x),
					startY + (yStep * y),
					startZ + (zStep * z)
			);
			return (perlin01 * outputDelta) + minValue + valueOffset;
		}
	}

	public final ArrayList<PerlinParameters> layers = new ArrayList<>();

	public PerlinGenerator() {
	}

	public PerlinGenerator(PerlinParameters... params){
		layers.addAll(Arrays.asList(params));
	}

	public void generate(VoxelGrid3D targetGrid) {
		ArrayList<LayoutVariables> layoutVariables = new ArrayList<>();
		for (PerlinParameters layer : layers) {
			layoutVariables.add(new LayoutVariables(targetGrid, layer));
		}

		float[][][] voxelGridXYZ = targetGrid.voxels;
		IntStream.range(0, targetGrid.xDimension).parallel().forEach(x -> {
			float[][] voxelGridYZ = voxelGridXYZ[x];
			for (int y = 0; y < targetGrid.yDimension; y++) {
				float[] voxelGridZ = voxelGridYZ[y];
				for (int z = 0; z < targetGrid.zDimension; z++) {
					int finalY = y;
					int finalZ = z;
					voxelGridZ[z] = layoutVariables.stream()
							.map(vars -> vars.computePerlin(x, finalY, finalZ))
							.reduce(0f, Float::sum);
				}
			}
		});
		targetGrid.setDirty();
	}

	public void printLayoutParameters(Logger logger) {
		logger.info("{} layouts:", layers.size());
		int layerIndex = 0;
		for (PerlinParameters layer : layers) {
			logger.info(" {} - {}", layerIndex, layer);
			layerIndex++;
		}
	}
}
