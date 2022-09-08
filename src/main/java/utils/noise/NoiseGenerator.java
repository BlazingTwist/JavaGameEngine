package utils.noise;

import java.util.ArrayList;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import utils.noise.threedim.INoiseLayer3D;
import utils.noise.threedim.IVoxelGrid3D;
import utils.noise.twodim.INoiseLayer2D;
import utils.noise.twodim.IVoxelGrid2D;

public class NoiseGenerator {

	public static void generate2D(IVoxelGrid2D targetGrid, INoiseLayer2D layer) {
		layer.prepareCompute(targetGrid);

		float[][] voxelGridXY = targetGrid.voxels2D();
		IntStream.range(0, targetGrid.xDimension()).parallel().forEach(x -> {
			float[] voxelGridY = voxelGridXY[x];
			for (int y = 0; y < targetGrid.yDimension(); y++) {
				voxelGridY[y] = layer.computeValue(x, y);
			}
		});
	}

	public static void generate3D(IVoxelGrid3D targetGrid, INoiseLayer3D layer) {
		layer.prepareCompute(targetGrid);

		float[][][] voxelGridXYZ = targetGrid.voxels3D();
		IntStream.range(0, targetGrid.xDimension()).parallel().forEach(x -> {
			float[][] voxelGridYZ = voxelGridXYZ[x];
			for (int y = 0; y < targetGrid.yDimension(); y++) {
				float[] voxelGridZ = voxelGridYZ[y];
				for (int z = 0; z < targetGrid.zDimension(); z++) {
					voxelGridZ[z] = layer.computeValue(x, y, z);
				}
			}
		});
	}

	public static void printLayoutParameters2D(Logger logger, ArrayList<INoiseLayer2D> layers) {
		logger.info("{} layouts:", layers.size());
		int layerIndex = 0;
		for (INoiseLayer2D layer : layers) {
			logger.info(" {} - {}", layerIndex, layer);
			layerIndex++;
		}
	}

	public static void printLayoutParameters3D(Logger logger, ArrayList<INoiseLayer3D> layers) {
		logger.info("{} layouts:", layers.size());
		int layerIndex = 0;
		for (INoiseLayer3D layer : layers) {
			logger.info(" {} - {}", layerIndex, layer);
			layerIndex++;
		}
	}
}
