package utils.quaternion;

import utils.operator.Operator;
import utils.vector.Vec3f;

public class QuaternionMathIP {
	/**
	 * @param quaternion Quaternion to rotate by
	 * @param vector     Vector to be rotated in-place
	 */
	public static Vec3f rotate(Quaternion quaternion, Vec3f vector) {
		Vec3f uv = new Vec3f(quaternion).cross(vector);
		Vec3f uuv = new Vec3f(quaternion).cross(uv);

		Vec3f offset = uv.apply(Operator.Mul, quaternion.data[3]).apply(Operator.Add, uuv).apply(Operator.Mul, 2f);
		vector.apply(Operator.Add, offset);
		return vector;
	}

	public static Quaternion rotate(Quaternion target, Quaternion a, Quaternion b) {
		target.set(
				a.data[3] * b.data[0] + a.data[0] * b.data[3] + a.data[1] * b.data[2] - a.data[2] * b.data[1],
				a.data[3] * b.data[1] + a.data[1] * b.data[3] + a.data[2] * b.data[0] - a.data[0] * b.data[2],
				a.data[3] * b.data[2] + a.data[2] * b.data[3] + a.data[0] * b.data[1] - a.data[1] * b.data[0],
				a.data[3] * b.data[3] - a.data[0] * b.data[0] - a.data[1] * b.data[1] - a.data[2] * b.data[2]
		);
		return target;
	}
}
