package logging;

import java.util.Map;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWErrorCallbackI;
import org.lwjgl.system.APIUtil;
import org.slf4j.Logger;

public class GLFWErrorLogback implements GLFWErrorCallbackI {
	private static final Logger logger = LogbackLoggerProvider.getLogger(GLFWErrorLogback.class);

	private final Map<Integer, String> ERROR_CODES;

	public GLFWErrorLogback() {
		ERROR_CODES = APIUtil.apiClassTokens((field, value) -> 65536 < value && value < 131072, null, GLFW.class);
	}

	@Override
	public void invoke(int error, long description) {
		if (!logger.isErrorEnabled()) {
			return;
		}

		StringBuilder messageBuilder = new StringBuilder(ERROR_CODES.get(error))
				.append(" error\n\tDescription: ")
				.append(GLFWErrorCallback.getDescription(description))
				.append("\n\tStacktrace:\n");
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();

		for (int i = 4; i < stack.length; i++) {
			messageBuilder.append("\t\t").append(stack[i]);
		}

		logger.error(messageBuilder.toString());
	}
}
