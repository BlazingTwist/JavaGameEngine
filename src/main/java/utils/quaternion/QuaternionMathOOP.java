package utils.quaternion;

import utils.matrix.Mat3f;
import utils.matrix.Mat4f;
import utils.operator.Operator;
import utils.vector.Vec3f;

public class QuaternionMathOOP {
	private static void _toMat(float[] target, int rowPadding, Quaternion quaternion) {
		float qxx = quaternion.data[0] * quaternion.data[0];
		float qyy = quaternion.data[1] * quaternion.data[1];
		float qzz = quaternion.data[2] * quaternion.data[2];
		float qxz = quaternion.data[0] * quaternion.data[2];
		float qxy = quaternion.data[0] * quaternion.data[1];
		float qyz = quaternion.data[1] * quaternion.data[2];
		float qwx = quaternion.data[3] * quaternion.data[0];
		float qwy = quaternion.data[3] * quaternion.data[1];
		float qwz = quaternion.data[3] * quaternion.data[2];

		target[0] = 1f - (2f * (qyy + qzz));
		target[1] = 2f * (qxy - qwz);
		target[2] = 2f * (qxz + qwy);
		target[3 + rowPadding] = 2f * (qxy + qwz);
		target[4 + rowPadding] = 1f - (2f * (qxx + qzz));
		target[5 + rowPadding] = 2f * (qyz - qwx);
		target[6 + rowPadding + rowPadding] = 2f * (qxz - qwy);
		target[7 + rowPadding + rowPadding] = 2f * (qyz + qwx);
		target[8 + rowPadding + rowPadding] = 1f - (2f * (qxx + qyy));
	}

	/**
	 * Convert quaternion to rotation matrix
	 */
	public static Mat3f toMat3(Quaternion quaternion) {
		Mat3f result = new Mat3f();
		_toMat(result.data, 0, quaternion);
		return result;
	}

	/**
	 * Convert quaternion to rotation matrix
	 */
	public static Mat4f toMat4(Quaternion quaternion) {
		Mat4f result = new Mat4f();
		_toMat(result.data, 1, quaternion);
		return result;
	}

	private static Quaternion _toQuaternion(float[] m, int rowSize) {
		//noinspection UnnecessaryLocalVariable - reduce expression clutter
		final int r = rowSize;

		float a0 = m[0];
		float a1 = m[1];
		float a2 = m[2];
		float b0 = m[r];
		float b1 = m[r + 1];
		float b2 = m[r + 2];
		float c0 = m[r + r];
		float c1 = m[r + r + 1];
		float c2 = m[r + r + 2];

		float fourXSquaredMinus1 = a0 - b1 - c2;
		float fourYSquaredMinus1 = b1 - a0 - c2;
		float fourZSquaredMinus1 = c2 - a0 - b1;
		float fourWSquaredMinus1 = a0 + b1 + c2;

		int biggestIndex = 0;
		float fourBiggestSquaredMinus1 = fourWSquaredMinus1;
		if (fourXSquaredMinus1 > fourBiggestSquaredMinus1) {
			fourBiggestSquaredMinus1 = fourXSquaredMinus1;
			biggestIndex = 1;
		}
		if (fourYSquaredMinus1 > fourBiggestSquaredMinus1) {
			fourBiggestSquaredMinus1 = fourYSquaredMinus1;
			biggestIndex = 2;
		}
		if (fourZSquaredMinus1 > fourBiggestSquaredMinus1) {
			fourBiggestSquaredMinus1 = fourZSquaredMinus1;
			biggestIndex = 3;
		}

		float biggestVal = (float) Math.sqrt(fourBiggestSquaredMinus1 + 1) * 0.5f;
		float multi = 0.25f / biggestVal;

		return switch (biggestIndex) {
			case 0 -> new Quaternion((c1 - b2) * multi, (a2 - c0) * multi, (b0 - a1) * multi, biggestVal);
			case 1 -> new Quaternion(biggestVal, (b0 + a1) * multi, (a2 + c0) * multi, (c1 - b2) * multi);
			case 2 -> new Quaternion((b0 + a1) * multi, biggestVal, (c1 + b2) * multi, (a2 - c0) * multi);
			case 3 -> new Quaternion((a2 + c0) * multi, (c1 + b2) * multi, biggestVal, (b0 - a1) * multi);
			default -> throw new IllegalStateException("Unexpected value: " + biggestIndex);
		};
	}

	public static Quaternion toQuaternion(Mat4f mat4f) {
		return _toQuaternion(mat4f.data, 4);
	}

	public static Quaternion toQuaternion(Mat3f mat3f) {
		return _toQuaternion(mat3f.data, 3);
	}

	public static Quaternion lookAtToQuaternion(Mat4f lookAt) {
		Vec3f camForward = new Vec3f(-lookAt.data[8], -lookAt.data[9], -lookAt.data[10]);
		Vec3f forward = Vec3f.forward();
		float dot = forward.dot(camForward);
		Vec3f axis = forward.copy().cross(camForward);
		float qw = 1 + dot;
		if (qw < 0.0001) { // vectors are 180 degrees apart
			return new Quaternion(-camForward.data[2], camForward.data[1], camForward.data[0], 0);
		}
		return new Quaternion(axis, qw).normalize();
	}

	/**
	 * @param quaternion Quaternion to rotate by
	 * @param vector     Vector to be rotated out-of-place
	 * @return rotation result
	 */
	public static Vec3f rotate(Quaternion quaternion, Vec3f vector) {
		Vec3f uv = new Vec3f(quaternion).cross(vector);
		Vec3f uuv = new Vec3f(quaternion).cross(uv);

		Vec3f offset = uv.apply(Operator.Mul, quaternion.data[3]).apply(Operator.Add, uuv).apply(Operator.Mul, 2f);
		return vector.copy().apply(Operator.Add, offset);
	}

	/**
	 * @param quaternion Quaternion to rotate by
	 * @param vector     Vector to be rotated out-of-place
	 */
	public static void rotate(Vec3f target, Quaternion quaternion, Vec3f vector) {
		Vec3f uv = new Vec3f(quaternion).cross(vector);
		Vec3f uuv = new Vec3f(quaternion).cross(uv);

		Vec3f offset = uv.apply(Operator.Mul, quaternion.data[3]).apply(Operator.Add, uuv).apply(Operator.Mul, 2f);
		Operator.Add.apply(target.data, 0, vector.data, 0, offset.data, 0, 3);
	}

	public static Quaternion rotate(Quaternion a, Quaternion b) {
		return new Quaternion(
				a.data[3] * b.data[0] + a.data[0] * b.data[3] + a.data[1] * b.data[2] - a.data[2] * b.data[1],
				a.data[3] * b.data[1] + a.data[1] * b.data[3] + a.data[2] * b.data[0] - a.data[0] * b.data[2],
				a.data[3] * b.data[2] + a.data[2] * b.data[3] + a.data[0] * b.data[1] - a.data[1] * b.data[0],
				a.data[3] * b.data[3] - a.data[0] * b.data[0] - a.data[1] * b.data[1] - a.data[2] * b.data[2]
		);
	}

	private static Quaternion _euler(Vec3f cos, Vec3f sin) {
		return new Quaternion(
				(sin.data[0] * cos.data[1] * cos.data[2]) - (cos.data[0] * sin.data[1] * sin.data[2]),
				(cos.data[0] * sin.data[1] * cos.data[2]) + (sin.data[0] * cos.data[1] * sin.data[2]),
				(cos.data[0] * cos.data[1] * sin.data[2]) - (sin.data[0] * sin.data[1] * cos.data[2]),
				(cos.data[0] * cos.data[1] * cos.data[2]) + (sin.data[0] * sin.data[1] * sin.data[2])
		);
	}

	/**
	 * @param eulerAngles euler angles in degrees
	 */
	public static Quaternion eulerDeg(Vec3f eulerAngles) {
		Vec3f cos = eulerAngles.copy().apply(x -> (float) Math.cos(Math.toRadians(x) * 0.5f));
		Vec3f sin = eulerAngles.copy().apply(x -> (float) Math.sin(Math.toRadians(x) * 0.5f));
		return _euler(cos, sin);
	}

	public static Quaternion eulerRad(Vec3f eulerAngles) {
		Vec3f cos = eulerAngles.copy().apply(x -> (float) Math.cos(x * 0.5f));
		Vec3f sin = eulerAngles.copy().apply(x -> (float) Math.sin(x * 0.5f));
		return _euler(cos, sin);
	}
}
