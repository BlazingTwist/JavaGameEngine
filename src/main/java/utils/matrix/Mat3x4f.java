package utils.matrix;

import java.nio.FloatBuffer;
import java.util.function.BiConsumer;
import org.lwjgl.opengl.GL45;
import rendering.shaderdata.IBufferData;
import utils.operator.IOperator;
import utils.vector.Vec3f;
import utils.vector.Vec4f;

/**
 * Matrix with 3 columns and 4 rows
 */
public class Mat3x4f implements IBufferData {
	public static final int DATA_LEN = 3*4;
	public final float[] data = {
			0f, 0f, 0f,
			0f, 0f, 0f,
			0f, 0f, 0f,
			0f, 0f, 0f,
	};

	public Mat3x4f() {
		data[0] = 1f;
		data[4] = 1f;
		data[8] = 1f;
	}

	public Mat3x4f(float scalar) {
		data[0] = scalar;
		data[4] = scalar;
		data[8] = scalar;
	}

	public Mat3x4f(float c00, float c10, float c20,
				   float c01, float c11, float c21,
				   float c02, float c12, float c22,
				   float c03, float c13, float c23) {
		data[0] = c00;
		data[1] = c10;
		data[2] = c20;

		data[3] = c01;
		data[4] = c11;
		data[5] = c21;

		data[6] = c02;
		data[7] = c12;
		data[8] = c22;

		data[9] = c03;
		data[10] = c13;
		data[11] = c23;
	}

	public Mat3x4f(Vec3f row1, Vec3f row2, Vec3f row3, Vec3f row4) {
		System.arraycopy(row1.data, 0, data, 0, 3);
		System.arraycopy(row2.data, 0, data, 3, 3);
		System.arraycopy(row3.data, 0, data, 6, 3);
		System.arraycopy(row4.data, 0, data, 9, 3);
	}

	public Mat3x4f(Vec4f col1, Vec4f col2, Vec4f col3) {
		this(
				col1.data[0], col2.data[0], col3.data[0],
				col1.data[1], col2.data[1], col3.data[1],
				col1.data[2], col2.data[2], col3.data[2],
				col1.data[3], col2.data[3], col3.data[3]
		);
	}

	public Mat3x4f(Mat3x4f other) {
		System.arraycopy(other.data, 0, data, 0, DATA_LEN);
	}

	public Mat3x4f copy() {
		return new Mat3x4f(this);
	}

	@SuppressWarnings("DuplicatedCode")
	public Mat3x4f set(float c00, float c10, float c20,
					   float c01, float c11, float c21,
					   float c02, float c12, float c22,
					   float c03, float c13, float c23) {
		data[0] = c00;
		data[1] = c10;
		data[2] = c20;

		data[3] = c01;
		data[4] = c11;
		data[5] = c21;

		data[6] = c02;
		data[7] = c12;
		data[8] = c22;

		data[9] = c03;
		data[10] = c13;
		data[11] = c23;
		return this;
	}

	public Mat3x4f apply(IOperator operator, float scalar) {
		operator.apply(data, 0, DATA_LEN, scalar);
		return this;
	}

	public Mat3x4f apply(IOperator operator, Mat3x4f other) {
		operator.apply(data, 0, DATA_LEN, other.data);
		return this;
	}

	public Mat4x3f transpose() {
		return new Mat4x3f(
				data[0], data[3], data[6], data[9],
				data[1], data[4], data[7], data[10],
				data[2], data[5], data[8], data[11]
		);
	}

	@Override
	public String toString() {
		return "Mat3x4f{"
				+ "\n\t" + data[0] + "\t" + data[1] + "\t" + data[2]
				+ "\n\t" + data[3] + "\t" + data[4] + "\t" + data[5]
				+ "\n\t" + data[6] + "\t" + data[7] + "\t" + data[8]
				+ "\n\t" + data[9] + "\t" + data[10] + "\t" + data[11]
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
		return (location, buffer) -> GL45.glUniformMatrix3x4fv(location, true, buffer);
	}
}
