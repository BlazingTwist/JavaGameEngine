package utils.noise.twodim;

import utils.noise.Perlin;

public class PerlinLayer2D implements INoiseLayer2D {
	public float scale;
	public float xOffset;
	public float yOffset;
	public float minValue;
	public float maxValue;
	public float valueOffset;
	public boolean enabled = true;

	protected float xStep;
	protected float yStep;
	protected float outputDelta;

	public PerlinLayer2D(float scale, float xOffset, float yOffset, float minValue, float maxValue, float valueOffset) {
		this.scale = scale;
		this.xOffset = xOffset;
		this.yOffset = yOffset;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.valueOffset = valueOffset;
	}

	public void toggle() {
		enabled = !enabled;
	}

	@Override
	public String toString() {
		return "PerlinLayer2D{"
				+ "scale: " + scale
				+ ", xOffset: " + xOffset
				+ ", yOffset: " + yOffset
				+ ", minValue: " + minValue
				+ ", maxValue: " + maxValue
				+ ", valueOffset: " + valueOffset
				+ '}';
	}

	@Override
	public void prepareCompute(IGridDimensions2D gridDimensions) {
		xStep = scale / gridDimensions.xDimension();
		yStep = scale / gridDimensions.yDimension();
		outputDelta = maxValue - minValue;
	}

	@Override
	public float computeValue(int x, int y) {
		if (!enabled) {
			return 0f;
		}
		float perlin01 = Perlin.perlin01(xOffset + (xStep * x), yOffset + (yStep * y));
		return (perlin01 * outputDelta) + minValue + valueOffset;
	}
}
