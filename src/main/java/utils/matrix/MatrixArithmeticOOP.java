package utils.matrix;

import utils.vector.Vec2f;
import utils.vector.Vec3f;
import utils.vector.Vec4f;

/**
 * Arithmetic operations on matrices and vectors that operate out-of-place
 */
public class MatrixArithmeticOOP {
	public static float mul2(float[] a, int aStart, Vec2f b) {
		return mul2(a, aStart, b.data, 0);
	}

	public static float mul2(float[] a, int aStart, float[] b, int bStart) {
		return a[aStart] * b[bStart] + a[aStart + 1] * b[bStart + 1];
	}

	public static float mul2(float[] a, int aStart, int aSpacing,
							 float[] b, int bStart, int bSpacing) {
		return a[aStart] * b[bStart] + a[aStart + aSpacing] * b[bStart + bSpacing];
	}

	public static float mul(Vec2f a, Vec2f b) {
		return mul2(a.data, 0, b.data, 0);
	}

	public static Vec2f mul2(float[] a, int aStart, Mat2f b) {
		return new Vec2f(
				mul2(a, aStart, 1, b.data, 0, 2),
				mul2(a, aStart, 1, b.data, 1, 2)
		);
	}

	public static Vec3f mul2(float[] a, int aStart, Mat3x2f b) {
		return new Vec3f(
				mul2(a, aStart, 1, b.data, 0, 3),
				mul2(a, aStart, 1, b.data, 1, 3),
				mul2(a, aStart, 1, b.data, 2, 3)
		);
	}

	public static Vec4f mul2(float[] a, int aStart, Mat4x2f b) {
		return new Vec4f(
				mul2(a, aStart, 1, b.data, 0, 4),
				mul2(a, aStart, 1, b.data, 1, 4),
				mul2(a, aStart, 1, b.data, 2, 4),
				mul2(a, aStart, 1, b.data, 3, 4)
		);
	}

	public static Vec2f mul(Vec2f a, Mat2f b) {
		return mul2(a.data, 0, b);
	}

	public static Vec3f mul(Vec2f a, Mat3x2f b) {
		return mul2(a.data, 0, b);
	}

	public static Vec4f mul(Vec2f a, Mat4x2f b) {
		return mul2(a.data, 0, b);
	}

	public static Vec2f mul(Mat2f a, Vec2f b) {
		return new Vec2f(
				mul2(a.data, 0, b),
				mul2(a.data, 2, b)
		);
	}

	public static Mat2f mul(Mat2f a, Mat2f b) {
		return new Mat2f(
				mul2(a.data, 0, b),
				mul2(a.data, 2, b)
		);
	}

	public static Mat3x2f mul(Mat2f a, Mat3x2f b) {
		return new Mat3x2f(
				mul2(a.data, 0, b),
				mul2(a.data, 2, b)
		);
	}

	public static Mat4x2f mul(Mat2f a, Mat4x2f b) {
		return new Mat4x2f(
				mul2(a.data, 0, b),
				mul2(a.data, 2, b)
		);
	}

	public static Vec3f mul(Mat2x3f a, Vec2f b) {
		return new Vec3f(
				mul2(a.data, 0, b),
				mul2(a.data, 2, b),
				mul2(a.data, 4, b)
		);
	}

	public static Mat2x3f mul(Mat2x3f a, Mat2f b) {
		return new Mat2x3f(
				mul2(a.data, 0, b),
				mul2(a.data, 2, b),
				mul2(a.data, 4, b)
		);
	}

	public static Mat3f mul(Mat2x3f a, Mat3x2f b) {
		return new Mat3f(
				mul2(a.data, 0, b),
				mul2(a.data, 2, b),
				mul2(a.data, 4, b)
		);
	}

	public static Mat4x3f mul(Mat2x3f a, Mat4x2f b) {
		return new Mat4x3f(
				mul2(a.data, 0, b),
				mul2(a.data, 2, b),
				mul2(a.data, 4, b)
		);
	}

	public static Vec4f mul(Mat2x4f a, Vec2f b) {
		return new Vec4f(
				mul2(a.data, 0, b),
				mul2(a.data, 2, b),
				mul2(a.data, 4, b),
				mul2(a.data, 6, b)
		);
	}

	public static Mat2x4f mul(Mat2x4f a, Mat2f b) {
		return new Mat2x4f(
				mul2(a.data, 0, b),
				mul2(a.data, 2, b),
				mul2(a.data, 4, b),
				mul2(a.data, 6, b)
		);
	}

	public static Mat3x4f mul(Mat2x4f a, Mat3x2f b) {
		return new Mat3x4f(
				mul2(a.data, 0, b),
				mul2(a.data, 2, b),
				mul2(a.data, 4, b),
				mul2(a.data, 6, b)
		);
	}

	public static Mat4f mul(Mat2x4f a, Mat4x2f b) {
		return new Mat4f(
				mul2(a.data, 0, b),
				mul2(a.data, 2, b),
				mul2(a.data, 4, b),
				mul2(a.data, 6, b)
		);
	}

	public static float mul3(float[] a, int aStart, Vec3f b) {
		return mul3(a, aStart, b.data, 0);
	}

	public static float mul3(float[] a, int aStart, float[] b, int bStart) {
		return a[aStart] * b[bStart] + a[aStart + 1] * b[bStart + 1] + a[aStart + 2] * b[bStart + 2];
	}

	public static float mul3(float[] a, int aStart, int aSpacing,
							 float[] b, int bStart, int bSpacing) {
		return a[aStart] * b[bStart]
				+ a[aStart + aSpacing] * b[bStart + bSpacing]
				+ a[aStart + aSpacing + aSpacing] * b[bStart + bSpacing + bSpacing];
	}

	public static float mul(Vec3f a, Vec3f b) {
		return mul3(a.data, 0, b.data, 0);
	}

	public static Vec2f mul3(float[] a, int aStart, Mat2x3f b) {
		return new Vec2f(
				mul3(a, aStart, 1, b.data, 0, 2),
				mul3(a, aStart, 1, b.data, 1, 2)
		);
	}

	public static Vec3f mul3(float[] a, int aStart, Mat3f b) {
		return new Vec3f(
				mul3(a, aStart, 1, b.data, 0, 3),
				mul3(a, aStart, 1, b.data, 1, 3),
				mul3(a, aStart, 1, b.data, 2, 3)
		);
	}

	public static Vec4f mul3(float[] a, int aStart, Mat4x3f b) {
		return new Vec4f(
				mul3(a, aStart, 1, b.data, 0, 4),
				mul3(a, aStart, 1, b.data, 1, 4),
				mul3(a, aStart, 1, b.data, 2, 4),
				mul3(a, aStart, 1, b.data, 3, 4)
		);
	}

	public static Vec2f mul(Vec3f a, Mat2x3f b) {
		return mul3(a.data, 0, b);
	}

	public static Vec3f mul(Vec3f a, Mat3f b) {
		return mul3(a.data, 0, b);
	}

	public static Vec4f mul(Vec3f a, Mat4x3f b) {
		return mul3(a.data, 0, b);
	}

	public static Vec2f mul(Mat3x2f a, Vec3f b) {
		return new Vec2f(
				mul3(a.data, 0, b),
				mul3(a.data, 3, b)
		);
	}

	public static Mat2f mul(Mat3x2f a, Mat2x3f b) {
		return new Mat2f(
				mul3(a.data, 0, b),
				mul3(a.data, 3, b)
		);
	}

	public static Mat3x2f mul(Mat3x2f a, Mat3f b) {
		return new Mat3x2f(
				mul3(a.data, 0, b),
				mul3(a.data, 3, b)
		);
	}

	public static Mat4x2f mul(Mat3x2f a, Mat4x3f b) {
		return new Mat4x2f(
				mul3(a.data, 0, b),
				mul3(a.data, 3, b)
		);
	}

	public static Vec3f mul(Mat3f a, Vec3f b) {
		return new Vec3f(
				mul3(a.data, 0, b),
				mul3(a.data, 3, b),
				mul3(a.data, 6, b)
		);
	}

	public static Mat2x3f mul(Mat3f a, Mat2x3f b) {
		return new Mat2x3f(
				mul3(a.data, 0, b),
				mul3(a.data, 3, b),
				mul3(a.data, 6, b)
		);
	}

	public static Mat3f mul(Mat3f a, Mat3f b) {
		return new Mat3f(
				mul3(a.data, 0, b),
				mul3(a.data, 3, b),
				mul3(a.data, 6, b)
		);
	}

	public static Mat4x3f mul(Mat3f a, Mat4x3f b) {
		return new Mat4x3f(
				mul3(a.data, 0, b),
				mul3(a.data, 3, b),
				mul3(a.data, 6, b)
		);
	}

	public static Vec4f mul(Mat3x4f a, Vec3f b) {
		return new Vec4f(
				mul3(a.data, 0, b),
				mul3(a.data, 3, b),
				mul3(a.data, 6, b),
				mul3(a.data, 9, b)
		);
	}

	public static Mat2x4f mul(Mat3x4f a, Mat2x3f b) {
		return new Mat2x4f(
				mul3(a.data, 0, b),
				mul3(a.data, 3, b),
				mul3(a.data, 6, b),
				mul3(a.data, 9, b)
		);
	}

	public static Mat3x4f mul(Mat3x4f a, Mat3f b) {
		return new Mat3x4f(
				mul3(a.data, 0, b),
				mul3(a.data, 3, b),
				mul3(a.data, 6, b),
				mul3(a.data, 9, b)
		);
	}

	public static Mat4f mul(Mat3x4f a, Mat4x3f b) {
		return new Mat4f(
				mul3(a.data, 0, b),
				mul3(a.data, 3, b),
				mul3(a.data, 6, b),
				mul3(a.data, 9, b)
		);
	}

	public static float mul4(float[] a, int aStart, Vec4f b) {
		return mul4(a, aStart, b.data, 0);
	}

	public static float mul4(float[] a, int aStart, float[] b, int bStart) {
		return a[aStart] * b[bStart]
				+ a[aStart + 1] * b[bStart + 1]
				+ a[aStart + 2] * b[bStart + 2]
				+ a[aStart + 3] * b[bStart + 3];
	}

	public static float mul4(float[] a, int aStart, int aSpacing,
							 float[] b, int bStart, int bSpacing) {
		return a[aStart] * b[bStart]
				+ a[aStart + aSpacing] * b[bStart + bSpacing]
				+ a[aStart + aSpacing + aSpacing] * b[bStart + bSpacing + bSpacing]
				+ a[aStart + aSpacing + aSpacing + aSpacing] * b[bStart + bSpacing + bSpacing + bSpacing];
	}

	public static float mul(Vec4f a, Vec4f b) {
		return mul4(a.data, 0, b.data, 0);
	}

	public static Vec2f mul4(float[] a, int aStart, Mat2x4f b) {
		return new Vec2f(
				mul4(a, aStart, 1, b.data, 0, 2),
				mul4(a, aStart, 1, b.data, 1, 2)
		);
	}

	public static Vec3f mul4(float[] a, int aStart, Mat3x4f b) {
		return new Vec3f(
				mul4(a, aStart, 1, b.data, 0, 3),
				mul4(a, aStart, 1, b.data, 1, 3),
				mul4(a, aStart, 1, b.data, 2, 3)
		);
	}

	public static Vec4f mul4(float[] a, int aStart, Mat4f b) {
		return new Vec4f(
				mul4(a, aStart, 1, b.data, 0, 4),
				mul4(a, aStart, 1, b.data, 1, 4),
				mul4(a, aStart, 1, b.data, 2, 4),
				mul4(a, aStart, 1, b.data, 3, 4)
		);
	}

	public static Vec2f mul(Vec4f a, Mat2x4f b) {
		return mul4(a.data, 0, b);
	}

	public static Vec3f mul(Vec4f a, Mat3x4f b) {
		return mul4(a.data, 0, b);
	}

	public static Vec4f mul(Vec4f a, Mat4f b) {
		return mul4(a.data, 0, b);
	}

	public static Vec2f mul(Mat4x2f a, Vec4f b) {
		return new Vec2f(
				mul4(a.data, 0, b),
				mul4(a.data, 4, b)
		);
	}

	public static Mat2f mul(Mat4x2f a, Mat2x4f b) {
		return new Mat2f(
				mul4(a.data, 0, b),
				mul4(a.data, 4, b)
		);
	}

	public static Mat3x2f mul(Mat4x2f a, Mat3x4f b) {
		return new Mat3x2f(
				mul4(a.data, 0, b),
				mul4(a.data, 4, b)
		);
	}

	public static Mat4x2f mul(Mat4x2f a, Mat4f b) {
		return new Mat4x2f(
				mul4(a.data, 0, b),
				mul4(a.data, 4, b)
		);
	}

	public static Vec3f mul(Mat4x3f a, Vec4f b) {
		return new Vec3f(
				mul4(a.data, 0, b),
				mul4(a.data, 4, b),
				mul4(a.data, 8, b)
		);
	}

	public static Mat2x3f mul(Mat4x3f a, Mat2x4f b) {
		return new Mat2x3f(
				mul4(a.data, 0, b),
				mul4(a.data, 4, b),
				mul4(a.data, 8, b)
		);
	}

	public static Mat3f mul(Mat4x3f a, Mat3x4f b) {
		return new Mat3f(
				mul4(a.data, 0, b),
				mul4(a.data, 4, b),
				mul4(a.data, 8, b)
		);
	}

	public static Mat4x3f mul(Mat4x3f a, Mat4f b) {
		return new Mat4x3f(
				mul4(a.data, 0, b),
				mul4(a.data, 4, b),
				mul4(a.data, 8, b)
		);
	}

	public static Vec4f mul(Mat4f a, Vec4f b) {
		return new Vec4f(
				mul4(a.data, 0, b),
				mul4(a.data, 4, b),
				mul4(a.data, 8, b),
				mul4(a.data, 12, b)
		);
	}

	public static Mat2x4f mul(Mat4f a, Mat2x4f b) {
		return new Mat2x4f(
				mul4(a.data, 0, b),
				mul4(a.data, 4, b),
				mul4(a.data, 8, b),
				mul4(a.data, 12, b)
		);
	}

	public static Mat3x4f mul(Mat4f a, Mat3x4f b) {
		return new Mat3x4f(
				mul4(a.data, 0, b),
				mul4(a.data, 4, b),
				mul4(a.data, 8, b),
				mul4(a.data, 12, b)
		);
	}

	public static Mat4f mul(Mat4f a, Mat4f b) {
		return new Mat4f(
				mul4(a.data, 0, b),
				mul4(a.data, 4, b),
				mul4(a.data, 8, b),
				mul4(a.data, 12, b)
		);
	}
}
