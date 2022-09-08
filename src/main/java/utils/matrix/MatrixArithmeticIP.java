package utils.matrix;

import utils.vector.Vec2f;
import utils.vector.Vec3f;
import utils.vector.Vec4f;

/**
 * Arithmetic operations on matrices and vectors that operate in-place
 */
public class MatrixArithmeticIP {

	private static void mul2(float[] a, int aStart, Mat2f b) {
		float tempX = MatrixArithmeticOOP.mul2(a, aStart, 1, b.data, 0, 2);
		float tempY = MatrixArithmeticOOP.mul2(a, aStart, 1, b.data, 1, 2);
		a[aStart] = tempX;
		a[aStart + 1] = tempY;
	}

	private static void mul2(Mat2f a, float[] b, int bStart, int bSpacing) {
		float tempX = MatrixArithmeticOOP.mul2(a.data, 0, 1, b, bStart, bSpacing);
		float tempY = MatrixArithmeticOOP.mul2(a.data, 2, 1, b, bStart, bSpacing);
		b[bStart] = tempX;
		b[bStart + bSpacing] = tempY;
	}

	private static void mul3(float[] a, int aStart, Mat3f b) {
		float tempX = MatrixArithmeticOOP.mul3(a, aStart, 1, b.data, 0, 3);
		float tempY = MatrixArithmeticOOP.mul3(a, aStart, 1, b.data, 1, 3);
		float tempZ = MatrixArithmeticOOP.mul3(a, aStart, 1, b.data, 2, 3);
		a[aStart] = tempX;
		a[aStart + 1] = tempY;
		a[aStart + 2] = tempZ;
	}

	private static void mul3(Mat3f a, float[] b, int bStart, int bSpacing) {
		float tempX = MatrixArithmeticOOP.mul3(a.data, 0, 1, b, bStart, bSpacing);
		float tempY = MatrixArithmeticOOP.mul3(a.data, 3, 1, b, bStart, bSpacing);
		float tempZ = MatrixArithmeticOOP.mul3(a.data, 6, 1, b, bStart, bSpacing);
		b[bStart] = tempX;
		b[bStart + bSpacing] = tempY;
		b[bStart + bSpacing + bSpacing] = tempZ;
	}

	private static void mul4(float[] a, int aStart, Mat4f b) {
		float tempX = MatrixArithmeticOOP.mul4(a, aStart, 1, b.data, 0, 4);
		float tempY = MatrixArithmeticOOP.mul4(a, aStart, 1, b.data, 1, 4);
		float tempZ = MatrixArithmeticOOP.mul4(a, aStart, 1, b.data, 2, 4);
		float tempW = MatrixArithmeticOOP.mul4(a, aStart, 1, b.data, 3, 4);
		a[aStart] = tempX;
		a[aStart + 1] = tempY;
		a[aStart + 2] = tempZ;
		a[aStart + 3] = tempW;
	}

	private static void mul4(Mat4f a, float[] b, int bStart, int bSpacing) {
		float tempX = MatrixArithmeticOOP.mul4(a.data, 0, 1, b, bStart, bSpacing);
		float tempY = MatrixArithmeticOOP.mul4(a.data, 4, 1, b, bStart, bSpacing);
		float tempZ = MatrixArithmeticOOP.mul4(a.data, 8, 1, b, bStart, bSpacing);
		float tempW = MatrixArithmeticOOP.mul4(a.data, 12, 1, b, bStart, bSpacing);
		b[bStart] = tempX;
		b[bStart + bSpacing] = tempY;
		b[bStart + bSpacing + bSpacing] = tempZ;
		b[bStart + bSpacing + bSpacing + bSpacing] = tempW;
	}

	private static void mul4(float[] target, int targetStart, float[] a, int aStart, Mat4f b) {
		target[targetStart] = MatrixArithmeticOOP.mul4(a, aStart, 1, b.data, 0, 4);
		target[targetStart + 1] = MatrixArithmeticOOP.mul4(a, aStart, 1, b.data, 1, 4);
		target[targetStart + 2] = MatrixArithmeticOOP.mul4(a, aStart, 1, b.data, 2, 4);
		target[targetStart + 3] = MatrixArithmeticOOP.mul4(a, aStart, 1, b.data, 3, 4);
	}

	/**
	 * Performs linear algebraic multiply and stores result in `a`
	 */
	public static Vec2f mul(Vec2f a, Mat2f b) {
		mul2(a.data, 0, b);
		return a;
	}

	/**
	 * Performs linear algebraic multiply and stores result in `b`
	 */
	public static Vec2f mul(Mat2f a, Vec2f b) {
		mul2(a, b.data, 0, 1);
		return b;
	}

	/**
	 * Performs linear algebraic multiply and stores result in `a`
	 */
	public static Mat2f mul(Mat2f a, Mat2f b) {
		mul2(a.data, 0, b);
		mul2(a.data, 2, b);
		return a;
	}

	/**
	 * Performs linear algebraic multiply and stores result in `b`
	 */
	public static Mat3x2f mul(Mat2f a, Mat3x2f b) {
		mul2(a, b.data, 0, 3);
		mul2(a, b.data, 1, 3);
		mul2(a, b.data, 2, 3);
		return b;
	}

	/**
	 * Performs linear algebraic multiply and stores result in `b`
	 */
	public static Mat4x2f mul(Mat2f a, Mat4x2f b) {
		mul2(a, b.data, 0, 4);
		mul2(a, b.data, 1, 4);
		mul2(a, b.data, 2, 4);
		mul2(a, b.data, 3, 4);
		return b;
	}

	/**
	 * Performs linear algebraic multiply and stores result in `a`
	 */
	public static Mat2x3f mul(Mat2x3f a, Mat2f b) {
		mul2(a.data, 0, b);
		mul2(a.data, 2, b);
		mul2(a.data, 4, b);
		return a;
	}

	/**
	 * Performs linear algebraic multiply and stores result in `a`
	 */
	public static Mat2x4f mul(Mat2x4f a, Mat2f b) {
		mul2(a.data, 0, b);
		mul2(a.data, 2, b);
		mul2(a.data, 4, b);
		mul2(a.data, 6, b);
		return a;
	}

	/**
	 * Performs linear algebraic multiply and stores result in `a`
	 */
	public static Vec3f mul(Vec3f a, Mat3f b) {
		mul3(a.data, 0, b);
		return a;
	}

	/**
	 * Performs linear algebraic multiply and stores result in `a`
	 */
	public static Mat3x2f mul(Mat3x2f a, Mat3f b) {
		mul3(a.data, 0, b);
		mul3(a.data, 3, b);
		return a;
	}

	/**
	 * Performs linear algebraic multiply and stores result in `b`
	 */
	public static Vec3f mul(Mat3f a, Vec3f b) {
		mul3(a, b.data, 0, 1);
		return b;
	}

	/**
	 * Performs linear algebraic multiply and stores result in `b`
	 */
	public static Mat2x3f mul(Mat3f a, Mat2x3f b) {
		mul3(a, b.data, 0, 2);
		mul3(a, b.data, 1, 2);
		return b;
	}

	/**
	 * Performs linear algebraic multiply and stores result in `a`
	 */
	public static Mat3f mul(Mat3f a, Mat3f b) {
		mul3(a.data, 0, b);
		mul3(a.data, 3, b);
		mul3(a.data, 6, b);
		return a;
	}


	/**
	 * Performs linear algebraic multiply and stores result in `b`
	 */
	public static Mat4x3f mul(Mat3f a, Mat4x3f b) {
		mul3(a, b.data, 0, 4);
		mul3(a, b.data, 1, 4);
		mul3(a, b.data, 2, 4);
		mul3(a, b.data, 3, 4);
		return b;
	}

	/**
	 * Performs linear algebraic multiply and stores result in `a`
	 */
	public static Mat3x4f mul(Mat3x4f a, Mat3f b) {
		mul3(a.data, 0, b);
		mul3(a.data, 3, b);
		mul3(a.data, 6, b);
		mul3(a.data, 9, b);
		return a;
	}


	/**
	 * Performs linear algebraic multiply and stores result in `a`
	 */
	public static Vec4f mul(Vec4f a, Mat4f b) {
		mul4(a.data, 0, b);
		return a;
	}

	/**
	 * Performs linear algebraic multiply and stores result in `a`
	 */
	public static Mat4x2f mul(Mat4x2f a, Mat4f b) {
		mul4(a.data, 0, b);
		mul4(a.data, 4, b);
		return a;
	}

	/**
	 * Performs linear algebraic multiply and stores result in `a`
	 */
	public static Mat4x3f mul(Mat4x3f a, Mat4f b) {
		mul4(a.data, 0, b);
		mul4(a.data, 4, b);
		mul4(a.data, 8, b);
		return a;
	}

	/**
	 * Performs linear algebraic multiply and stores result in `b`
	 */
	public static Vec4f mul(Mat4f a, Vec4f b) {
		mul4(a, b.data, 0, 1);
		return b;
	}

	/**
	 * Performs linear algebraic multiply and stores result in `b`
	 */
	public static Mat2x4f mul(Mat4f a, Mat2x4f b) {
		mul4(a, b.data, 0, 2);
		mul4(a, b.data, 1, 2);
		return b;
	}

	/**
	 * Performs linear algebraic multiply and stores result in `b`
	 */
	public static Mat3x4f mul(Mat4f a, Mat3x4f b) {
		mul4(a, b.data, 0, 3);
		mul4(a, b.data, 1, 3);
		mul4(a, b.data, 2, 3);
		return b;
	}

	/**
	 * Performs linear algebraic multiply and stores result in `a`
	 */
	public static Mat4f mul(Mat4f a, Mat4f b) {
		mul4(a.data, 0, b);
		mul4(a.data, 4, b);
		mul4(a.data, 8, b);
		mul4(a.data, 12, b);
		return a;
	}

	/**
	 * Performs linear algebraic multiply and stores result in `target`
	 */
	public static Mat4f mul(Mat4f target, Mat4f a, Mat4f b) {
		mul4(target.data, 0, a.data, 0, b);
		mul4(target.data, 4, a.data, 4, b);
		mul4(target.data, 8, a.data, 8, b);
		mul4(target.data, 12, a.data, 12, b);
		return target;
	}
}
