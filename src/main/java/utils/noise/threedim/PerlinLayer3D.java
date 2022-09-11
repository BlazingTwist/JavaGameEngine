package utils.noise.threedim;

import utils.noise.Perlin;
import utils.noise.twodim.PerlinLayer2D;

public class PerlinLayer3D
		extends PerlinLayer2D
		implements INoiseLayer3D {

	public float zOffset;

	protected float zStep;

	public PerlinLayer3D(float scale, float xOffset, float yOffset, float zOffset, float minValue, float maxValue, float valueOffset) {
		super(scale, xOffset, yOffset, minValue, maxValue, valueOffset);
		this.zOffset = zOffset;
	}

	public void toggle() {
		enabled = !enabled;
	}

	@Override
	public String toString() {
		return "PerlinLayer3D{"
				+ "scale: " + scale
				+ ", xOffset: " + xOffset
				+ ", yOffset: " + yOffset
				+ ", zOffset: " + zOffset
				+ ", minValue: " + minValue
				+ ", maxValue: " + maxValue
				+ ", valueOffset: " + valueOffset
				+ '}';
	}

	@Override
	public void prepareCompute(IGridDimensions3D gridDimensions) {
		super.prepareCompute(gridDimensions);
		zStep = scale / gridDimensions.zDimension();
	}

	@Override
	public float computeValue(float x, float y, float z) {
		if (!enabled) {
			return 0f;
		}
		float perlin01 = Perlin.perlin01(
				xOffset + (xStep * x),
				yOffset + (yStep * y),
				zOffset + (zStep * z)
		);
		return (perlin01 * outputDelta) + minValue + valueOffset;
	}
}
