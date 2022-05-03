package camera;

import utils.matrix.MatrixMath;
import utils.vector.Vec2f;

public class OrthographicCamera extends BaseCamera {
	private final Vec2f orthographicSize;
	private final Vec2f origin;

	private float nearClipPlane;
	private float farClipPlane;

	public OrthographicCamera(Vec2f orthographicSize, Vec2f origin, float nearClipPlane, float farClipPlane) {
		super(MatrixMath.orthographic(
				-orthographicSize.data[0] * origin.data[0], orthographicSize.data[0] * (1f - origin.data[0]),
				-orthographicSize.data[1] * origin.data[1], orthographicSize.data[1] * (1f - origin.data[1]),
				nearClipPlane, farClipPlane
		));
		this.orthographicSize = orthographicSize;
		this.origin = origin;
		this.nearClipPlane = nearClipPlane;
		this.farClipPlane = farClipPlane;
		this.moveAbsolute(0f, 0f, nearClipPlane);
	}

	public Vec2f getOrthographicSize() {
		return orthographicSize;
	}

	public OrthographicCamera setOrthographicSize(float x, float y, boolean skipUpdate) {
		orthographicSize.set(x, y);
		if (!skipUpdate) {
			updateMatrix();
		}
		return this;
	}

	public Vec2f getOrigin() {
		return origin;
	}

	public OrthographicCamera setOrigin(float x, float y, boolean skipUpdate) {
		origin.set(x, y);
		if (!skipUpdate) {
			updateMatrix();
		}
		return this;
	}

	public float getNearClipPlane() {
		return nearClipPlane;
	}

	public OrthographicCamera setNearClipPlane(float nearClipPlane, boolean skipUpdate) {
		this.nearClipPlane = nearClipPlane;
		if (!skipUpdate) {
			updateMatrix();
		}
		return this;
	}

	public float getFarClipPlane() {
		return farClipPlane;
	}

	public OrthographicCamera setFarClipPlane(float farClipPlane, boolean skipUpdate) {
		this.farClipPlane = farClipPlane;
		if (!skipUpdate) {
			updateMatrix();
		}
		return this;
	}

	private void updateMatrix() {
		MatrixMath.orthographic(
				projectionMatrix,
				-orthographicSize.data[0] * origin.data[0], orthographicSize.data[0] * (1f - origin.data[0]),
				-orthographicSize.data[1] * origin.data[1], orthographicSize.data[1] * (1f - origin.data[1]),
				nearClipPlane, farClipPlane
		);
		this.updateTransform();
	}
}
