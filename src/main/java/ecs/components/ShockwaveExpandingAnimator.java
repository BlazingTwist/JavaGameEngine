package ecs.components;

import ecs.Entity;
import ecs.EntityRegistry;
import gamestate.Time;

public class ShockwaveExpandingAnimator {
	public final float targetRadius;
	public final float bufferRadius;
	public final float initialThickness;
	public final float initialIntensity;
	public final float radiusStep;

	public ShockwaveExpandingAnimator(float targetRadius, float initialThickness, float initialIntensity, float radiusStep) {
		this.targetRadius = targetRadius;
		this.bufferRadius = targetRadius * 1.5f;
		this.initialThickness = initialThickness;
		this.initialIntensity = initialIntensity;
		this.radiusStep = radiusStep;
	}

	public void apply(Entity entity) {
		Shockwave shockwave = entity.shockwaveComponent;
		float currentRadius = shockwave.getRadius();
		float bufferFactor = squared((bufferRadius - currentRadius) / bufferRadius);
		float targetFactor = squared((targetRadius - currentRadius) / targetRadius);
		shockwave.setRadius(currentRadius + (radiusStep * Time.physicsDeltaSecondsF * bufferFactor));
		shockwave.setIntensity(initialIntensity * bufferFactor);
		shockwave.setThickness(initialThickness * targetFactor);
		if (shockwave.getRadius() > targetRadius) {
			EntityRegistry.getInstance().enqueueEraseEntity(entity);
		}
	}

	private static float squared(float v) {
		return v * v;
	}
}
