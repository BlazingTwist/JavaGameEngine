package ecs.components;

import java.nio.FloatBuffer;
import rendering.lighting.LightType;
import utils.vector.Vec3f;

public class Light {
	public static final int BYTES = Float.BYTES * (1 + 1 + 1 + 1 + 4 + 4 + 4);

	public static Light directional(Vec3f color, float intensity) {
		return new Light(LightType.directional, 0f, 0f, color, intensity);
	}

	public static Light spot(float range, float spotAngle, Vec3f color, float intensity) {
		return new Light(LightType.spot, range, spotAngle, color, intensity);
	}

	public static Light point(float range, Vec3f color, float intensity) {
		return new Light(LightType.point, range, 0f, color, intensity);
	}

	public int lightManagerIndex = -1;
	public boolean lightDataChanged;
	public byte trackedTransformChangeID = 0;

	private final Vec3f color;

	private LightType lightType;
	private float range;
	private float spotAngle;
	private float spotAngleCosine;
	private float intensity;

	public Light(LightType lightType, float range, float spotAngle, Vec3f color, float intensity) {
		lightDataChanged = true;
		this.lightType = lightType;
		this.range = range;
		this.spotAngle = spotAngle;
		this.spotAngleCosine = (float) Math.cos(Math.toRadians(spotAngle));
		this.color = color;
		this.intensity = intensity;
	}

	public Light setDirty() {
		lightDataChanged = true;
		return this;
	}

	public LightType getLightType() {
		return lightType;
	}

	public Light setDirectional(Vec3f color, float intensity) {
		lightDataChanged = true;
		this.lightType = LightType.directional;
		this.color.set(color.data);
		this.intensity = intensity;
		return this;
	}

	public Light setSpot(float range, float spotAngle, Vec3f color, float intensity) {
		lightDataChanged = true;
		this.lightType = LightType.spot;
		this.range = range;
		this.spotAngle = spotAngle;
		this.spotAngleCosine = (float) Math.cos(Math.toRadians(spotAngle));
		this.color.set(color.data);
		this.intensity = intensity;
		return this;
	}

	public Light setPoint(float range, Vec3f color, float intensity) {
		lightDataChanged = true;
		this.lightType = LightType.point;
		this.range = range;
		this.color.set(color.data);
		this.intensity = intensity;
		return this;
	}

	public float getRange() {
		return range;
	}

	public Light setRange(float range) {
		lightDataChanged = true;
		this.range = range;
		return this;
	}

	public float getSpotAngle() {
		return spotAngle;
	}

	public Light setSpotAngle(float spotAngle) {
		lightDataChanged = true;
		this.spotAngle = spotAngle;
		this.spotAngleCosine = (float) Math.cos(Math.toRadians(spotAngle));
		return this;
	}

	public Vec3f getColor() {
		return color;
	}

	public Light setColor(Vec3f color) {
		lightDataChanged = true;
		this.color.set(color.data);
		return this;
	}

	public float getIntensity() {
		return intensity;
	}

	public Light setIntensity(float intensity) {
		lightDataChanged = true;
		this.intensity = intensity;
		return this;
	}

	public void writeToBuffer(FloatBuffer buffer, Transform transform) {
		buffer.put(Float.intBitsToFloat(lightType.glsl_type));
		buffer.put(range);
		buffer.put(spotAngleCosine);
		buffer.put(intensity);
		transform.getPosition().writeToBuffer(buffer, 0f);
		transform.getForward().writeToBuffer(buffer, 0f);
		color.writeToBuffer(buffer, 0f);
	}
}
