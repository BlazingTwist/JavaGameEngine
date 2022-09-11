package utils.noise.threedim;

public interface INoiseLayer3D {

	void prepareCompute(IGridDimensions3D gridDimensions);

	float computeValue(float x, float y, float z);

}
