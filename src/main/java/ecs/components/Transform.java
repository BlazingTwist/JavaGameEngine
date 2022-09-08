package ecs.components;

import utils.matrix.MatrixArithmeticIP;
import utils.matrix.Mat4f;
import utils.matrix.MatrixMath;
import utils.quaternion.Quaternion;
import utils.quaternion.QuaternionMathIP;
import utils.quaternion.QuaternionMathOOP;
import utils.vector.Vec3f;

public class Transform {

	private final Vec3f position;
	private final Quaternion rotation;
	private final Vec3f scale;
	private final Mat4f transformMatrix;
	private final Vec3f forward;

	private boolean transformChanged = false;
	private byte transformChangeID = 0;

	public Transform(Vec3f position, Quaternion rotation, Vec3f scale) {
		this.position = position;
		this.rotation = rotation;
		this.scale = scale;
		transformMatrix = new Mat4f();
		forward = Vec3f.forward();
		setDirty();
	}

	public Transform setDirty() {
		if (!transformChanged) {
			transformChanged = true;
			transformChangeID++;
		}
		return this;
	}

	public byte getChangeID() {
		return transformChangeID;
	}

	public Vec3f getPosition() {
		return position;
	}

	public Transform setPosition(Vec3f position) {
		setDirty();
		this.position.set(position.data);
		return this;
	}

	public Quaternion getRotation() {
		return rotation;
	}

	public Transform setRotation(Quaternion rotation) {
		setDirty();
		this.rotation.set(rotation.data);
		return this;
	}

	public Vec3f getScale() {
		return scale;
	}

	public Transform setScale(Vec3f scale) {
		setDirty();
		this.scale.set(scale.data);
		return this;
	}

	public void checkRecompute() {
		if (transformChanged) {
			MatrixArithmeticIP.mul(transformMatrix,
					MatrixMath.translate(position),
					MatrixArithmeticIP.mul(
							QuaternionMathOOP.toMat4(rotation),
							MatrixMath.scale(scale)
					)
			);
			QuaternionMathIP.rotate(rotation, forward.set(0f, 0f, 1f));
			transformChanged = false;
		}
	}

	public Mat4f getTransformMatrix() {
		return transformMatrix;
	}

	public Vec3f getForward() {
		return forward;
	}
}
