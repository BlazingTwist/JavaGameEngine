package ecs.components;

public class LifeTimeLightIntensity {
	public final float startLightIntensity;
	public final float endLightIntensity;

	/**
	 * @param startLightIntensity intensity at start of lifeTime (lifeTime = initialLifeTime)
	 * @param endLightIntensity   intensity at end of lifeTime   (lifeTime = 0)
	 */
	public LifeTimeLightIntensity(float startLightIntensity, float endLightIntensity) {
		this.startLightIntensity = startLightIntensity;
		this.endLightIntensity = endLightIntensity;
	}
}
