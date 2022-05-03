package utils.vector;

import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.function.BiConsumer;
import org.lwjgl.opengl.GL45;
import rendering.shaderdata.IBufferData;
import utils.operator.IOperator;

public class Vec2f implements IBufferData {
	public static void apply(IOperator operator, float[] data, float scalar) {
		operator.apply(data, 0, 2, scalar);
	}

	public static void apply(IOperator operator, float[] data, float x, float y) {
		data[0] = operator.apply(data[0], x);
		data[1] = operator.apply(data[1], y);
	}

	public static void apply(IOperator operator, float[] data, float[] otherData) {
		operator.apply(data, 0, 2, otherData);
	}

	public static final int DATA_LEN = 2;
	public final float[] data = {0f, 0f};

	public Vec2f() {
	}

	public Vec2f(float scalar) {
		data[0] = scalar;
		data[1] = scalar;
	}

	public Vec2f(float x, float y) {
		data[0] = x;
		data[1] = y;
	}

	public Vec2f(Vec2f other) {
		data[0] = other.data[0];
		data[1] = other.data[1];
	}

	public Vec2f(Vec3f other) {
		data[0] = other.data[0];
		data[1] = other.data[1];
	}

	public Vec2f(Vec4f other) {
		data[0] = other.data[0];
		data[1] = other.data[1];
	}

	public Vec2f set(float x, float y) {
		data[0] = x;
		data[1] = y;
		return this;
	}

	public Vec2f set(float[] arr){
		System.arraycopy(arr, 0, data, 0, DATA_LEN);
		return this;
	}

	public Vec2f copy() {
		return new Vec2f(this);
	}

	public Vec2f apply(IOperator operator, float scalar) {
		apply(operator, data, scalar);
		return this;
	}

	public Vec2f apply(IOperator operator, float x, float y) {
		apply(operator, data, x, y);
		return this;
	}

	public Vec2f apply(IOperator operator, Vec2f other) {
		apply(operator, data, other.data);
		return this;
	}

	public float dot(float x, float y) {
		return data[0] * x
				+ data[1] * y;
	}

	public float dot(Vec2f other) {
		return data[0] * other.data[0]
				+ data[1] * other.data[1];
	}

	public float lengthSquared() {
		return data[0] * data[0] + data[1] * data[1];
	}

	public float length() {
		return (float) Math.sqrt(data[0] * data[0] + data[1] * data[1]);
	}

	public Vec2f normalize() {
		float scalar = 1f / length();
		data[0] *= scalar;
		data[1] *= scalar;
		return this;
	}

	@Override
	public String toString() {
		return "Vec2f{"
				+ "x: " + data[0]
				+ ", y: " + data[1]
				+ '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Vec2f vec2f = (Vec2f) o;
		return Arrays.equals(data, vec2f.data);
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
		return GL45::glUniform2fv;
	}
}
