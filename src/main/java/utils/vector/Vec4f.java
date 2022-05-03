package utils.vector;

import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.function.BiConsumer;
import org.lwjgl.opengl.GL45;
import rendering.shaderdata.IBufferData;
import utils.operator.IOperator;
import utils.quaternion.Quaternion;

public class Vec4f implements IBufferData {
	public static void apply(IOperator operator, float[] data, float scalar) {
		operator.apply(data, 0, 4, scalar);
	}

	public static void apply(IOperator operator, float[] data, float x, float y, float z, float w) {
		data[0] = operator.apply(data[0], x);
		data[1] = operator.apply(data[1], y);
		data[2] = operator.apply(data[2], z);
		data[3] = operator.apply(data[3], w);
	}

	public static void apply(IOperator operator, float[] data, float[] otherData) {
		operator.apply(data, 0, 4, otherData);
	}

	public static final int DATA_LEN = 4;
	public final float[] data = {0f, 0f, 0f, 0f};

	public Vec4f() {
	}

	public Vec4f(float scalar) {
		data[0] = scalar;
		data[1] = scalar;
		data[2] = scalar;
		data[3] = scalar;
	}

	public Vec4f(float x, float y, float z, float w) {
		data[0] = x;
		data[1] = y;
		data[2] = z;
		data[3] = w;
	}

	public Vec4f(Vec2f xy, Vec2f zw) {
		data[0] = xy.data[0];
		data[1] = xy.data[1];
		data[2] = zw.data[0];
		data[3] = zw.data[1];
	}

	public Vec4f(Vec2f xy, float z, float w) {
		data[0] = xy.data[0];
		data[1] = xy.data[1];
		data[2] = z;
		data[3] = w;
	}

	public Vec4f(Vec3f xyz, float w) {
		data[0] = xyz.data[0];
		data[1] = xyz.data[1];
		data[2] = xyz.data[2];
		data[3] = w;
	}

	public Vec4f(Vec4f other) {
		data[0] = other.data[0];
		data[1] = other.data[1];
		data[2] = other.data[2];
		data[3] = other.data[3];
	}

	public Vec4f(Quaternion quaternion) {
		data[0] = quaternion.data[0];
		data[1] = quaternion.data[1];
		data[2] = quaternion.data[2];
		data[3] = quaternion.data[3];
	}

	public Vec4f set(float x, float y, float z, float w) {
		data[0] = x;
		data[1] = y;
		data[2] = z;
		data[3] = w;
		return this;
	}

	public Vec4f set(float[] arr){
		System.arraycopy(arr, 0, data, 0, DATA_LEN);
		return this;
	}

	public Vec4f copy() {
		return new Vec4f(this);
	}

	public Vec4f apply(IOperator operator, float scalar) {
		apply(operator, data, scalar);
		return this;
	}

	public Vec4f apply(IOperator operator, float x, float y, float z, float w) {
		apply(operator, data, x, y, z, w);
		return this;
	}

	public Vec4f apply(IOperator operator, Vec4f other) {
		apply(operator, data, other.data);
		return this;
	}

	public float dot(float x, float y, float z, float w) {
		return data[0] * x
				+ data[1] * y
				+ data[2] * z
				+ data[3] * w;
	}

	public float dot(Vec4f other) {
		return data[0] * other.data[0]
				+ data[1] * other.data[1]
				+ data[2] * other.data[2]
				+ data[3] * other.data[3];
	}

	public float lengthSquared() {
		return data[0] * data[0] + data[1] * data[1] + data[2] * data[2] + data[3] * data[3];
	}

	public float length() {
		return (float) Math.sqrt(data[0] * data[0] + data[1] * data[1] + data[2] * data[2] + data[3] * data[3]);
	}

	public Vec4f normalize() {
		float scalar = 1f / length();
		data[0] *= scalar;
		data[1] *= scalar;
		data[2] *= scalar;
		data[3] *= scalar;
		return this;
	}

	@Override
	public String toString() {
		return "Vec4f{"
				+ "x: " + data[0]
				+ ", y: " + data[1]
				+ ", z: " + data[2]
				+ ", w: " + data[3]
				+ '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Vec4f vec4f = (Vec4f) o;
		return Arrays.equals(data, vec4f.data);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(data);
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
