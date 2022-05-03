package camera.controls;

import camera.BaseCamera;
import ecs.Entity;
import gamestate.Time;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rendering.shaderdata.ShaderDataManager;
import utils.input.InputManager;
import utils.operator.Operator;
import utils.quaternion.Quaternion;
import utils.quaternion.QuaternionMathIP;
import utils.quaternion.QuaternionMathOOP;
import utils.vector.Vec2f;
import utils.vector.Vec3f;

public class FollowCamera {
	private static final Logger logger = LoggerFactory.getLogger(FollowCamera.class);

	private final BaseCamera camera;

	private final Vec3f currentPositionOffset;
	private final Vec3f currentRotationOffset;

	private final Vec3f initialPositionOffset;
	private final Vec3f initialRotationOffset;

	private final int unlockCameraRotationButton;
	private final Vec2f mouseSensitivity;
	private final Vec2f prevCursorPosition;

	private final int translateForwardKey;
	private final int translateBackwardKey;
	private final int translateUpKey;
	private final int translateDownKey;
	private final int translateLeftKey;
	private final int translateRightKey;
	private final Vec3f translateSensitivity;

	private Entity trackedEntity = null;
	private byte lastEntityChangeID = 0;
	private Vec3f lastEntityPosition = Vec3f.zero();
	private Quaternion lastEntityRotation = Quaternion.identity();

	private boolean rotationUnlocked = false;

	public FollowCamera(BaseCamera camera, Vec3f initialPositionOffset, Vec3f initialRotationOffset,
						int unlockCameraRotationButton, Vec2f mouseSensitivity,
						int translateForwardKey, int translateBackwardKey, int translateUpKey, int translateDownKey,
						int translateLeftKey, int translateRightKey, Vec3f translateSensitivity) {
		this.camera = camera;
		this.currentPositionOffset = initialPositionOffset.copy();
		this.currentRotationOffset = initialRotationOffset.copy();
		this.initialPositionOffset = initialPositionOffset;
		this.initialRotationOffset = initialRotationOffset;
		this.unlockCameraRotationButton = unlockCameraRotationButton;
		this.mouseSensitivity = mouseSensitivity;
		this.prevCursorPosition = InputManager.getInstance().getCursorPosition().copy();
		this.translateForwardKey = translateForwardKey;
		this.translateBackwardKey = translateBackwardKey;
		this.translateUpKey = translateUpKey;
		this.translateDownKey = translateDownKey;
		this.translateLeftKey = translateLeftKey;
		this.translateRightKey = translateRightKey;
		this.translateSensitivity = translateSensitivity;
	}

	/**
	 * Initializes controls to some default values
	 *
	 * @param camera                camera to use
	 * @param initialPositionOffset offset from tracked object (or world-root)
	 * @param initialRotationOffset rotation-offset from tracked object (or 0,0,0)
	 */
	public FollowCamera(BaseCamera camera, Vec3f initialPositionOffset, Vec3f initialRotationOffset) {
		this.camera = camera;
		this.currentPositionOffset = initialPositionOffset.copy();
		this.currentRotationOffset = initialRotationOffset.copy();
		this.initialPositionOffset = initialPositionOffset;
		this.initialRotationOffset = initialRotationOffset;
		this.unlockCameraRotationButton = GLFW.GLFW_MOUSE_BUTTON_LEFT;
		this.mouseSensitivity = new Vec2f(2.5f);
		this.prevCursorPosition = InputManager.getInstance().getCursorPosition().copy();
		this.translateForwardKey = GLFW.GLFW_KEY_W;
		this.translateBackwardKey = GLFW.GLFW_KEY_S;
		this.translateUpKey = GLFW.GLFW_KEY_E;
		this.translateDownKey = GLFW.GLFW_KEY_Q;
		this.translateLeftKey = GLFW.GLFW_KEY_A;
		this.translateRightKey = GLFW.GLFW_KEY_D;
		this.translateSensitivity = new Vec3f(10f);
		applyCameraTransform();
	}

	public BaseCamera getCamera() {
		return camera;
	}

	public void trackEntity(Entity entity) {
		trackedEntity = entity;
		if (entity == null) {
			lastEntityChangeID = 0;
			lastEntityPosition = Vec3f.zero();
			lastEntityRotation = Quaternion.identity();
		}
	}

	public void resetCameraOffsets() {
		currentPositionOffset.set(initialPositionOffset.data);
		currentRotationOffset.set(initialRotationOffset.data);
		applyCameraTransform();
	}

	public void update() {
		InputManager inputManager = InputManager.getInstance();
		float deltaSeconds = Time.physicsDeltaSecondsF;
		boolean transformChanged = false;

		boolean unlockRotationPressed = inputManager.getMouseButton(unlockCameraRotationButton);
		if (rotationUnlocked) {
			if (unlockRotationPressed) {
				Vec2f cursorPosition = inputManager.getCursorPosition();
				Vec2f cursorDelta = cursorPosition.copy()
						.apply(Operator.Sub, prevCursorPosition)
						.apply(Operator.Mul, mouseSensitivity.data[0] * deltaSeconds, mouseSensitivity.data[1] * deltaSeconds);
				prevCursorPosition.set(cursorPosition.data);
				currentRotationOffset.apply(Operator.Add, cursorDelta.data[1], cursorDelta.data[0], 0f);
				if (currentRotationOffset.data[0] > 360f) {
					currentRotationOffset.data[0] -= 360f;
				} else if (currentRotationOffset.data[0] < -360f) {
					currentRotationOffset.data[0] += 360f;
				}
				if (currentRotationOffset.data[1] > 360f) {
					currentRotationOffset.data[1] -= 360f;
				} else if (currentRotationOffset.data[1] < -360f) {
					currentRotationOffset.data[1] += 360f;
				}
				transformChanged = true;
			} else {
				rotationUnlocked = false;
			}
		} else if (unlockRotationPressed) {
			rotationUnlocked = true;
			Vec2f cursorPosition = inputManager.getCursorPosition();
			prevCursorPosition.set(cursorPosition.data);
		}

		Vec3f translationInput = new Vec3f();
		if (applyTranslationInput(translationInput)) {
			QuaternionMathIP.rotate(QuaternionMathOOP.eulerDeg(currentRotationOffset), translationInput);
			currentPositionOffset.apply(Operator.Add, translationInput);
			transformChanged = true;
		}

		if (trackedEntity != null) {
			if (trackedEntity.isExpired()) {
				trackEntity(null);
			} else {
				if (trackedEntity.transform == null) {
					logger.error("trackedEntity does not have a transform component!");
					return;
				}
				if(trackedEntity.transform.getChangeID() != lastEntityChangeID){
					lastEntityChangeID = trackedEntity.transform.getChangeID();
					lastEntityPosition = trackedEntity.transform.getPosition();
					lastEntityRotation = trackedEntity.transform.getRotation();
					transformChanged = true;
				}
			}
		}

		if (transformChanged) {
			applyCameraTransform();
		}
	}

	private void applyCameraTransform() {
		Vec3f positionOffset = QuaternionMathOOP.rotate(lastEntityRotation, currentPositionOffset);
		Operator.Add.apply(camera.getPosition().data, 0, lastEntityPosition.data, 0, positionOffset.data, 0, 3);

		Quaternion rotationOffset = QuaternionMathOOP.eulerDeg(currentRotationOffset);
		QuaternionMathIP.rotate(rotationOffset, lastEntityRotation, rotationOffset);
		camera.setRotation(QuaternionMathOOP.toMat3(rotationOffset));

		ShaderDataManager dataManager = ShaderDataManager.getInstance();
		dataManager.camera_position.setData(camera.getPosition());
		dataManager.camera_upVector.setData(camera.getUp());
		dataManager.camera_rightVector.setData(camera.getRight());
		dataManager.camera_worldToCameraMatrix.setData(camera.getWorldToCameraMatrix());
	}

	private boolean applyTranslationInput(Vec3f target) {
		boolean didTranslate = false;
		InputManager inputManager = InputManager.getInstance();
		float deltaSeconds = Time.physicsDeltaSecondsF;
		if (inputManager.getKey(translateForwardKey)) {
			target.data[2] += (translateSensitivity.data[2] * deltaSeconds);
			didTranslate = true;
		}
		if (inputManager.getKey(translateBackwardKey)) {
			target.data[2] -= (translateSensitivity.data[2] * deltaSeconds);
			didTranslate = true;
		}
		if (inputManager.getKey(translateLeftKey)) {
			target.data[0] -= (translateSensitivity.data[0] * deltaSeconds);
			didTranslate = true;
		}
		if (inputManager.getKey(translateRightKey)) {
			target.data[0] += (translateSensitivity.data[0] * deltaSeconds);
			didTranslate = true;
		}
		if (inputManager.getKey(translateUpKey)) {
			target.data[1] += (translateSensitivity.data[1] * deltaSeconds);
			didTranslate = true;
		}
		if (inputManager.getKey(translateDownKey)) {
			target.data[1] -= (translateSensitivity.data[1] * deltaSeconds);
			didTranslate = true;
		}
		return didTranslate;
	}
}
