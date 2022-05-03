package gamestate.states;

import ecs.Entity;
import ecs.EntityRegistry;
import ecs.components.Light;
import ecs.components.Mesh;
import ecs.components.Transform;
import gamestate.DefaultGameState;
import org.lwjgl.glfw.GLFW;
import rendering.mesh.MeshData;
import rendering.texture.ITexture;
import rendering.texture.Sampler;
import rendering.texture.Texture2D;
import utils.input.InputManager;
import utils.quaternion.Quaternion;
import utils.quaternion.QuaternionMathIP;
import utils.quaternion.QuaternionMathOOP;
import utils.vector.Vec3f;

public class ParallaxOcclusionDemo extends DefaultGameState {

	private final MeshData xyPlaneMeshData = MeshData.loadFromFile("models/xyplane.obj", false);
	private final Texture2D woodTexture = Texture2D.fromResource("textures/woodlogwall/texture.png", Sampler.linearMirroredSampler);
	private final Texture2D woodPhong = Texture2D.fromResource("textures/woodlogwall/phong.png", Sampler.linearMirroredSampler);
	private final Texture2D woodNormal = Texture2D.fromResource("textures/woodlogwall/normal.png", Sampler.linearMirroredSampler);
	private final Texture2D woodHeightMap = Texture2D.fromResource("textures/woodlogwall/heightmap.png", Sampler.linearMirroredSampler);

	private Entity xyPlaneEntity = null;
	private Entity lightSource = null;

	public ParallaxOcclusionDemo() {
		super("Parallax Occlusion Demo", new Vec3f(1.4f, 1.4f, 1.4f), new Vec3f(0f, 0f, -3f),
				true, false, false, false);
		super.init();
	}

	@Override
	public void update() {
		Mesh xyPlaneMeshComp = xyPlaneEntity.meshComponent;
		if(InputManager.getInstance().getKeyDown(GLFW.GLFW_KEY_1)){
			xyPlaneMeshComp.setTextureData(xyPlaneMeshComp.getTextureData() == null ? woodTexture : null);
		}
		if(InputManager.getInstance().getKeyDown(GLFW.GLFW_KEY_2)){
			xyPlaneMeshComp.setPhongData(xyPlaneMeshComp.getPhongData() == null ? woodPhong : null);
		}
		if(InputManager.getInstance().getKeyDown(GLFW.GLFW_KEY_3)){
			xyPlaneMeshComp.setNormalData(xyPlaneMeshComp.getNormalData() == null ? woodNormal : null);
		}
		if(InputManager.getInstance().getKeyDown(GLFW.GLFW_KEY_4)){
			xyPlaneMeshComp.setHeightData(xyPlaneMeshComp.getHeightData() == null ? woodHeightMap : null);
		}
		if(InputManager.getInstance().getKeyDown(GLFW.GLFW_KEY_5)){
			logger.info("exiting {} state", stateName);
			isFinished = true;
			return;
		}

		if(InputManager.getInstance().getKey(GLFW.GLFW_KEY_UP)){
			Quaternion xyPlaneRotation = xyPlaneEntity.transform.setDirty().getRotation();
			QuaternionMathIP.rotate(xyPlaneRotation, xyPlaneRotation, QuaternionMathOOP.eulerDeg(new Vec3f(0.5f, 0f, 0f)));
		}
		if(InputManager.getInstance().getKey(GLFW.GLFW_KEY_DOWN)){
			Quaternion xyPlaneRotation = xyPlaneEntity.transform.setDirty().getRotation();
			QuaternionMathIP.rotate(xyPlaneRotation, xyPlaneRotation, QuaternionMathOOP.eulerDeg(new Vec3f(-0.5f, 0f, 0f)));
		}
		if(InputManager.getInstance().getKey(GLFW.GLFW_KEY_RIGHT)){
			Quaternion xyPlaneRotation = xyPlaneEntity.transform.setDirty().getRotation();
			QuaternionMathIP.rotate(xyPlaneRotation, xyPlaneRotation, QuaternionMathOOP.eulerDeg(new Vec3f(0f, 0f, 0.5f)));
		}
		if(InputManager.getInstance().getKey(GLFW.GLFW_KEY_LEFT)){
			Quaternion xyPlaneRotation = xyPlaneEntity.transform.setDirty().getRotation();
			QuaternionMathIP.rotate(xyPlaneRotation, xyPlaneRotation, QuaternionMathOOP.eulerDeg(new Vec3f(0f, 0f, -0.5f)));
		}

		cameraControls.update();
	}

	@Override
	public void onExit() {
		EntityRegistry.getInstance().eraseEntity(xyPlaneEntity);
		EntityRegistry.getInstance().eraseEntity(lightSource);
		if(xyPlaneMeshData != null){
			xyPlaneMeshData.delete();
		}
		ITexture.deleteTexture(woodTexture);
		ITexture.deleteTexture(woodPhong);
		ITexture.deleteTexture(woodNormal);
		ITexture.deleteTexture(woodHeightMap);
		super.onExit();
	}

	@Override
	public void printControls() {
		super.printControls();
		logger.info("- WASD + EQ = Camera Movement");
		logger.info("- Mouse = Camera Yaw/Pitch");
		logger.info("- Use [UP], [DOWN], [LEFT], [RIGHT] to rotate around the x and z axes.");
		logger.info("- press [1] to toggle between fallback and wood texture");
		logger.info("- press [2] to toggle between fallback and wood phong");
		logger.info("- press [3] to toggle between fallback and wood normal");
		logger.info("- press [4] to toggle between fallback and wood heightmap");
		logger.info("- press [5] to exit this state");
	}

	@Override
	public void loadEntities() {
		xyPlaneEntity = EntityRegistry.getInstance().createEntity();
		xyPlaneEntity.transform = new Transform(Vec3f.zero(), Quaternion.identity(), new Vec3f(1f));
		xyPlaneEntity.meshComponent = new Mesh(xyPlaneMeshData, woodTexture, woodPhong, woodNormal, woodHeightMap);

		lightSource = EntityRegistry.getInstance().createEntity();
		lightSource.lightComponent = Light.directional(new Vec3f(1f, 1f, 0.8f), 1.5f);
		lightSource.transform = new Transform(Vec3f.zero(), QuaternionMathOOP.eulerDeg(new Vec3f(40f, -40f, 0f)), new Vec3f(1f));
	}
}
