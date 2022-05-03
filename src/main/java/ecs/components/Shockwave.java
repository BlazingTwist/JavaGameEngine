package ecs.components;

import java.nio.FloatBuffer;

public class Shockwave {
	public static final int BYTES = Float.BYTES * (1 + 1 + 2 /* alignment */ + 4);

	public int shockwaveManagerIndex = -1;
	public boolean shockwaveDataChanged = false;
	public byte trackedTransformChangeID = 0;

	private float radius;
	private float intensity;
	private float thickness;

	public Shockwave setDirty() {
		shockwaveDataChanged = true;
		return this;
	}

	public float getRadius() {
		return radius;
	}

	public Shockwave setRadius(float radius) {
		setDirty();
		this.radius = radius;
		return this;
	}

	public float getIntensity() {
		return intensity;
	}

	public Shockwave setIntensity(float intensity) {
		setDirty();
		this.intensity = intensity;
		return this;
	}

	public float getThickness() {
		return thickness;
	}

	public Shockwave setThickness(float thickness) {
		setDirty();
		this.thickness = thickness;
		return this;
	}

	public Shockwave(float radius, float intensity, float thickness) {
		this.radius = radius;
		this.intensity = intensity;
		this.thickness = thickness;
	}

	public void writeToBuffer(FloatBuffer buffer, Transform transform){
		buffer.put(radius);
		buffer.put(intensity);
		buffer.put(thickness);
		buffer.put(0f);
		transform.getPosition().writeToBuffer(buffer, 0f);
	}
}
