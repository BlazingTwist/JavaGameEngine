package ecs.components;

import java.nio.FloatBuffer;
import utils.vector.Vec4f;

public class SphereParticle {
	public static final int BYTES = Float.BYTES * (3 + 4 + 4 + 1);

	public int particleManagerIndex = -1;
	public boolean particleDataChanged = false;
	public byte trackedTransformChangeID = 0;

	private final Vec4f colorInner;
	private final Vec4f colorOuter;

	private float radius = 1.0f;

	public SphereParticle setDirty() {
		particleDataChanged = true;
		return this;
	}

	public Vec4f getColorInner() {
		return colorInner;
	}

	public SphereParticle setColorInner(Vec4f colorInner) {
		setDirty();
		this.colorInner.set(colorInner.data);
		return this;
	}

	public Vec4f getColorOuter() {
		return colorOuter;
	}

	public SphereParticle setColorOuter(Vec4f colorOuter) {
		setDirty();
		this.colorOuter.set(colorOuter.data);
		return this;
	}

	public float getRadius() {
		return radius;
	}

	public SphereParticle setRadius(float radius) {
		setDirty();
		this.radius = radius;
		return this;
	}

	public SphereParticle(float radius, Vec4f colorInner, Vec4f colorOuter) {
		this.radius = radius;
		this.colorInner = colorInner;
		this.colorOuter = colorOuter;
	}

	public SphereParticle() {
		colorInner = new Vec4f(0f, 0f, 0f, 1f);
		colorOuter = new Vec4f(0f, 0f, 0f, 1f);
	}

	public void writeToBuffer(FloatBuffer buffer, Transform transform){
		transform.getPosition().writeToBuffer(buffer, radius);
		colorInner.writeToBuffer(buffer);
		colorOuter.writeToBuffer(buffer);
	}
}
