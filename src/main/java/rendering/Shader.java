package rendering;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import logging.LogbackLoggerProvider;
import org.lwjgl.opengl.GL45;
import org.slf4j.Logger;

public class Shader {
	private static final Logger logger = LogbackLoggerProvider.getLogger(Shader.class);

	public static Shader fragmentShader(String resourcePath) {
		return new Shader(resourcePath, GL45.GL_FRAGMENT_SHADER);
	}

	public static Shader vertexShader(String resourcePath) {
		return new Shader(resourcePath, GL45.GL_VERTEX_SHADER);
	}

	public static Shader geometryShader(String resourcePath) {
		return new Shader(resourcePath, GL45.GL_GEOMETRY_SHADER);
	}

	public static Shader computeShader(String resourcePath) {
		return new Shader(resourcePath, GL45.GL_COMPUTE_SHADER);
	}

	public final int shaderID;

	public Shader(String resourcePath, int type) {
		String shaderCode = "";
		try {
			InputStream resourceAsStream = Shader.class.getClassLoader().getResourceAsStream(resourcePath);
			if (resourceAsStream == null) {
				throw new FileNotFoundException("Resource-Stream was null.");
			}
			shaderCode = new String(resourceAsStream.readAllBytes(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			logger.error("Failed to load file: {}", resourcePath, e);
		}

		shaderID = GL45.glCreateShader(type);
		GL45.glShaderSource(shaderID, shaderCode);
		GL45.glCompileShader(shaderID);

		String infoLog = GL45.glGetShaderInfoLog(shaderID, GL45.glGetShaderi(shaderID, GL45.GL_INFO_LOG_LENGTH));
		if (infoLog.trim().length() > 0) {
			logger.info(infoLog);
		}

		if (GL45.glGetShaderi(shaderID, GL45.GL_COMPILE_STATUS) == GL45.GL_FALSE) {
			logger.error("Failed to compile shader from file: {}", resourcePath);
		}
	}

	public void delete() {
		GL45.glDeleteShader(shaderID);
	}
}
