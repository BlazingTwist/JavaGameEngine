package utils.input;

import java.util.ArrayList;
import logging.LogbackLoggerProvider;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import utils.WindowInfo;
import utils.vector.Vec2f;

public class InputManager {
	private static final Logger logger = LogbackLoggerProvider.getLogger(InputManager.class);

	private static InputManager instance;

	public static InputManager getInstance() {
		if (instance == null) {
			instance = new InputManager();
		}
		return instance;
	}

	private final ArrayList<Integer> currentFrameKeysDown = new ArrayList<>();
	private final ArrayList<Integer> currentFrameKeysUp = new ArrayList<>();
	private final ArrayList<Integer> currentFrameKeysRepeat = new ArrayList<>();

	private final ArrayList<Integer> currentFrameButtonsDown = new ArrayList<>();
	private final ArrayList<Integer> currentFrameButtonsUp = new ArrayList<>();

	private final Vec2f cursorPosition = new Vec2f(0f);

	private InputManager() {
	}

	public void reset() {
		currentFrameKeysDown.clear();
		currentFrameKeysUp.clear();
		currentFrameKeysRepeat.clear();

		currentFrameButtonsDown.clear();
		currentFrameButtonsUp.clear();
	}

	public void keyCallback(long window, int key, int scancode, int action, int mods) {
		switch (action) {
			case GLFW.GLFW_RELEASE -> currentFrameKeysUp.add(key);
			case GLFW.GLFW_PRESS -> currentFrameKeysDown.add(key);
			case GLFW.GLFW_REPEAT -> currentFrameKeysRepeat.add(key);
		}
	}

	public void mouseButtonCallback(long window, int button, int action, int mods) {
		switch (action) {
			case GLFW.GLFW_RELEASE -> currentFrameButtonsUp.add(button);
			case GLFW.GLFW_PRESS -> currentFrameButtonsDown.add(button);
		}
	}

	public void cursorPositionCallback(long window, double xPos, double yPos) {
		cursorPosition.set((float) xPos, (float) yPos);
	}

	/**
	 * @return true if key was pressed this frame
	 */
	public boolean getKeyDown(int key) {
		return currentFrameKeysDown.contains(key);
	}

	/**
	 * @return true if key was released this frame
	 */
	public boolean getKeyUp(int key) {
		return currentFrameKeysUp.contains(key);
	}

	/**
	 * @return true if key repeat event was triggered this frame
	 */
	public boolean getKeyRepeat(int key) {
		return currentFrameKeysRepeat.contains(key);
	}

	public boolean getKey(int key) {
		return GLFW.glfwGetKey(WindowInfo.getInstance().getWindowPtr(), key) == GLFW.GLFW_TRUE;
	}

	/**
	 * @return true if mouse button was pressed this frame
	 */
	public boolean getMouseButtonDown(int button) {
		return currentFrameButtonsDown.contains(button);
	}

	/**
	 * @return true if mouse button was released this frame
	 */
	public boolean getMouseButtonUp(int button) {
		return currentFrameButtonsUp.contains(button);
	}

	public boolean getMouseButton(int button) {
		return GLFW.glfwGetMouseButton(WindowInfo.getInstance().getWindowPtr(), button) == GLFW.GLFW_TRUE;
	}

	public Vec2f getCursorPosition() {
		return cursorPosition;
	}
}
