package rendering;

import logging.LogbackLoggerProvider;
import org.lwjgl.opengl.GL45;
import org.slf4j.Logger;

public class ShaderProgram {
	private static final Logger logger = LogbackLoggerProvider.getLogger(ShaderProgram.class);

	public final int programID;
	public final String programName;

	public Shader vertexShader = null;
	public Shader geometryShader = null;
	public Shader fragmentShader = null;

	public static ShaderProgram fromBaseDirectory(String basePath,
												  boolean vertexShader, boolean geometryShader, boolean fragmentShader) {
		return new ShaderProgram(basePath,
				vertexShader ? basePath + "/vert.glsl" : null,
				geometryShader ? basePath + "/geom.glsl" : null,
				fragmentShader ? basePath + "/frag.glsl" : null);
	}

	public ShaderProgram(String programName,
						 String vertexShaderPath, String geometryShaderPath, String fragmentShaderPath) {
		programID = GL45.glCreateProgram();
		this.programName = programName;

		if (vertexShaderPath != null) {
			vertexShader = Shader.vertexShader(vertexShaderPath);
			GL45.glAttachShader(programID, vertexShader.shaderID);
		}
		if (geometryShaderPath != null) {
			geometryShader = Shader.geometryShader(geometryShaderPath);
			GL45.glAttachShader(programID, geometryShader.shaderID);
		}
		if (fragmentShaderPath != null) {
			fragmentShader = Shader.fragmentShader(fragmentShaderPath);
			GL45.glAttachShader(programID, fragmentShader.shaderID);
		}

		GL45.glLinkProgram(programID);

		String infoLog = GL45.glGetProgramInfoLog(programID, GL45.glGetProgrami(programID, GL45.GL_INFO_LOG_LENGTH));
		if (infoLog.trim().length() > 0) {
			logger.info(infoLog);
		}else{
			logger.info("Program '{}' linked without logs.", programName);
		}

		if (GL45.glGetProgrami(programID, GL45.GL_LINK_STATUS) == GL45.GL_FALSE) {
			logger.error("Failed to link program {}", programName);
		}else{
			logger.info("Program '{}' linked successfully.", programName);
		}
	}

	public void use() {
		GL45.glUseProgram(programID);
	}

	public int getUniformLocation(String str) {
		return GL45.glGetUniformLocation(programID, str);
	}

	public void delete() {
		if (vertexShader != null) {
			vertexShader.delete();
			vertexShader = null;
		}
		if (geometryShader != null) {
			geometryShader.delete();
			geometryShader = null;
		}
		if (fragmentShader != null) {
			fragmentShader.delete();
			fragmentShader = null;
		}
		GL45.glDeleteProgram(programID);
	}
}
