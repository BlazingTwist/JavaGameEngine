package gamestate;

import camera.PerspectiveCamera;
import camera.controls.FollowCamera;
import logging.LogbackLoggerProvider;
import org.lwjgl.opengl.GL45;
import org.slf4j.Logger;
import rendering.programs.MarchingCubesRenderProgram;
import rendering.programs.MeshRenderProgram;
import rendering.programs.PostProcessingProgram;
import rendering.programs.SphereParticleProgram;
import rendering.shaderdata.ShaderDataManager;
import utils.vector.Vec3f;

public abstract class DefaultGameState extends BaseGameState {

	protected static final Logger logger = LogbackLoggerProvider.getLogger(DefaultGameState.class);

	protected final MeshRenderProgram meshRenderProgram;
	protected final SphereParticleProgram particleProgram;
	protected final PostProcessingProgram postProcessingProgram;
	protected final MarchingCubesRenderProgram marchingCubesProgram;
	protected final FollowCamera cameraControls;
	protected final String stateName;
	protected final Vec3f ambientLight;

	public DefaultGameState(String stateName, Vec3f ambientLight, Vec3f cameraPosition,
							boolean meshRenderer, boolean particleRenderer, boolean postProcessing, boolean marchingCubes) {
		this.stateName = stateName;
		this.ambientLight = ambientLight;
		cameraControls = new FollowCamera(
				new PerspectiveCamera(90f, 0.1f, 300f),
				cameraPosition, new Vec3f(0f)
		);
		meshRenderProgram = meshRenderer ? new MeshRenderProgram() : null;
		particleProgram = particleRenderer ? new SphereParticleProgram() : null;
		postProcessingProgram = postProcessing ? new PostProcessingProgram() : null;
		marchingCubesProgram = marchingCubes ? new MarchingCubesRenderProgram() : null;
	}

	private void forceBindPrograms() {
		ShaderDataManager.getInstance().lighting_ambientLight.setData(this.ambientLight);
		if (meshRenderProgram != null) {
			meshRenderProgram.forceBindData();
		}
		if (particleProgram != null) {
			particleProgram.forceBindData();
		}
		if (postProcessingProgram != null) {
			postProcessingProgram.forceBindData();
		}
		if (marchingCubesProgram != null) {
			marchingCubesProgram.forceBindData();
		}
	}

	protected final void init() {
		forceBindPrograms();
		loadEntities();
		initializeScene();
		printControls();
	}

	public void printControls() {
		logger.info("{} Controls:", stateName);
	}

	public abstract void loadEntities();

	public void initializeScene() {
		cameraControls.resetCameraOffsets();
	}

	@Override
	public void draw() {
		boolean postProcessing = postProcessingProgram != null;
		if (postProcessing) {
			postProcessingProgram.bindFrameBuffer();
		}
		if (meshRenderProgram != null) {
			meshRenderProgram.execute();
		}
		if (particleProgram != null) {
			particleProgram.execute();
		}
		if (marchingCubesProgram != null) {
			marchingCubesProgram.execute();
		}
		if (postProcessing) {
			GL45.glBindFramebuffer(GL45.GL_FRAMEBUFFER, 0);
			postProcessingProgram.execute();
		}
	}

	@Override
	public void onPause() {
		logger.info("===== {} State paused =====", stateName);
	}

	@Override
	public void onResume() {
		forceBindPrograms();
		printControls();
	}

	@Override
	public void onExit() {
		if (meshRenderProgram != null) {
			meshRenderProgram.delete();
		}
		if (particleProgram != null) {
			particleProgram.delete();
		}
		if (postProcessingProgram != null) {
			postProcessingProgram.delete();
		}
		if (marchingCubesProgram != null) {
			marchingCubesProgram.delete();
		}
	}
}
