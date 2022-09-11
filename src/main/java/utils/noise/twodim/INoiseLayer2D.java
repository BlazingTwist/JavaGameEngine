package utils.noise.twodim;

public interface INoiseLayer2D {
	void prepareCompute(IGridDimensions2D gridDimensions);

	float computeValue(int x, int y);
}
