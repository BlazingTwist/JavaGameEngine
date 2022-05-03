package rendering.programs;

import ecs.systems.ShockwaveManagerSystem;
import java.nio.ByteBuffer;
import org.lwjgl.opengl.GL45;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rendering.GeometryBuffer;
import rendering.ShaderProgram;
import rendering.shaderdata.ShaderData;
import rendering.shaderdata.ShaderDataManager;
import utils.WindowInfo;

public class PostProcessingProgram {

	private static final Logger logger = LoggerFactory.getLogger(PostProcessingProgram.class);

	private final ShaderProgram program = ShaderProgram.fromBaseDirectory("shader/postprocessing", true, false, true);

	private final int frameBufferID;
	private final int textureBufferID;
	private final int renderBufferID;

	private final GeometryBuffer geometryBuffer = new GeometryBuffer(GL45.GL_TRIANGLE_STRIP, 4 * 4, new int[]{2, 2}, false, 0);

	private final int glsl_worldToCameraMatrix;
	private final int glsl_cameraUpVector;
	private final int glsl_cameraRightVector;

	private boolean forceBindData = false;

	public PostProcessingProgram() {
		frameBufferID = GL45.glGenFramebuffers();
		textureBufferID = GL45.glGenTextures();
		glsl_worldToCameraMatrix = program.getUniformLocation("world_to_camera_matrix");
		glsl_cameraUpVector = program.getUniformLocation("camera_up_vector");
		glsl_cameraRightVector = program.getUniformLocation("camera_right_vector");

		GL45.glBindFramebuffer(GL45.GL_FRAMEBUFFER, frameBufferID);
		GL45.glBindTexture(GL45.GL_TEXTURE_2D, textureBufferID);
		int width = WindowInfo.getInstance().getWindowWidth();
		int height = WindowInfo.getInstance().getWindowHeight();
		GL45.glTexImage2D(GL45.GL_TEXTURE_2D, 0, GL45.GL_RGB, width, height, 0, GL45.GL_RGB, GL45.GL_UNSIGNED_BYTE, (ByteBuffer) null);
		GL45.glTexParameteri(GL45.GL_TEXTURE_2D, GL45.GL_TEXTURE_MIN_FILTER, GL45.GL_LINEAR);
		GL45.glTexParameteri(GL45.GL_TEXTURE_2D, GL45.GL_TEXTURE_MAG_FILTER, GL45.GL_LINEAR);
		GL45.glTexParameteri(GL45.GL_TEXTURE_2D, GL45.GL_TEXTURE_WRAP_S, GL45.GL_MIRRORED_REPEAT);
		GL45.glTexParameteri(GL45.GL_TEXTURE_2D, GL45.GL_TEXTURE_WRAP_T, GL45.GL_MIRRORED_REPEAT);
		GL45.glTexParameteri(GL45.GL_TEXTURE_2D, GL45.GL_TEXTURE_WRAP_R, GL45.GL_MIRRORED_REPEAT);
		GL45.glFramebufferTexture2D(GL45.GL_FRAMEBUFFER, GL45.GL_COLOR_ATTACHMENT0, GL45.GL_TEXTURE_2D, textureBufferID, 0);

		renderBufferID = GL45.glGenRenderbuffers();
		GL45.glBindRenderbuffer(GL45.GL_RENDERBUFFER, renderBufferID);
		GL45.glRenderbufferStorage(GL45.GL_RENDERBUFFER, GL45.GL_DEPTH24_STENCIL8, width, height);
		GL45.glFramebufferRenderbuffer(GL45.GL_FRAMEBUFFER, GL45.GL_DEPTH_STENCIL_ATTACHMENT, GL45.GL_RENDERBUFFER, renderBufferID);

		if (GL45.glCheckFramebufferStatus(GL45.GL_FRAMEBUFFER) != GL45.GL_FRAMEBUFFER_COMPLETE) {
			logger.error("PostProcessing Framebuffer is not complete!");
		}

		GL45.glBindFramebuffer(GL45.GL_FRAMEBUFFER, 0);
		GL45.glBindTexture(GL45.GL_TEXTURE_2D, 0);
		GL45.glBindRenderbuffer(GL45.GL_RENDERBUFFER, 0);

		geometryBuffer.setData(new float[]{
				-1f, -1f, 0f, 0f,
				1f, -1f, 1f, 0f,
				-1f, 1f, 0f, 1f,
				1f, 1f, 1f, 1f
		});
	}

	public void forceBindData() {
		forceBindData = true;
	}

	public void bindFrameBuffer() {
		GL45.glBindFramebuffer(GL45.GL_FRAMEBUFFER, frameBufferID);
		GL45.glClear(GL45.GL_COLOR_BUFFER_BIT | GL45.GL_DEPTH_BUFFER_BIT);
	}

	private void bindData(ShaderData<?> data, int glsl_location) {
		if (data.isDirty() || forceBindData) {
			data.loadData(glsl_location);
		}
	}

	public void execute() {
		program.use();

		ShaderDataManager dataManager = ShaderDataManager.getInstance();
		bindData(dataManager.camera_worldToCameraMatrix, glsl_worldToCameraMatrix);
		bindData(dataManager.camera_upVector, glsl_cameraUpVector);
		bindData(dataManager.camera_rightVector, glsl_cameraRightVector);

		GL45.glActiveTexture(GL45.GL_TEXTURE0 + 4);
		GL45.glBindTexture(GL45.GL_TEXTURE_2D, textureBufferID);
		ShockwaveManagerSystem.getInstance().bindShaderData(5);

		geometryBuffer.draw();
		forceBindData = false;
	}

	public void delete() {
		program.delete();
		geometryBuffer.delete();
		GL45.glDeleteTextures(textureBufferID);
		GL45.glDeleteRenderbuffers(renderBufferID);
		GL45.glDeleteFramebuffers(frameBufferID);
	}
}
