package gamestate.states;

import ecs.Entity;
import ecs.EntityRegistry;
import ecs.components.Light;
import ecs.components.Mesh;
import ecs.components.OrbitalObject;
import ecs.components.Transform;
import ecs.components.Velocity;
import gamestate.DefaultGameState;
import gamestate.IGameStateManager;
import org.lwjgl.glfw.GLFW;
import rendering.mesh.MeshData;
import rendering.texture.Sampler;
import rendering.texture.Texture2D;
import utils.input.InputManager;
import utils.quaternion.Quaternion;
import utils.vector.Vec3f;

public class OrbitDemo extends DefaultGameState {
	private static final float defaultLightRange = 100f;
	private static final float defaultLightIntensity = 0.75f;

	private static final Vec3f defaultPlanetPosition = new Vec3f(0f, 3.5f, 0f);
	private static final Vec3f defaultPlanetScale = new Vec3f(1f);
	private static final Vec3f defaultPlanetVelocity = new Vec3f(-2f, -0.5f, 0f);

	private static final Vec3f defaultSunPosition = new Vec3f(0f, -3.5f, 0f);
	private static final Vec3f defaultSunScale = new Vec3f(1.6f);
	private static final Vec3f defaultSunVelocity = new Vec3f(0.5f, 0.125f, 0f);

	private final MeshData sphereMeshData = MeshData.loadFromFile("models/sphere.obj", false);
	private final MeshData sphereInvertedMeshData = MeshData.loadFromFile("models/sphere.obj", true);
	private final Texture2D planetTexture = Texture2D.fromResource("textures/planet1.png", Sampler.linearMirroredSampler);
	private final Texture2D planetPhong = Texture2D.fromResource("textures/Planet1_phong.png", Sampler.linearMirroredSampler);
	private final Texture2D sunTexture = Texture2D.fromResource("textures/SunTexture.png", Sampler.linearMirroredSampler);
	private final Texture2D sunPhong = Texture2D.fromResource("textures/Sun_phong.png", Sampler.linearMirroredSampler);

	private Entity planetEntity = null;
	private Entity sunEntity = null;

	public OrbitDemo() {
		super("Orbit Demo", new Vec3f(1.4f, 1.4f, 1.4f), new Vec3f(0f, 0f, -7f),
				true, false, false, false);
		super.init();
	}

	@Override
	public void update() {
		if (InputManager.getInstance().getKeyDown(GLFW.GLFW_KEY_1)) {
			logger.info("resetting scene");
			initializeScene();
		}
		if (InputManager.getInstance().getKeyDown(GLFW.GLFW_KEY_2)) {
			logger.info("main state hotkey pressed");
			IGameStateManager.startGameState(MainState.class);
			return;
		}
		if (InputManager.getInstance().getKeyDown(GLFW.GLFW_KEY_3)) {
			logger.info("exiting {} state", stateName);
			isFinished = true;
			return;
		}

		cameraControls.update();

		final float lightRangeStep = 0.1f;
		final float lightIntensityStep = 0.05f;
		if(InputManager.getInstance().getKey(GLFW.GLFW_KEY_R)){
			sunEntity.lightComponent.setRange(sunEntity.lightComponent.getRange() + lightRangeStep);
			logger.info("new range: {}", sunEntity.lightComponent.getRange());
		}
		if(InputManager.getInstance().getKey(GLFW.GLFW_KEY_F)){
			sunEntity.lightComponent.setRange(sunEntity.lightComponent.getRange() - lightRangeStep);
			logger.info("new range: {}", sunEntity.lightComponent.getRange());
		}
		if(InputManager.getInstance().getKey(GLFW.GLFW_KEY_T)){
			sunEntity.lightComponent.setIntensity(sunEntity.lightComponent.getIntensity() + lightIntensityStep);
			logger.info("new intensity: {}", sunEntity.lightComponent.getIntensity());
		}
		if(InputManager.getInstance().getKey(GLFW.GLFW_KEY_G)){
			sunEntity.lightComponent.setIntensity(sunEntity.lightComponent.getIntensity() - lightIntensityStep);
			logger.info("new intensity: {}", sunEntity.lightComponent.getIntensity());
		}
	}

	@Override
	public void onExit() {
		EntityRegistry.getInstance().eraseEntity(planetEntity);
		EntityRegistry.getInstance().eraseEntity(sunEntity);
		if(sphereMeshData != null){
			sphereMeshData.delete();
		}
		if(sphereInvertedMeshData != null){
			sphereInvertedMeshData.delete();
		}
		if(planetTexture != null){
			planetTexture.deleteTexture();
		}
		if(planetPhong != null){
			planetPhong.deleteTexture();
		}
		if(sunTexture != null){
			sunTexture.deleteTexture();
		}
		if(sunPhong != null){
			sunPhong.deleteTexture();
		}
		super.onExit();
	}

	@Override
	public void printControls() {
		super.printControls();
		logger.info("- WASD + EQ = Camera Movement");
		logger.info("- Mouse = Camera Yaw/Pitch");
		logger.info("- R/F = increase / decrease light range");
		logger.info("- T/G = increase / decrease light intensity");
		logger.info("- press [1] to reset scene");
		logger.info("- press [2] to start a new MainGameState");
		logger.info("- press [3] to exit this state");
	}

	@Override
	public void loadEntities() {
		planetEntity = EntityRegistry.getInstance().createEntity();
		planetEntity.transform = new Transform(defaultPlanetPosition.copy(), Quaternion.identity(), defaultPlanetScale.copy());
		planetEntity.meshComponent = new Mesh(sphereMeshData, planetTexture, planetPhong, null, null);
		planetEntity.velocityComponent = new Velocity(defaultPlanetVelocity.copy());
		planetEntity.orbitalComponent = new OrbitalObject(150_000.0);

		sunEntity = EntityRegistry.getInstance().createEntity();
		sunEntity.transform = new Transform(defaultSunPosition.copy(), Quaternion.identity(), defaultSunScale.copy());
		sunEntity.meshComponent = new Mesh(sphereInvertedMeshData, sunTexture, sunPhong, null, null);
		sunEntity.velocityComponent = new Velocity(defaultSunVelocity.copy());
		sunEntity.lightComponent = Light.point(defaultLightRange, new Vec3f(1f, 1f, 0.8f), defaultLightIntensity);
		sunEntity.orbitalComponent = new OrbitalObject(600_000.0);
	}

	@Override
	public void initializeScene() {
		super.initializeScene();
		sunEntity.transform.setPosition(defaultSunPosition)
				.setScale(defaultSunScale);
		sunEntity.velocityComponent.setVelocity(defaultSunVelocity);
		sunEntity.lightComponent.setRange(defaultLightRange)
				.setIntensity(defaultLightIntensity);

		planetEntity.transform.setPosition(defaultPlanetPosition)
				.setScale(defaultPlanetScale);
		planetEntity.velocityComponent.setVelocity(defaultPlanetVelocity);
	}
}
