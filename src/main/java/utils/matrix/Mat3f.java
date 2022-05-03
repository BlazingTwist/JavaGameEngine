package utils.matrix;

import java.nio.FloatBuffer;
import java.util.function.BiConsumer;
import org.lwjgl.opengl.GL45;
import rendering.shaderdata.IBufferData;
import utils.operator.IOperator;
import utils.vector.Vec3f;

/**
 * Matrix with 3 columns and 3 rows
 */
public class Mat3f implements IBufferData {
	public static final int DATA_LEN = 3*3;
	public final float[] data = {
			0f, 0f, 0f,
			0f, 0f, 0f,
			0f, 0f, 0f,
	};

	public Mat3f() {
		data[0] = 1f;
		data[4] = 1f;
		data[8] = 1f;
	}

	public Mat3f(float scalar) {
		data[0] = scalar;
		data[4] = scalar;
		data[8] = scalar;
	}

	public Mat3f(float c00, float c10, float c20,
				 float c01, float c11, float c21,
				 float c02, float c12, float c22) {
		data[0] = c00;
		data[1] = c10;
		data[2] = c20;

		data[3] = c01;
		data[4] = c11;
		data[5] = c21;

		data[6] = c02;
		data[7] = c12;
		data[8] = c22;
	}

	public Mat3f(Vec3f row1, Vec3f row2, Vec3f row3) {
		System.arraycopy(row1.data, 0, data, 0, 3);
		System.arraycopy(row2.data, 0, data, 3, 3);
		System.arraycopy(row3.data, 0, data, 6, 3);
	}

	public Mat3f(Mat3f other) {
		System.arraycopy(other.data, 0, data, 0, DATA_LEN);
	}

	public Mat3f copy() {
		return new Mat3f(this);
	}

	public Mat3f set(float c00, float c10, float c20,
					 float c01, float c11, float c21,
					 float c02, float c12, float c22) {
		data[0] = c00;
		data[1] = c10;
		data[2] = c20;

		data[3] = c01;
		data[4] = c11;
		data[5] = c21;

		data[6] = c02;
		data[7] = c12;
		data[8] = c22;
		return this;
	}

	public Mat3f apply(IOperator operator, float scalar) {
		operator.apply(data, 0, DATA_LEN, scalar);
		return this;
	}

	public Mat3f apply(IOperator operator, Mat3f other) {
		operator.apply(data, 0, DATA_LEN, other.data);
		return this;
	}

	public float determinant() {
		return data[0] * (data[4] * data[8] - data[5] * data[7])
				+ data[1] * (data[5] * data[6] - data[3] * data[8])
				+ data[2] * (data[3] * data[7] - data[4] * data[6]);
	}

	public Mat3f inverse() {
		float oneOverDet = 1f / determinant();
		return new Mat3f(
				(data[4] * data[8] - data[5] * data[7]) * oneOverDet,
				(data[2] * data[7] - data[1] * data[8]) * oneOverDet,
				(data[1] * data[5] - data[2] * data[4]) * oneOverDet,
				(data[5] * data[6] - data[3] * data[8]) * oneOverDet,
				(data[0] * data[8] - data[2] * data[6]) * oneOverDet,
				(data[2] * data[3] - data[0] * data[5]) * oneOverDet,
				(data[3] * data[7] - data[4] * data[6]) * oneOverDet,
				(data[1] * data[6] - data[0] * data[7]) * oneOverDet,
				(data[0] * data[4] - data[1] * data[3]) * oneOverDet
		);
	}

	public Mat3f transpose() {
		return new Mat3f(
				data[0], data[3], data[6],
				data[1], data[4], data[7],
				data[2], data[5], data[8]
		);
	}

	public Mat3f transposeIP() {
		float temp = data[1];
		data[1] = data[3];
		data[3] = temp;

		temp = data[2];
		data[2] = data[6];
		data[6] = temp;

		temp = data[5];
		data[5] = data[7];
		data[7] = temp;

		return this;
	}

	@Override
	public String toString() {
		return "Mat3f{"
				+ "\n\t" + data[0] + "\t" + data[1] + "\t" + data[2]
				+ "\n\t" + data[3] + "\t" + data[4] + "\t" + data[5]
				+ "\n\t" + data[6] + "\t" + data[7] + "\t" + data[8]
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
		return (location, buffer) -> GL45.glUniformMatrix3fv(location, true, buffer);
	}
}
