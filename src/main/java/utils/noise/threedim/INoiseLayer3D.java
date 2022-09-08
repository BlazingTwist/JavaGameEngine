package utils.noise.threedim;

public interface INoiseLayer3D {
	void prepareCompute(IVoxelGrid3D grid);

	float computeValue(int x, int y, int z);
}
