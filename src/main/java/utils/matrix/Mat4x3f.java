package utils.matrix;

import java.nio.FloatBuffer;
import java.util.function.BiConsumer;
import org.lwjgl.opengl.GL45;
import rendering.shaderdata.IBufferData;
import utils.operator.IOperator;
import utils.vector.Vec3f;
import utils.vector.Vec4f;

/**
 * Matrix with 4 columns and 3 rows
 */
public class Mat4x3f implements IBufferData {
	public static final int DATA_LEN = 4*3;
	public final float[] data = {
			0f, 0f, 0f, 0f,
			0f, 0f, 0f, 0f,
			0f, 0f, 0f, 0f,
	};

	public Mat4x3f() {
		data[0] = 1f;
		data[5] = 1f;
		data[10] = 1f;
	}

	public Mat4x3f(float scalar) {
		data[0] = scalar;
		data[5] = scalar;
		data[10] = scalar;
	}

	public Mat4x3f(float c00, float c10, float c20, float c30,
				   float c01, float c11, float c21, float c31,
				   float c02, float c12, float c22, float c32) {
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
	}

	public Mat4x3f(Vec4f row1, Vec4f row2, Vec4f row3) {
		System.arraycopy(row1.data, 0, data, 0, 4);
		System.arraycopy(row2.data, 0, data, 4, 4);
		System.arraycopy(row3.data, 0, data, 8, 4);
	}

	public Mat4x3f(Vec3f col1, Vec3f col2, Vec3f col3, Vec3f col4) {
		this(
				col1.data[0], col2.data[0], col3.data[0], col4.data[0],
				col1.data[1], col2.data[1], col3.data[1], col4.data[1],
				col1.data[2], col2.data[2], col3.data[2], col4.data[2]
		);
	}

	public Mat4x3f(Mat4x3f other) {
		System.arraycopy(other.data, 0, data, 0, DATA_LEN);
	}

	public Mat4x3f copy() {
		return new Mat4x3f(this);
	}

	@SuppressWarnings("DuplicatedCode")
	public Mat4x3f set(float c00, float c10, float c20, float c30,
					   float c01, float c11, float c21, float c31,
					   float c02, float c12, float c22, float c32) {
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
		return this;
	}

	public Mat4x3f apply(IOperator operator, float scalar) {
		operator.apply(data, 0, DATA_LEN, scalar);
		return this;
	}

	public Mat4x3f apply(IOperator operator, Mat4x3f other) {
		operator.apply(data, 0, DATA_LEN, other.data);
		return this;
	}

	public Mat3x4f transpose() {
		return new Mat3x4f(
				data[0], data[4], data[8],
				data[1], data[5], data[9],
				data[2], data[6], data[10],
				data[3], data[7], data[11]
		);
	}

	@Override
	public String toString() {
		return "Mat4x3f{"
				+ "\n\t" + data[0] + "\t" + data[1] + "\t" + data[2] + "\t" + data[3]
				+ "\n\t" + data[4] + "\t" + data[5] + "\t" + data[6] + "\t" + data[7]
				+ "\n\t" + data[8] + "\t" + data[9] + "\t" + data[10] + "\t" + data[11]
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
		return (location, buffer) -> GL45.glUniformMatrix4x3fv(location, true, buffer);
	}
}
