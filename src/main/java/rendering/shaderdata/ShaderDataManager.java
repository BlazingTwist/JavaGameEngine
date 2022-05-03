package rendering.shaderdata;

import utils.matrix.Mat4f;
import utils.vector.Vec3f;

public class ShaderDataManager {
	private static ShaderDataManager instance;

	public static ShaderDataManager getInstance() {
		if (instance == null) {
			instance = new ShaderDataManager();
		}
		return instance;
	}

	public final ShaderData<Vec3f> lighting_ambientLight = new ShaderData<>(Vec3f.zero());
	public final ShaderData<Mat4f> camera_worldToCameraMatrix = new ShaderData<>(new Mat4f());
	public final ShaderData<Vec3f> camera_rightVector = new ShaderData<>(Vec3f.right());
	public final ShaderData<Vec3f> camera_upVector = new ShaderData<>(Vec3f.up());
	public final ShaderData<Vec3f> camera_position = new ShaderData<>(Vec3f.zero());

	private ShaderDataManager() {
	}

	public void setNotDirty(){
		lighting_ambientLight.setDirty(false);
		camera_worldToCameraMatrix.setDirty(false);
		camera_rightVector.setDirty(false);
		camera_upVector.setDirty(false);
		camera_position.setDirty(false);
	}
}
