package camera;

import utils.WindowInfo;
import utils.matrix.MatrixMath;

public class PerspectiveCamera extends BaseCamera {
	private float fov;
	private float nearClipPlane;
	private float farClipPlane;

	public PerspectiveCamera(float fov, float nearClipPlane, float farClipPlane) {
		super(MatrixMath.perspective(fov, WindowInfo.getInstance().getAspectRatio(), nearClipPlane, farClipPlane));
		this.fov = fov;
		this.nearClipPlane = nearClipPlane;
		this.farClipPlane = farClipPlane;
	}

	public float getFov() {
		return fov;
	}

	public PerspectiveCamera setFov(float fov, boolean skipUpdate) {
		this.fov = fov;
		if (!skipUpdate) {
			updateMatrix();
		}
		return this;
	}

	public float getNearClipPlane() {
		return nearClipPlane;
	}

	public PerspectiveCamera setNearClipPlane(float nearClipPlane, boolean skipUpdate) {
		this.nearClipPlane = nearClipPlane;
		if (!skipUpdate) {
			updateMatrix();
		}
		return this;
	}

	public float getFarClipPlane() {
		return farClipPlane;
	}

	public PerspectiveCamera setFarClipPlane(float farClipPlane, boolean skipUpdate) {
		this.farClipPlane = farClipPlane;
		if (!skipUpdate) {
			updateMatrix();
		}
		return this;
	}

	private void updateMatrix() {
		float aspect = WindowInfo.getInstance().getAspectRatio();
		MatrixMath.perspective(projectionMatrix, fov, aspect, nearClipPlane, farClipPlane);
		this.updateTransform();
	}
}
