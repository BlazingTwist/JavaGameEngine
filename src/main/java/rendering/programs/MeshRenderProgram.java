package rendering.programs;

import ecs.DrawSystem;
import ecs.Entity;
import ecs.EntityRegistry;
import ecs.systems.LightManagerSystem;
import org.lwjgl.opengl.GL45;
import rendering.ShaderProgram;
import rendering.shaderdata.ShaderData;
import rendering.shaderdata.ShaderDataManager;

public class MeshRenderProgram implements DrawSystem {
	private final ShaderProgram program = ShaderProgram.fromBaseDirectory("shader/demo", true, true, true);

	private final int glsl_objectToWorldMatrix;
	private final int glsl_worldToCameraMatrix;
	private final int glsl_cameraPosition;
	private final int glsl_ambientLight;
	private final int glsl_textureFlags;

	private boolean forceBindData = false;

	public MeshRenderProgram() {
		glsl_objectToWorldMatrix = program.getUniformLocation("object_to_world_matrix");
		glsl_worldToCameraMatrix = program.getUniformLocation("world_to_camera_matrix");
		glsl_cameraPosition = program.getUniformLocation("camera_position");
		glsl_ambientLight = program.getUniformLocation("ambient_light");
		glsl_textureFlags = program.getUniformLocation("textureFlags");
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

		LightManagerSystem.getInstance().bindShaderData(4);

		EntityRegistry.getInstance().executeDrawSystem(this);
		forceBindData = false;
	}

	public void delete() {
		program.delete();
	}

	@Override
	public boolean isTargetEntity_drawTick(Entity entity) {
		return entity.transform != null && entity.meshComponent != null && entity.meshComponent.isEnabled();
	}

	@Override
	public void execute_drawTick(Entity entity) {
		entity.meshComponent.bindTextures(0, 1, 2, 3, glsl_textureFlags);
		GL45.glUniformMatrix4fv(glsl_objectToWorldMatrix, true, entity.transform.getTransformMatrix().data);
		entity.meshComponent.getMeshData().getGeometryBuffer().draw();
	}

	@Override
	public void onExecuteDrawDone() {
	}
}
