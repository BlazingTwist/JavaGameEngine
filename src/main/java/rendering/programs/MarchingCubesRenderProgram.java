package rendering.programs;

import ecs.DrawSystem;
import ecs.Entity;
import ecs.EntityRegistry;
import ecs.systems.LightManagerSystem;
import org.lwjgl.opengl.GL45;
import rendering.ShaderProgram;
import rendering.shaderdata.ShaderData;
import rendering.shaderdata.ShaderDataManager;
import rendering.texture.ITexture;
import rendering.texture.Sampler;
import rendering.texture.ShortDataTexture2D;

public class MarchingCubesRenderProgram implements DrawSystem {
	private final ShaderProgram program = ShaderProgram.fromBaseDirectory("shader/marchingCubes", true, true, true);

	private final ITexture polygonisationTable = ShortDataTexture2D.fromResource("textures/marchingCubes/lookupTetrahedron.png", Sampler.intDataSampler);

	private final int glsl_objectToWorldMatrix;
	private final int glsl_worldToCameraMatrix;
	private final int glsl_cameraPosition;
	private final int glsl_phongData;
	private final int glsl_ambientLight;

	private boolean forceBindData = false;

	public MarchingCubesRenderProgram() {
		glsl_objectToWorldMatrix = program.getUniformLocation("object_to_world_matrix");
		glsl_worldToCameraMatrix = program.getUniformLocation("world_to_camera_matrix");
		glsl_cameraPosition = program.getUniformLocation("camera_position");
		glsl_phongData = program.getUniformLocation("phong_data");
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
		if (polygonisationTable != null) {
			polygonisationTable.bindTexture(2);
		}
		EntityRegistry.getInstance().executeDrawSystem(this);
		forceBindData = false;
	}

	public void delete() {
		program.delete();
		ITexture.deleteTexture(polygonisationTable);
	}

	@Override
	public boolean isTargetEntity_drawTick(Entity entity) {
		return entity.transform != null && entity.marchingCubesMesh != null;
	}

	@Override
	public void execute_drawTick(Entity entity) {
		GL45.glUniform4fv(glsl_phongData, entity.marchingCubesMesh.phongData().data);
		entity.marchingCubesMesh.voxelGrid().bindShaderData(1);
		GL45.glUniformMatrix4fv(glsl_objectToWorldMatrix, true, entity.transform.getTransformMatrix().data);
		entity.marchingCubesMesh.voxelGrid().draw();
	}

	@Override
	public void onExecuteDrawDone() {
	}
}
