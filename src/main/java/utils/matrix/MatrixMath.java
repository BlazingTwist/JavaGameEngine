package utils.matrix;

import utils.vector.Vec3f;

public class MatrixMath {
	private static void _lookAtAssign(Mat4f target, Vec3f eyePosition, Vec3f f, Vec3f s, Vec3f u) {
		target.data[0] = -s.data[0];
		target.data[1] = -s.data[1];
		target.data[2] = -s.data[2];
		target.data[3] = s.dot(eyePosition);

		target.data[4] = u.data[0];
		target.data[5] = u.data[1];
		target.data[6] = u.data[2];
		target.data[7] = -u.dot(eyePosition);

		target.data[8] = -f.data[0];
		target.data[9] = -f.data[1];
		target.data[10] = -f.data[2];
		target.data[11] = f.dot(eyePosition);
	}

	public static Mat4f lookAt(Vec3f eyePosition, Vec3f forwardDirection, Vec3f upDirection) {
		Vec3f s = forwardDirection.copy().cross(upDirection).normalize();
		Mat4f result = new Mat4f(1f);
		_lookAtAssign(result, eyePosition, forwardDirection, s, upDirection);
		return result;
	}

	public static void lookAt(Mat4f target, Vec3f eyePosition, Vec3f forwardDirection, Vec3f upDirection) {
		Vec3f s = forwardDirection.copy().cross(upDirection).normalize();
		_lookAtAssign(target, eyePosition, forwardDirection, s, upDirection);

		target.data[12] = 0f;
		target.data[13] = 0f;
		target.data[14] = 0f;
		target.data[15] = 1f;
	}

	/**
	 * @param target matrix to store result in
	 * @param origin matrix to rotate
	 * @param angle  angle in radians
	 * @param axis   axis of rotation
	 */
	public static void rotate(Mat4f target, Mat4f origin, float angle, Vec3f axis) {
		float cos = (float) Math.cos(angle);
		float sin = (float) Math.sin(angle);

		float temp0 = (1f - cos) * axis.data[0];
		float temp1 = (1f - cos) * axis.data[1];
		float temp2 = (1f - cos) * axis.data[2];

		float rot00 = cos + temp0 * axis.data[0];
		float rot01 = temp0 * axis.data[1] + sin * axis.data[2];
		float rot02 = temp0 * axis.data[2] - sin * axis.data[1];

		float rot10 = temp1 * axis.data[0] - sin * axis.data[2];
		float rot11 = cos + temp1 * axis.data[1];
		float rot12 = temp1 * axis.data[2] + sin * axis.data[0];

		float rot20 = temp2 * axis.data[0] + sin * axis.data[1];
		float rot21 = temp2 * axis.data[1] - sin * axis.data[0];
		float rot22 = cos + temp2 * axis.data[2];

		for (int i = 0; i < 4; i++) {
			float a = origin.data[i] * rot00 + origin.data[i + 4] * rot01 + origin.data[i + 8] * rot02;
			float b = origin.data[i] * rot10 + origin.data[i + 4] * rot11 + origin.data[i + 8] * rot12;
			float c = origin.data[i] * rot20 + origin.data[i + 4] * rot21 + origin.data[i + 8] * rot22;

			target.data[i] = a;
			target.data[i + 4] = b;
			target.data[i + 8] = c;
			target.data[i + 12] = origin.data[i + 12];
		}
	}

	/**
	 * Because sometimes, you just don't care for those extra 7 cells
	 *
	 * @param target matrix to store result in
	 * @param origin matrix to rotate
	 * @param angle  angle in radians
	 * @param axis   axis of rotation
	 */
	public static void rotate(Mat3f target, Mat3f origin, float angle, Vec3f axis) {
		float cos = (float) Math.cos(angle);
		float sin = (float) Math.sin(angle);

		float temp0 = (1f - cos) * axis.data[0];
		float temp1 = (1f - cos) * axis.data[1];
		float temp2 = (1f - cos) * axis.data[2];

		float rot00 = cos + temp0 * axis.data[0];
		float rot01 = temp0 * axis.data[1] + sin * axis.data[2];
		float rot02 = temp0 * axis.data[2] - sin * axis.data[1];

		float rot10 = temp1 * axis.data[0] - sin * axis.data[2];
		float rot11 = cos + temp1 * axis.data[1];
		float rot12 = temp1 * axis.data[2] + sin * axis.data[0];

		float rot20 = temp2 * axis.data[0] + sin * axis.data[1];
		float rot21 = temp2 * axis.data[1] - sin * axis.data[0];
		float rot22 = cos + temp2 * axis.data[2];

		for (int i = 0; i < 3; i++) {
			float a = origin.data[i] * rot00 + origin.data[i + 3] * rot01 + origin.data[i + 6] * rot02;
			float b = origin.data[i] * rot10 + origin.data[i + 3] * rot11 + origin.data[i + 6] * rot12;
			float c = origin.data[i] * rot20 + origin.data[i + 3] * rot21 + origin.data[i + 6] * rot22;

			target.data[i] = a;
			target.data[i + 3] = b;
			target.data[i + 6] = c;
		}
	}

	/**
	 * Assumes an identity mat3 as origin.
	 * Because sometimes, you just don't care for those extra 7 cells.
	 *
	 * @param angle angle in radians
	 * @param axis  axis of rotation
	 * @return new Mat3f with applied rotation
	 */
	public static Mat3f rotate(float angle, Vec3f axis) {
		float cos = (float) Math.cos(angle);
		float sin = (float) Math.sin(angle);

		float temp0 = (1f - cos) * axis.data[0];
		float temp1 = (1f - cos) * axis.data[1];
		float temp2 = (1f - cos) * axis.data[2];

		return new Mat3f(
				cos + temp0 * axis.data[0],
				temp0 * axis.data[1] + sin * axis.data[2],
				temp0 * axis.data[2] - sin * axis.data[1],

				temp1 * axis.data[0] - sin * axis.data[2],
				cos + temp1 * axis.data[1],
				temp1 * axis.data[2] + sin * axis.data[0],

				temp2 * axis.data[0] + sin * axis.data[1],
				temp2 * axis.data[1] - sin * axis.data[0],
				cos + temp2 * axis.data[2]
		);
	}

	public static Mat4f perspective(float fov, float aspect, float zNear, float zFar) {
		float tanHalfFov = (float) Math.tan(Math.toRadians(fov) / 2f);
		return new Mat4f(
				1f / (aspect * tanHalfFov), 0f, 0f, 0f,
				0f, 1f / tanHalfFov, 0f, 0f,
				0f, 0f, -(zFar + zNear) / (zFar - zNear), -(2f * zFar * zNear) / (zFar - zNear),
				0f, 0f, -1f, 0f
		);
	}

	public static void perspective(Mat4f target, float fov, float aspect, float zNear, float zFar) {
		float tanHalfFov = (float) Math.tan(Math.toRadians(fov) / 2f);
		target.set(
				1f / (aspect * tanHalfFov), 0f, 0f, 0f,
				0f, 1f / tanHalfFov, 0f, 0f,
				0f, 0f, -(zFar + zNear) / (zFar - zNear), -(2f * zFar * zNear) / (zFar - zNear),
				0f, 0f, -1f, 0f
		);
	}

	public static Mat4f orthographic(float left, float right, float bottom, float top, float zNear, float zFar) {
		return new Mat4f(
				2f / (right - left), 0f, 0f, -(right + left) / (right - left),
				0f, 2f / (top - bottom), 0f, -(top + bottom) / (top - bottom),
				0f, 0f, 1f / (zFar - zNear), -zNear / (zFar - zNear),
				0f, 0f, 0f, 1f
		);
	}

	public static void orthographic(Mat4f target,
									float left, float right, float bottom, float top, float zNear, float zFar) {
		target.set(
				2f / (right - left), 0f, 0f, -(right + left) / (right - left),
				0f, 2f / (top - bottom), 0f, -(top + bottom) / (top - bottom),
				0f, 0f, 1f / (zFar - zNear), -zNear / (zFar - zNear),
				0f, 0f, 0f, 1f
		);
	}

	/**
	 * Assumes an identity mat4 as origin.
	 */
	public static Mat4f translate(Vec3f offset) {
		Mat4f result = new Mat4f();
		result.data[3] = offset.data[0];
		result.data[7] = offset.data[1];
		result.data[11] = offset.data[2];
		return result;
	}

	/**
	 * Assumes an identity mat4 as origin.
	 */
	public static Mat4f scale(Vec3f scale) {
		Mat4f result = new Mat4f();
		result.data[0] = scale.data[0];
		result.data[5] = scale.data[1];
		result.data[10] = scale.data[2];
		return result;
	}
}
