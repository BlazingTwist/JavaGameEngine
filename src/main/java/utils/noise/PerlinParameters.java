package utils.noise;

public class PerlinParameters {
	public float scale;
	public float xOffset;
	public float yOffset;
	public float zOffset;
	public float minValue;
	public float maxValue;
	public float valueOffset;

	public PerlinParameters() {
	}

	public PerlinParameters(float scale, float xOffset, float yOffset, float zOffset, float minValue, float maxValue, float valueOffset) {
		this.scale = scale;
		this.xOffset = xOffset;
		this.yOffset = yOffset;
		this.zOffset = zOffset;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.valueOffset = valueOffset;
	}

	@Override
	public String toString() {
		return "PerlinParameters{"
				+ "scale: " + scale
				+ ", xOffset: " + xOffset
				+ ", yOffset: " + yOffset
				+ ", zOffset: " + zOffset
				+ ", minValue: " + minValue
				+ ", maxValue: " + maxValue
				+ ", valueOffset: " + valueOffset
				+ '}';
	}
}
