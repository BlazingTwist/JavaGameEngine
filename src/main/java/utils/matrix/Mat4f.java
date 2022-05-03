package utils.matrix;

import java.nio.FloatBuffer;
import java.util.function.BiConsumer;
import org.lwjgl.opengl.GL45;
import rendering.shaderdata.IBufferData;
import utils.operator.IOperator;
import utils.vector.Vec4f;

/**
 * Matrix with 4 columns and 4 rows
 */
public class Mat4f implements IBufferData {
	public static final int DATA_LEN = 4*4;
	public final float[] data = {
			0f, 0f, 0f, 0f,
			0f, 0f, 0f, 0f,
			0f, 0f, 0f, 0f,
			0f, 0f, 0f, 0f,
	};

	public Mat4f() {
		data[0] = 1f;
		data[5] = 1f;
		data[10] = 1f;
		data[15] = 1f;
	}

	public Mat4f(float scalar) {
		data[0] = scalar;
		data[5] = scalar;
		data[10] = scalar;
		data[15] = scalar;
	}

	public Mat4f(float c00, float c10, float c20, float c30,
				 float c01, float c11, float c21, float c31,
				 float c02, float c12, float c22, float c32,
				 float c03, float c13, float c23, float c33) {
		data[0] = c00;
		data[1] = c10;
		data[2] = c20;
		data[3] = c30;

		data[4] = c01;
		data[5] = c11;
		data[6] = c21;
		data[7] = c31;

		data[8] = c02;
		data[9] = c12;
		data[10] = c22;
		data[11] = c32;

		data[12] = c03;
		data[13] = c13;
		data[14] = c23;
		data[15] = c33;
	}

	public Mat4f(Vec4f row1, Vec4f row2, Vec4f row3, Vec4f row4) {
		System.arraycopy(row1.data, 0, data, 0, 4);
		System.arraycopy(row2.data, 0, data, 4, 4);
		System.arraycopy(row3.data, 0, data, 8, 4);
		System.arraycopy(row4.data, 0, data, 12, 4);
	}

	public Mat4f(Mat4f other) {
		System.arraycopy(other.data, 0, data, 0, DATA_LEN);
	}

	public Mat4f copy() {
		return new Mat4f(this);
	}

	@SuppressWarnings("DuplicatedCode")
	public Mat4f set(float c00, float c10, float c20, float c30,
					 float c01, float c11, float c21, float c31,
					 float c02, float c12, float c22, float c32,
					 float c03, float c13, float c23, float c33) {
		data[0] = c00;
		data[1] = c10;
		data[2] = c20;
		data[3] = c30;

		data[4] = c01;
		data[5] = c11;
		data[6] = c21;
		data[7] = c31;

		data[8] = c02;
		data[9] = c12;
		data[10] = c22;
		data[11] = c32;

		data[12] = c03;
		data[13] = c13;
		data[14] = c23;
		data[15] = c33;
		return this;
	}

	public Mat4f apply(IOperator operator, float scalar) {
		operator.apply(data, 0, DATA_LEN, scalar);
		return this;
	}

	public Mat4f apply(IOperator operator, Mat4f other) {
		operator.apply(data, 0, DATA_LEN, other.data);
		return this;
	}

	public float determinant() {
		// shamelessly stolen from glm func_matrix.inl
		final float subFactor00 = data[10] * data[15] - data[11] * data[14];
		final float subFactor01 = data[6] * data[15] - data[7] * data[14];
		final float subFactor02 = data[6] * data[11] - data[7] * data[10];
		final float subFactor03 = data[2] * data[15] - data[3] * data[14];
		final float subFactor04 = data[2] * data[11] - data[3] * data[10];
		final float subFactor05 = data[2] * data[7] - data[3] * data[6];

		final float cof00 = data[5] * subFactor00 - data[9] * subFactor01 + data[13] * subFactor02;
		final float cof01 = data[1] * subFactor00 - data[9] * subFactor03 + data[13] * subFactor04;
		final float cof02 = data[1] * subFactor01 - data[5] * subFactor03 + data[13] * subFactor05;
		final float cof03 = data[1] * subFactor02 - data[5] * subFactor04 + data[9] * subFactor05;

		return data[0] * cof00 - data[4] * cof01
				+ data[8] * cof02 - data[12] * cof03;
	}

	// TODO inverse (so much effort though...)

	public Mat4f transpose() {
		return new Mat4f(
				data[0], data[4], data[8], data[12],
				data[1], data[5], data[9], data[13],
				data[2], data[6], data[10], data[14],
				data[3], data[7], data[11], data[15]
		);
	}

	public Mat4f transposeIP() {
		float temp = data[1];
		data[1] = data[4];
		data[4] = temp;

		temp = data[2];
		data[2] = data[8];
		data[8] = temp;

		temp = data[3];
		data[3] = data[12];
		data[12] = temp;

		temp = data[6];
		data[6] = data[9];
		data[9] = temp;

		temp = data[7];
		data[7] = data[13];
		data[13] = temp;

		temp = data[11];
		data[11] = data[14];
		data[14] = temp;

		return this;
	}

	@Override
	public String toString() {
		return "Mat4f{"
				+ "\n\t" + data[0] + "\t" + data[1] + "\t" + data[2] + "\t" + data[3]
				+ "\n\t" + data[4] + "\t" + data[5] + "\t" + data[6] + "\t" + data[7]
				+ "\n\t" + data[8] + "\t" + data[9] + "\t" + data[10] + "\t" + data[11]
				+ "\n\t" + data[12] + "\t" + data[13] + "\t" + data[14] + "\t" + data[15]
				+ "\n}";
	}

	@Override
	public int bytes() {
		return Float.BYTES * DATA_LEN;
	}

	@Override
	public void writeToBuffer(FloatBuffer buffer) {
		buffer.put(data, 0, DATA_LEN);
	}

	@Override
	public BiConsumer<Integer, FloatBuffer> getBindBufferFunction() {
		return (location, buffer) -> GL45.glUniformMatrix4fv(location, true, buffer);
	}
}
