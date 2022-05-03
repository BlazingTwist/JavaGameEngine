package ecs.components;

import gamestate.Time;
import utils.operator.Operator;
import utils.vector.Vec3f;

public class Velocity {
	private final Vec3f velocity;

	public Velocity() {
		velocity = Vec3f.zero();
	}

	public Velocity(Vec3f velocity) {
		this.velocity = velocity;
	}

	public Vec3f getVelocity() {
		return velocity;
	}

	public void setVelocity(Vec3f velocity) {
		this.velocity.set(velocity.data);
	}

	/**
	 * For physical (semi) correctness, apply Velocity FIRST, then update velocity values.
	 */
	public void applyVelocity(Transform transform) {
		transform.setDirty().getPosition().apply(
				Operator.Add,
				velocity.data[0] * Time.physicsDeltaSecondsF,
				velocity.data[1] * Time.physicsDeltaSecondsF,
				velocity.data[2] * Time.physicsDeltaSecondsF
		);
	}

	/**
	 * this method assumes that the direction and magnitude of the acceleration remains constant for a duration of `deltaSeconds`
	 * if that is not your case, update the velocity vector directly instead of using this method.
	 */
	public void applyAccelerationWithPositionUpdate(Vec3f acceleration, Transform transform) {
		transform.setDirty().getPosition().apply(
				Operator.Add,
				acceleration.data[0] * Time.physicsDeltaSecondsSquaredF / 2f,
				acceleration.data[1] * Time.physicsDeltaSecondsSquaredF / 2f,
				acceleration.data[2] * Time.physicsDeltaSecondsSquaredF / 2f
		);
		applyAcceleration(acceleration);
	}

	public void applyAcceleration(Vec3f acceleration) {
		velocity.apply(
				Operator.Add,
				acceleration.data[0] * Time.physicsDeltaSecondsF,
				acceleration.data[1] * Time.physicsDeltaSecondsF,
				acceleration.data[2] * Time.physicsDeltaSecondsF
		);
	}

	public void applyAcceleration(Vec3f acceleration, float scalar) {
		velocity.apply(
				Operator.Add,
				acceleration.data[0] * Time.physicsDeltaSecondsF * scalar,
				acceleration.data[1] * Time.physicsDeltaSecondsF * scalar,
				acceleration.data[2] * Time.physicsDeltaSecondsF * scalar
		);
	}
}
