package utils.matrix;

import java.nio.FloatBuffer;
import java.util.function.BiConsumer;
import org.lwjgl.opengl.GL45;
import rendering.shaderdata.IBufferData;
import utils.operator.IOperator;
import utils.vector.Vec2f;
import utils.vector.Vec4f;

/**
 * Matrix with 2 columns and 2 rows
 */
public class Mat2f implements IBufferData {
	public static final int DATA_LEN = 2*2;
	public final float[] data = {
			0f, 0f,
			0f, 0f
	};

	public Mat2f() {
		data[0] = 1f;
		data[3] = 1f;
	}

	public Mat2f(float scalar) {
		data[0] = scalar;
		data[3] = scalar;
	}

	public Mat2f(float c00, float c10,
				 float c01, float c11) {
		data[0] = c00;
		data[1] = c10;
		data[2] = c01;
		data[3] = c11;
	}

	public Mat2f(Vec2f row1, Vec2f row2) {
		System.arraycopy(row1.data, 0, data, 0, 2);
		System.arraycopy(row2.data, 0, data, 2, 2);
	}

	public Mat2f(Vec4f vec4) {
		System.arraycopy(vec4.data, 0, data, 0, DATA_LEN);
	}

	public Mat2f(Mat2f other) {
		System.arraycopy(other.data, 0, data, 0, DATA_LEN);
	}

	public Mat2f copy() {
		return new Mat2f(this);
	}

	public Mat2f set(float c00, float c10,
					 float c01, float c11) {
		data[0] = c00;
		data[1] = c10;
		data[2] = c01;
		data[3] = c11;
		return this;
	}

	public Mat2f apply(IOperator operator, float scalar) {
		Vec4f.apply(operator, data, scalar);
		return this;
	}

	public Mat2f apply(IOperator operator, Mat2f other) {
		Vec4f.apply(operator, data, other.data);
		return this;
	}

	public float determinant() {
		return data[0] * data[3] - data[1] * data[2];
	}

	public Mat2f inverse() {
		float oneOverDet = 1f / determinant();
		return new Mat2f(
				data[3] * oneOverDet,
				-data[1] * oneOverDet,
				-data[2] * oneOverDet,
				data[0] * oneOverDet
		);
	}

	public Mat2f transpose() {
		return new Mat2f(
				data[0], data[2],
				data[1], data[3]
		);
	}

	public Mat2f transposeIP() {
		float temp = data[1];
		data[1] = data[2];
		data[2] = temp;
		return this;
	}

	@Override
	public String toString() {
		return "Mat2f{"
				+ "\n\t" + data[0] + "\t" + data[1]
				+ "\n\t" + data[2] + "\t" + data[3]
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
		return (location, buffer) -> GL45.glUniformMatrix2fv(location, true, buffer);
	}
}
