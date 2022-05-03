package utils.collision;

import utils.operator.Operator;
import utils.vector.Vec3f;

public record AxisAlignedBoundingBox(Vec3f min, Vec3f max) {

	public AxisAlignedBoundingBox copy() {
		return new AxisAlignedBoundingBox(min.copy(), max.copy());
	}

	public Vec3f size() {
		return max.copy().apply(Operator.Sub, min);
	}

	public float averageSize() {
		return (float) Math.sqrt(max.distanceSquared(min));
	}

	public boolean isIntersecting(AxisAlignedBoundingBox other) {
		for (int i = 0; i < Vec3f.DATA_LEN; i++) {
			if (min.data[i] > other.max.data[i] || max.data[i] < other.min.data[i]) {
				return false;
			}
		}
		return true;
	}

	public boolean isIn(Vec3f point) {
		for (int i = 0; i < Vec3f.DATA_LEN; i++) {
			if (min.data[i] > point.data[i] || max.data[i] < point.data[i]) {
				return false;
			}
		}
		return true;
	}

	public boolean containsFully(AxisAlignedBoundingBox other) {
		for (int i = 0; i < Vec3f.DATA_LEN; i++) {
			if (min.data[i] > other.min.data[i] || max.data[i] <= other.max.data[i]) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		return "AxisAlignedBoundingBox{"
				+ "min: " + min
				+ ", max: " + max
				+ '}';
	}
}
