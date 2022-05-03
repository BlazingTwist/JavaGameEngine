package utils;

import logging.LogbackLoggerProvider;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

public class WindowInfo {
	private static final Logger logger = LogbackLoggerProvider.getLogger(WindowInfo.class);

	private static WindowInfo instance;

	public static WindowInfo getInstance() {
		if (instance == null) {
			instance = new WindowInfo();
		}
		return instance;
	}

	private long windowPtr;
	private int windowWidth;
	private int windowHeight;

	public int getWindowWidth() {
		return windowWidth;
	}

	public int getWindowHeight() {
		return windowHeight;
	}

	private WindowInfo() {
	}

	public long getWindowPtr() {
		return windowPtr;
	}

	public WindowInfo setWindowPtr(long windowPtr) {
		this.windowPtr = windowPtr;

		int[] widthPtr = new int[]{-1};
		int[] heightPtr = new int[]{-1};
		GLFW.glfwGetFramebufferSize(windowPtr, widthPtr, heightPtr);
		windowWidth = widthPtr[0];
		windowHeight = heightPtr[0];
		if (windowWidth < 0 || windowHeight < 0) {
			logger.error("glfwGetFramebufferSize failed! received width: {} | height: {}", windowWidth, windowHeight);
		}

		return this;
	}

	public float getAspectRatio() {
		if (windowWidth < 0 || windowHeight < 0) {
			return 1f;
		}
		return ((float) windowWidth) / ((float) windowHeight);
	}
}
