package gamestate.states;

import ecs.Entity;
import ecs.EntityRegistry;
import ecs.components.Light;
import ecs.components.Mesh;
import ecs.components.Transform;
import gamestate.DefaultGameState;
import gamestate.IGameStateManager;
import gamestate.Time;
import org.lwjgl.glfw.GLFW;
import rendering.mesh.MeshData;
import rendering.texture.Sampler;
import rendering.texture.Texture2D;
import utils.input.InputManager;
import utils.operator.Operator;
import utils.quaternion.Quaternion;
import utils.quaternion.QuaternionMathOOP;
import utils.vector.Vec3f;

public class SpringDemo extends DefaultGameState {

	private final MeshData sphereMeshData = MeshData.loadFromFile("models/sphere.obj", false);
	private final Texture2D planetTexture = Texture2D.fromResource("textures/planet1.png", Sampler.linearMirroredSampler);
	private final Texture2D planetPhong = Texture2D.fromResource("textures/Planet1_phong.png", Sampler.linearMirroredSampler);

	private final MeshData crateMeshData = MeshData.loadFromFile("models/crate.obj", false);
	private final Texture2D crateTexture = Texture2D.fromResource("textures/cratetex.png", Sampler.linearMirroredSampler);

	private Entity planetEntity = null;
	private Entity crateEntity = null;
	private Entity lightSource = null;
	private float planetVelocity = 0f;

	public SpringDemo() {
		super("Spring Demo", new Vec3f(0.7f, 0.7f, 0.7f), new Vec3f(0f, 0f, -3f),
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
			logger.info("exiting Spring Demo state");
			isFinished = true;
			return;
		}

		cameraControls.update();

		final float spotLightMoveStep = 0.1f;
		final float spotLightAngleStep = 0.2f;
		Light lightComponent = lightSource.lightComponent;
		Transform lightTransform = lightSource.transform;
		if (InputManager.getInstance().getKey(GLFW.GLFW_KEY_UP)) {
			lightTransform.setDirty().getPosition().apply(Operator.Add, 0f, spotLightMoveStep, 0f);
		}
		if (InputManager.getInstance().getKey(GLFW.GLFW_KEY_DOWN)) {
			lightTransform.setDirty().getPosition().apply(Operator.Add, 0f, -spotLightMoveStep, 0f);
		}
		if (InputManager.getInstance().getKey(GLFW.GLFW_KEY_LEFT)) {
			lightTransform.setDirty().getPosition().apply(Operator.Add, -spotLightMoveStep, 0f, 0f);
		}
		if (InputManager.getInstance().getKey(GLFW.GLFW_KEY_RIGHT)) {
			lightTransform.setDirty().getPosition().apply(Operator.Add, spotLightMoveStep, 0f, 0f);
		}
		if (InputManager.getInstance().getKey(GLFW.GLFW_KEY_R)) {
			lightComponent.setSpotAngle(lightComponent.getSpotAngle() + spotLightAngleStep);
		}
		if (InputManager.getInstance().getKey(GLFW.GLFW_KEY_F)) {
			lightComponent.setSpotAngle(lightComponent.getSpotAngle() - spotLightAngleStep);
		}
		if (InputManager.getInstance().getKey(GLFW.GLFW_KEY_X)) {
			lightTransform.setPosition(cameraControls.getCamera().getPosition());
			lightTransform.setRotation(QuaternionMathOOP.toQuaternion(cameraControls.getCamera().getLookAtMatrix()));
		}

		Transform planetTransform = planetEntity.transform;
		Vec3f planetPosition = planetTransform.getPosition();
		final float springConstant = 10f;
		final float sphereMass = 50f;
		float xDisplacement = planetPosition.data[0];
		double displacementForce = 0 - (springConstant * xDisplacement);
		double displacementAcceleration = displacementForce / sphereMass;
		double displacementVelocity = displacementAcceleration * Time.physicsDeltaSeconds;
		double accelerationDistance = displacementAcceleration * Time.physicsDeltaSecondsSquared * 0.5;
		double totalTravelDistance = (planetVelocity * Time.physicsDeltaSeconds) + accelerationDistance;
		planetPosition.apply(Operator.Add, (float) totalTravelDistance, 0f, 0f);
		planetVelocity += (float) displacementVelocity;
		planetTransform.setDirty();
	}

	@Override
	public void onExit() {
		EntityRegistry.getInstance().eraseEntity(planetEntity);
		EntityRegistry.getInstance().eraseEntity(crateEntity);
		EntityRegistry.getInstance().eraseEntity(lightSource);
		if (sphereMeshData != null) {
			sphereMeshData.delete();
		}
		if (crateMeshData != null) {
			crateMeshData.delete();
		}
		if (planetTexture != null) {
			planetTexture.deleteTexture();
		}
		if (planetPhong != null) {
			planetPhong.deleteTexture();
		}
		if (crateTexture != null) {
			crateTexture.deleteTexture();
		}
		super.onExit();
	}

	@Override
	public void printControls() {
		super.printControls();
		logger.info("- WASD + EQ = Camera Movement");
		logger.info("- Mouse = Camera Yaw/Pitch");
		logger.info("- Arrows = Move light-source in x/y plane");
		logger.info("- R/F = increase / decrease light spot-angle");
		logger.info("- X = move spotlight to camera position and direction");
		logger.info("- press [1] to reset scene");
		logger.info("- press [2] to start a new MainGameState");
		logger.info("- press [3] to exit this state");
	}

	@Override
	public void loadEntities() {
		planetEntity = EntityRegistry.getInstance().createEntity();
		planetEntity.transform = new Transform(new Vec3f(-2f, 0f, 0f), Quaternion.identity(), new Vec3f(1f));
		planetEntity.meshComponent = new Mesh(sphereMeshData, planetTexture, planetPhong, null, null);

		crateEntity = EntityRegistry.getInstance().createEntity();
		crateEntity.transform = new Transform(new Vec3f(0f, -1.5f, 0f), Quaternion.identity(), new Vec3f(4f, 0.2f, 1.0f));
		crateEntity.meshComponent = new Mesh(crateMeshData, crateTexture, null, null, null);

		lightSource = EntityRegistry.getInstance().createEntity();
		lightSource.lightComponent = Light.spot(25f, 25f, new Vec3f(1f, 1f, 0.8f), 2f);
		lightSource.transform = new Transform(new Vec3f(0f, 0f, -3f), Quaternion.identity(), new Vec3f(1f));
	}

	@Override
	public void initializeScene() {
		super.initializeScene();
		lightSource.transform.getPosition().set(0f, 0f, -3f);
		lightSource.transform.setRotation(Quaternion.identity());
		lightSource.lightComponent.setSpotAngle(25f);

		planetEntity.transform.getPosition().set(-2f, 0f, 0f);
		planetEntity.transform.setDirty();

		planetVelocity = 0f;
	}
}
