package utils.matrix;

import java.nio.FloatBuffer;
import java.util.function.BiConsumer;
import org.lwjgl.opengl.GL45;
import rendering.shaderdata.IBufferData;
import utils.operator.IOperator;
import utils.vector.Vec2f;
import utils.vector.Vec3f;

/**
 * Matrix with 3 columns and 2 rows
 */
public class Mat3x2f implements IBufferData {
	public static final int DATA_LEN = 3*2;
	public final float[] data = {
			0f, 0f, 0f,
			0f, 0f, 0f,
	};

	public Mat3x2f() {
		data[0] = 1f;
		data[4] = 1f;
	}

	public Mat3x2f(float scalar) {
		data[0] = scalar;
		data[4] = scalar;
	}

	public Mat3x2f(float c00, float c10, float c20,
				 float c01, float c11, float c21) {
		data[0] = c00;
		data[1] = c10;
		data[2] = c20;

		data[3] = c01;
		data[4] = c11;
		data[5] = c21;
	}

	public Mat3x2f(Vec3f row1, Vec3f row2) {
		System.arraycopy(row1.data, 0, data, 0, 3);
		System.arraycopy(row2.data, 0, data, 3, 3);
	}

	public Mat3x2f(Vec2f col1, Vec2f col2, Vec2f col3) {
		this(
				col1.data[0], col2.data[0], col3.data[0],
				col1.data[1], col2.data[1], col3.data[1]
		);
	}

	public Mat3x2f(Mat3x2f other) {
		System.arraycopy(other.data, 0, data, 0, DATA_LEN);
	}

	public Mat3x2f copy() {
		return new Mat3x2f(this);
	}

	public Mat3x2f set(float c00, float c10, float c20,
				   float c01, float c11, float c21) {
		data[0] = c00;
		data[1] = c10;
		data[2] = c20;

		data[3] = c01;
		data[4] = c11;
		data[5] = c21;
		return this;
	}

	public Mat3x2f apply(IOperator operator, float scalar) {
		operator.apply(data, 0, DATA_LEN, scalar);
		return this;
	}

	public Mat3x2f apply(IOperator operator, Mat3x2f other) {
		operator.apply(data, 0, DATA_LEN, other.data);
		return this;
	}

	public Mat2x3f transpose() {
		return new Mat2x3f(
				data[0], data[3],
				data[1], data[4],
				data[2], data[5]
		);
	}

	@Override
	public String toString() {
		return "Mat3x2f{"
				+ "\n\t" + data[0] + "\t" + data[1] + "\t" + data[2]
				+ "\n\t" + data[3] + "\t" + data[4] + "\t" + data[5]
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
		return (location, buffer) -> GL45.glUniformMatrix3x2fv(location, true, buffer);
	}
}
