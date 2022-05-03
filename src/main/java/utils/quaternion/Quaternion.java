package utils.quaternion;

import java.nio.FloatBuffer;
import java.util.function.BiConsumer;
import org.lwjgl.opengl.GL45;
import rendering.shaderdata.IBufferData;
import utils.operator.IOperator;
import utils.vector.Vec2f;
import utils.vector.Vec3f;
import utils.vector.Vec4f;

public class Quaternion implements IBufferData {
	public static Quaternion identity() {
		return new Quaternion(0f, 0f, 0f, 1f);
	}

	public static final int DATA_LEN = 4;
	public final float[] data = {0f, 0f, 0f, 0f};

	public Quaternion() {
	}

	public Quaternion(float scalar) {
		data[0] = scalar;
		data[1] = scalar;
		data[2] = scalar;
		data[3] = scalar;
	}

	public Quaternion(float x, float y, float z, float w) {
		data[0] = x;
		data[1] = y;
		data[2] = z;
		data[3] = w;
	}

	public Quaternion(Vec2f xy, Vec2f zw) {
		data[0] = xy.data[0];
		data[1] = xy.data[1];
		data[2] = zw.data[0];
		data[3] = zw.data[1];
	}

	public Quaternion(Vec2f xy, float z, float w) {
		data[0] = xy.data[0];
		data[1] = xy.data[1];
		data[2] = z;
		data[3] = w;
	}

	public Quaternion(Vec3f xyz, float w) {
		data[0] = xyz.data[0];
		data[1] = xyz.data[1];
		data[2] = xyz.data[2];
		data[3] = w;
	}

	public Quaternion(Vec4f xyzw) {
		data[0] = xyzw.data[0];
		data[1] = xyzw.data[1];
		data[2] = xyzw.data[2];
		data[3] = xyzw.data[3];
	}

	public Quaternion(Quaternion other) {
		data[0] = other.data[0];
		data[1] = other.data[1];
		data[2] = other.data[2];
		data[3] = other.data[3];
	}

	public Quaternion set(float x, float y, float z, float w) {
		data[0] = x;
		data[1] = y;
		data[2] = z;
		data[3] = w;
		return this;
	}

	public Quaternion set(float[] arr) {
		System.arraycopy(arr, 0, data, 0, DATA_LEN);
		return this;
	}

	public Quaternion copy() {
		return new Quaternion(this);
	}

	public Quaternion normalize() {
		float magnitude = (float) Math.sqrt(data[0] * data[0] + data[1] * data[1] + data[2] * data[2] + data[3] * data[3]);
		for (int i = 0; i < DATA_LEN; i++) {
			data[i] /= magnitude;
		}
		return this;
	}

	public Quaternion apply(IOperator operator, float scalar) {
		data[0] = operator.apply(data[0], scalar);
		data[1] = operator.apply(data[1], scalar);
		data[2] = operator.apply(data[2], scalar);
		data[3] = operator.apply(data[3], scalar);
		return this;
	}

	public Quaternion apply(IOperator operator, float x, float y, float z, float w) {
		data[0] = operator.apply(data[0], x);
		data[1] = operator.apply(data[1], y);
		data[2] = operator.apply(data[2], z);
		data[3] = operator.apply(data[3], w);
		return this;
	}

	public Quaternion apply(IOperator operator, Quaternion other) {
		data[0] = operator.apply(data[0], other.data[0]);
		data[1] = operator.apply(data[1], other.data[1]);
		data[2] = operator.apply(data[2], other.data[2]);
		data[3] = operator.apply(data[3], other.data[3]);
		return this;
	}

	@Override
	public String toString() {
		return "Quaternion{"
				+ "x: " + data[0]
				+ ", y: " + data[1]
				+ ", z: " + data[2]
				+ ", w: " + data[3]
				+ '}';
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
		return GL45::glUniform4fv;
	}
}
