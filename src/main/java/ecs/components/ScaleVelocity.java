package ecs.components;

import gamestate.Time;
import utils.vector.Vec3f;

public class ScaleVelocity {
	public final Vec3f velocity;

	public ScaleVelocity(Vec3f velocity) {
		this.velocity = velocity;
	}

	public void applyScale(Transform transform) {
		float[] scaleData = transform.getScale().data;
		for (int i = 0; i < Vec3f.DATA_LEN; i++) {
			scaleData[i] += velocity.data[i] * Time.physicsDeltaSecondsF;
		}
		transform.setDirty();
	}
}
