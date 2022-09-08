package utils.noise.threedim;

import utils.MathF;

public class SphereLayer3D implements INoiseLayer3D {
	public float radius;
	public float distanceMultiplier;
	public float minValue;
	public float maxValue;
	public float valueOffset;

	private float sphereMidX;
	private float sphereMidY;
	private float sphereMidZ;

	public SphereLayer3D(float radius, float distanceMultiplier, float minValue, float maxValue, float valueOffset) {
		this.radius = radius;
		this.distanceMultiplier = distanceMultiplier;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.valueOffset = valueOffset;
	}

	@Override
	public String toString() {
		return "SphereLayer3D{"
				+ "radius: " + radius
				+ ", minValue: " + minValue
				+ ", maxValue: " + maxValue
				+ ", valueOffset: " + valueOffset
				+ ", valueMultiplier: " + distanceMultiplier
				+ '}';
	}

	@Override
	public void prepareCompute(IVoxelGrid3D grid) {
		sphereMidX = (grid.xDimension() - 1) * 0.5f;
		sphereMidY = (grid.yDimension() - 1) * 0.5f;
		sphereMidZ = (grid.zDimension() - 1) * 0.5f;
	}

	@Override
	public float computeValue(int x, int y, int z) {
		float xDifference = 1f - (((float) x) / sphereMidX);
		float yDifference = 1f - (((float) y) / sphereMidY);
		float zDifference = 1f - (((float) z) / sphereMidZ);
		float distance = (float) Math.sqrt(xDifference * xDifference + yDifference * yDifference + zDifference * zDifference);
		return MathF.clamp((distance - radius) * distanceMultiplier, minValue, maxValue) + valueOffset;
	}
}
