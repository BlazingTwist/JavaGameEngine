package utils.matrix;

import java.nio.FloatBuffer;
import java.util.function.BiConsumer;
import org.lwjgl.opengl.GL45;
import rendering.shaderdata.IBufferData;
import utils.operator.IOperator;
import utils.vector.Vec2f;
import utils.vector.Vec3f;

/**
 * Matrix with 2 columns and 3 rows
 */
public class Mat2x3f implements IBufferData {
	public static final int DATA_LEN = 2*3;
	public final float[] data = {
			0f, 0f,
			0f, 0f,
			0f, 0f
	};

	public Mat2x3f() {
		data[0] = 1f;
		data[3] = 1f;
	}

	public Mat2x3f(float scalar) {
		data[0] = scalar;
		data[3] = scalar;
	}

	public Mat2x3f(float c00, float c10,
				   float c01, float c11,
				   float c02, float c12) {
		data[0] = c00;
		data[1] = c10;
		data[2] = c01;
		data[3] = c11;
		data[4] = c02;
		data[5] = c12;
	}

	public Mat2x3f(Vec2f row1, Vec2f row2, Vec2f row3) {
		System.arraycopy(row1.data, 0, data, 0, 2);
		System.arraycopy(row2.data, 0, data, 2, 2);
		System.arraycopy(row3.data, 0, data, 4, 2);
	}

	public Mat2x3f(Vec3f col1, Vec3f col2) {
		this(
				col1.data[0], col2.data[0],
				col1.data[1], col2.data[1],
				col1.data[2], col2.data[2]
		);
	}

	public Mat2x3f(Mat2x3f other) {
		System.arraycopy(other.data, 0, data, 0, DATA_LEN);
	}

	public Mat2x3f copy() {
		return new Mat2x3f(this);
	}

	public Mat2x3f set(float c00, float c10,
					   float c01, float c11,
					   float c02, float c12) {
		data[0] = c00;
		data[1] = c10;
		data[2] = c01;
		data[3] = c11;
		data[4] = c02;
		data[5] = c12;
		return this;
	}

	public Mat2x3f apply(IOperator operator, float scalar) {
		operator.apply(data, 0, DATA_LEN, scalar);
		return this;
	}

	public Mat2x3f apply(IOperator operator, Mat2x3f other) {
		operator.apply(data, 0, DATA_LEN, other.data);
		return this;
	}

	public Mat3x2f transpose() {
		return new Mat3x2f(
				data[0], data[2], data[4],
				data[1], data[3], data[5]
		);
	}

	@Override
	public String toString() {
		return "Mat2x3f{"
				+ "\n\t" + data[0] + "\t" + data[1]
				+ "\n\t" + data[2] + "\t" + data[3]
				+ "\n\t" + data[4] + "\t" + data[5]
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
		return (location, buffer) -> GL45.glUniformMatrix2x3fv(location, true, buffer);
	}
}
