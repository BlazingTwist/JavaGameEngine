package utils.vector;

import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.lwjgl.opengl.GL45;
import rendering.shaderdata.IBufferData;
import utils.MathF;
import utils.operator.IOperator;
import utils.quaternion.Quaternion;

public class Vec3f implements IBufferData {
	public static Vec3f zero() {
		return new Vec3f(0f);
	}

	public static Vec3f up() {
		return new Vec3f(0f, 1f, 0f);
	}

	public static Vec3f down() {
		return new Vec3f(0f, -1f, 0f);
	}

	public static Vec3f left() {
		return new Vec3f(-1f, 0f, 0f);
	}

	public static Vec3f right() {
		return new Vec3f(1f, 0f, 0f);
	}

	public static Vec3f forward() {
		return new Vec3f(0f, 0f, 1f);
	}

	public static Vec3f backward() {
		return new Vec3f(0f, 0f, -1f);
	}

	public static void apply(IOperator operator, float[] data, float scalar) {
		operator.apply(data, 0, 3, scalar);
	}

	public static void apply(IOperator operator, float[] data, float x, float y, float z) {
		data[0] = operator.apply(data[0], x);
		data[1] = operator.apply(data[1], y);
		data[2] = operator.apply(data[2], z);
	}

	public static void apply(IOperator operator, float[] data, float[] otherData) {
		operator.apply(data, 0, 3, otherData);
	}

	public static final int DATA_LEN = 3;
	public final float[] data = {0f, 0f, 0f};

	public Vec3f() {
	}

	public Vec3f(float scalar) {
		data[0] = scalar;
		data[1] = scalar;
		data[2] = scalar;
	}

	public Vec3f(float x, float y, float z) {
		data[0] = x;
		data[1] = y;
		data[2] = z;
	}

	public Vec3f(Vec2f xy, float z) {
		data[0] = xy.data[0];
		data[1] = xy.data[1];
		data[2] = z;
	}

	public Vec3f(Vec3f other) {
		data[0] = other.data[0];
		data[1] = other.data[1];
		data[2] = other.data[2];
	}

	public Vec3f(Vec4f other) {
		data[0] = other.data[0];
		data[1] = other.data[1];
		data[2] = other.data[2];
	}

	public Vec3f(Quaternion quaternion) {
		System.arraycopy(quaternion.data, 0, data, 0, DATA_LEN);
	}

	public Vec3f set(float x, float y, float z) {
		data[0] = x;
		data[1] = y;
		data[2] = z;
		return this;
	}

	public Vec3f set(float[] arr) {
		System.arraycopy(arr, 0, data, 0, DATA_LEN);
		return this;
	}

	public Vec3f copy() {
		return new Vec3f(this);
	}

	public Vec3f apply(IOperator operator, float scalar) {
		apply(operator, data, scalar);
		return this;
	}

	public Vec3f apply(IOperator operator, float x, float y, float z) {
		apply(operator, data, x, y, z);
		return this;
	}

	public Vec3f apply(IOperator operator, Vec3f other) {
		apply(operator, data, other.data);
		return this;
	}

	public Vec3f apply(Function<Float, Float> function) {
		data[0] = function.apply(data[0]);
		data[1] = function.apply(data[1]);
		data[2] = function.apply(data[2]);
		return this;
	}

	public float dot(float x, float y, float z) {
		return data[0] * x
				+ data[1] * y
				+ data[2] * z;
	}

	public float dot(Vec3f other) {
		return data[0] * other.data[0]
				+ data[1] * other.data[1]
				+ data[2] * other.data[2];
	}

	public float lengthSquared() {
		return data[0] * data[0] + data[1] * data[1] + data[2] * data[2];
	}

	public float length() {
		return (float) Math.sqrt(data[0] * data[0] + data[1] * data[1] + data[2] * data[2]);
	}

	public float distanceSquared(Vec3f other) {
		float a = this.data[0] - other.data[0];
		float b = this.data[1] - other.data[1];
		float c = this.data[2] - other.data[2];
		return a * a + b * b + c * c;
	}

	public float distance(Vec3f other) {
		return MathF.sqrt(distanceSquared(other));
	}

	public Vec3f normalize() {
		float scalar = 1f / length();
		data[0] *= scalar;
		data[1] *= scalar;
		data[2] *= scalar;
		return this;
	}

	public Vec3f cross(Vec3f other) {
		float newX = data[1] * other.data[2] - data[2] * other.data[1];
		float newY = data[2] * other.data[0] - data[0] * other.data[2];
		data[2] = data[0] * other.data[1] - data[1] * other.data[0];
		data[0] = newX;
		data[1] = newY;
		return this;
	}

	public void writeToBuffer(FloatBuffer buffer, float w) {
		buffer.put(data, 0, DATA_LEN);
		buffer.put(w);
	}

	@Override
	public String toString() {
		return "Vec3f{"
				+ "x: " + data[0]
				+ ", y: " + data[1]
				+ ", z: " + data[2]
				+ '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Vec3f vec3f = (Vec3f) o;
		return Arrays.equals(data, vec3f.data);
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
		return GL45::glUniform3fv;
	}
}
