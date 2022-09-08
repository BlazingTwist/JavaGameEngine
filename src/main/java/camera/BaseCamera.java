package camera;

import utils.matrix.MatrixArithmeticIP;
import utils.matrix.MatrixArithmeticOOP;
import utils.matrix.Mat3f;
import utils.matrix.Mat4f;
import utils.matrix.MatrixMath;
import utils.operator.Operator;
import utils.vector.Vec3f;

public abstract class BaseCamera {
	protected final Mat4f projectionMatrix;
	protected final Vec3f position = new Vec3f(0f);
	protected final Vec3f forward = new Vec3f(0f, 0f, 1f);
	protected final Vec3f right = new Vec3f(1f, 0f, 0f);
	protected final Vec3f up = new Vec3f(0f, 1f, 0f);
	protected final Mat4f lookAtMatrix;
	protected final Mat4f worldToCameraMatrix;

	public BaseCamera(Mat4f projectionMatrix) {
		this.projectionMatrix = projectionMatrix;
		this.lookAtMatrix = MatrixMath.lookAt(position, forward, up);
		this.worldToCameraMatrix = MatrixArithmeticOOP.mul(projectionMatrix, lookAtMatrix);
	}

	public Vec3f getPosition() {
		return position;
	}

	public Vec3f getForward() {
		return forward;
	}

	public Vec3f getRight() {
		return right;
	}

	public Vec3f getUp() {
		return up;
	}

	public Mat4f getWorldToCameraMatrix() {
		return worldToCameraMatrix;
	}

	public void moveRelative(float right, float up, float forward) {
		if (right == 0f && up == 0f && forward == 0f) {
			return;
		}

		if (right != 0f) {
			this.position.apply(Operator.Add, this.right.copy().apply(Operator.Mul, right));
		}
		if (up != 0f) {
			this.position.apply(Operator.Add, this.up.copy().apply(Operator.Mul, up));
		}
		if (forward != 0f) {
			this.position.apply(Operator.Add, this.forward.copy().apply(Operator.Mul, forward));
		}
		updateTransform();
	}

	public void moveAbsolute(Vec3f direction) {
		this.position.apply(Operator.Add, direction);
		updateTransform();
	}

	public void moveAbsolute(float x, float y, float z) {
		this.position.apply(Operator.Add, x, y, z);
		updateTransform();
	}

	public void setRotation(float yawAngle, float pitchAngle, float rollAngle) {
		right.set(1f, 0f, 0f);
		up.set(0f, 1f, 0f);
		forward.set(0f, 0f, 1f);
		applyRotation(yawAngle, pitchAngle, rollAngle);
		updateTransform();
	}

	public void setRotation(Mat3f rotationMatrix) {
		MatrixArithmeticIP.mul(rotationMatrix, right.set(1f, 0f, 0f));
		MatrixArithmeticIP.mul(rotationMatrix, up.set(0f, 1f, 0f));
		MatrixArithmeticIP.mul(rotationMatrix, forward.set(0f, 0f, 1f));
		updateTransform();
	}

	public void rotate(float yawAngle, float pitchAngle, float rollAngle) {
		applyRotation(yawAngle, pitchAngle, rollAngle);
		updateTransform();
	}

	public void rotate(Mat3f rotationMatrix) {
		MatrixArithmeticIP.mul(rotationMatrix, right);
		MatrixArithmeticIP.mul(rotationMatrix, up);
		MatrixArithmeticIP.mul(rotationMatrix, forward);
		updateTransform();
	}

	/**
	 * If you've been a naughty boy and changed the position by editing the return value of 'getPosition',
	 * you can use this to force an update of the matrices.
	 */
	public void updateTransform() {
		MatrixMath.lookAt(lookAtMatrix, position, forward, up);
		MatrixArithmeticIP.mul(worldToCameraMatrix, projectionMatrix, lookAtMatrix);
	}

	public Mat4f getLookAtMatrix() {
		return lookAtMatrix;
	}

	public Mat4f getProjectionMatrix() {
		return projectionMatrix;
	}

	private void applyRotation(float yawAngle, float pitchAngle, float rollAngle) {
		if (yawAngle == 0f && pitchAngle == 0f && rollAngle == 0f) {
			return;
		}

		if (yawAngle != 0f) {
			Mat3f yawRotationMatrix = MatrixMath.rotate((float) Math.toRadians(yawAngle), up);
			MatrixArithmeticIP.mul(yawRotationMatrix, right);
			MatrixArithmeticIP.mul(yawRotationMatrix, forward);
		}
		if (pitchAngle != 0f) {
			Mat3f pitchRotationMatrix = MatrixMath.rotate((float) Math.toRadians(pitchAngle), right);
			MatrixArithmeticIP.mul(pitchRotationMatrix, forward);
			MatrixArithmeticIP.mul(pitchRotationMatrix, up);
		}
		if (rollAngle != 0f) {
			Mat3f rollRotationMatrix = MatrixMath.rotate((float) Math.toRadians(rollAngle), forward);
			MatrixArithmeticIP.mul(rollRotationMatrix, right);
			MatrixArithmeticIP.mul(rollRotationMatrix, up);
		}
	}
}
