package ecs.components;

public class LifeTime {
	public final float initialLifeTime;
	public float lifeTime;

	/**
	 * Destroys the Entity it is attached to at the end of its lifeTime
	 *
	 * @param lifeTime time until destruction, in seconds
	 */
	public LifeTime(float lifeTime) {
		initialLifeTime = lifeTime;
		this.lifeTime = lifeTime;
	}
}
