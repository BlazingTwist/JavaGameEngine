package utils.noise.twodim;

public interface INoiseLayer2D {
	void prepareCompute(IVoxelGrid2D grid);

	float computeValue(int x, int y);
}
