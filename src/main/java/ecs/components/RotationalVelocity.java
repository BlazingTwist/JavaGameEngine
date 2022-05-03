package ecs.components;

import gamestate.Time;
import utils.operator.Operator;
import utils.quaternion.QuaternionMathIP;
import utils.quaternion.QuaternionMathOOP;
import utils.vector.Vec3f;

public class RotationalVelocity {
	public final Vec3f eulerAngleVelocity;

	/**
	 * @param eulerAngleVelocity euler angles in radians per second
	 */
	public RotationalVelocity(Vec3f eulerAngleVelocity) {
		this.eulerAngleVelocity = eulerAngleVelocity;
	}

	public void applyRotation(Transform transform) {
		QuaternionMathIP.rotate(
				transform.getRotation(),
				transform.getRotation(),
				QuaternionMathOOP.eulerRad(eulerAngleVelocity.copy().apply(Operator.Mul, Time.physicsDeltaSecondsF))
		);
		//transform.setDirty();
	}
}
