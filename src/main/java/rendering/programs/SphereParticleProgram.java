package rendering.programs;

import ecs.systems.LightManagerSystem;
import ecs.systems.SphereParticleManagerSystem;
import rendering.ShaderProgram;
import rendering.shaderdata.ShaderData;
import rendering.shaderdata.ShaderDataManager;

public class SphereParticleProgram {
	private final ShaderProgram program = ShaderProgram.fromBaseDirectory("shader/sphereParticle", true, true, true);

	private final int glsl_worldToCameraMatrix;
	private final int glsl_cameraPosition;
	private final int glsl_ambientLight;

	private boolean forceBindData = false;

	public SphereParticleProgram() {
		glsl_worldToCameraMatrix = program.getUniformLocation("world_to_camera_matrix");
		glsl_cameraPosition = program.getUniformLocation("camera_position");
		glsl_ambientLight = program.getUniformLocation("ambient_light");
	}

	public void forceBindData() {
		forceBindData = true;
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
		bindData(dataManager.camera_position, glsl_cameraPosition);
		bindData(dataManager.lighting_ambientLight, glsl_ambientLight);

		LightManagerSystem.getInstance().bindShaderData(0);
		SphereParticleManagerSystem.getInstance().getParticleBuffer().draw();
		forceBindData = false;
	}

	public void delete() {
		program.delete();
	}
}
