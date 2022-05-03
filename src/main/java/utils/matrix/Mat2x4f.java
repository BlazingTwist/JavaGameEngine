package utils.matrix;

import java.nio.FloatBuffer;
import java.util.function.BiConsumer;
import org.lwjgl.opengl.GL45;
import rendering.shaderdata.IBufferData;
import utils.operator.IOperator;
import utils.vector.Vec2f;
import utils.vector.Vec4f;

/**
 * Matrix with 2 columns and 4 rows
 */
public class Mat2x4f implements IBufferData {
	public static final int DATA_LEN = 2*4;
	public final float[] data = {
			0f, 0f,
			0f, 0f,
			0f, 0f,
			0f, 0f
	};

	public Mat2x4f() {
		data[0] = 1f;
		data[3] = 1f;
	}

	public Mat2x4f(float scalar) {
		data[0] = scalar;
		data[3] = scalar;
	}

	public Mat2x4f(float c00, float c10,
				   float c01, float c11,
				   float c02, float c12,
				   float c03, float c13) {
		data[0] = c00;
		data[1] = c10;
		data[2] = c01;
		data[3] = c11;
		data[4] = c02;
		data[5] = c12;
		data[6] = c03;
		data[7] = c13;
	}

	public Mat2x4f(Vec2f row1, Vec2f row2, Vec2f row3, Vec2f row4) {
		System.arraycopy(row1.data, 0, data, 0, 2);
		System.arraycopy(row2.data, 0, data, 2, 2);
		System.arraycopy(row3.data, 0, data, 4, 2);
		System.arraycopy(row4.data, 0, data, 6, 2);
	}

	public Mat2x4f(Vec4f col1, Vec4f col2) {
		this(
				col1.data[0], col2.data[0],
				col1.data[1], col2.data[1],
				col1.data[2], col2.data[2],
				col1.data[3], col2.data[3]
		);
	}

	public Mat2x4f(Mat2x4f other) {
		System.arraycopy(other.data, 0, data, 0, DATA_LEN);
	}

	public Mat2x4f copy() {
		return new Mat2x4f(this);
	}

	public Mat2x4f set(float c00, float c10,
					   float c01, float c11,
					   float c02, float c12,
					   float c03, float c13) {
		data[0] = c00;
		data[1] = c10;
		data[2] = c01;
		data[3] = c11;
		data[4] = c02;
		data[5] = c12;
		data[6] = c03;
		data[7] = c13;
		return this;
	}

	public Mat2x4f apply(IOperator operator, float scalar) {
		operator.apply(data, 0, DATA_LEN, scalar);
		return this;
	}

	public Mat2x4f apply(IOperator operator, Mat2x4f other) {
		operator.apply(data, 0, DATA_LEN, other.data);
		return this;
	}

	public Mat4x2f transpose() {
		return new Mat4x2f(
				data[0], data[2], data[4], data[6],
				data[1], data[3], data[5], data[7]
		);
	}

	@Override
	public String toString() {
		return "Mat2x4f{"
				+ "\n\t" + data[0] + "\t" + data[1]
				+ "\n\t" + data[2] + "\t" + data[3]
				+ "\n\t" + data[4] + "\t" + data[5]
				+ "\n\t" + data[6] + "\t" + data[7]
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
		return (location, buffer) -> GL45.glUniformMatrix2x4fv(location, true, buffer);
	}
}
