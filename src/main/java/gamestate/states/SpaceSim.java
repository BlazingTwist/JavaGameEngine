package gamestate.states;

import camera.BaseCamera;
import camera.PerspectiveCamera;
import camera.controls.FollowCamera;
import ecs.Entity;
import ecs.EntityRegistry;
import ecs.components.LifeTime;
import ecs.components.LifeTimeLightIntensity;
import ecs.components.Light;
import ecs.components.Mesh;
import ecs.components.OrbitalObject;
import ecs.components.RotationalVelocity;
import ecs.components.ScaleVelocity;
import ecs.components.SphereParticle;
import ecs.components.Transform;
import ecs.components.Velocity;
import gamestate.BaseGameState;
import gamestate.Time;
import java.util.ArrayList;
import java.util.Random;
import logging.LogbackLoggerProvider;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL45;
import org.slf4j.Logger;
import rendering.mesh.MeshData;
import rendering.programs.MeshRenderProgram;
import rendering.programs.PostProcessingProgram;
import rendering.programs.SphereParticleProgram;
import rendering.shaderdata.ShaderDataManager;
import rendering.texture.ITexture;
import rendering.texture.Sampler;
import rendering.texture.Texture2D;
import utils.input.InputManager;
import utils.operator.Operator;
import utils.quaternion.Quaternion;
import utils.quaternion.QuaternionMathIP;
import utils.quaternion.QuaternionMathOOP;
import utils.vector.Vec2f;
import utils.vector.Vec3f;
import utils.vector.Vec4f;

public class SpaceSim extends BaseGameState {

	private static final Logger logger = LogbackLoggerProvider.getLogger(SpaceSim.class);

	private static final Vec3f[] cannonOffsets = new Vec3f[]{
			new Vec3f(-6.6f, 0.8f, 0.58f),
			new Vec3f(-2.4f, -0.6f, -0.45f),
			new Vec3f(4.5f, -1.2f, -0.4f),
			new Vec3f(-0.7f, -0.45f, -4.9f)
	};

	private final MeshData sphereMeshData = MeshData.loadFromFile("models/midPolyUVSphereSmoothed.obj", false);
	private final MeshData smoothSphereInvertedMeshData = MeshData.loadFromFile("models/midPolyUVSphereSmoothed.obj", true);
	private final MeshData lowPolyIcoSphereSmoothedInverted = MeshData.loadFromFile("models/lowPolyIcoSphereSmoothed.obj", true);
	private final MeshData starSparrowMeshData = MeshData.loadFromFile("models/starSparrow/mesh01.obj", false);

	private final Texture2D skyboxTexture = Texture2D.fromResource("textures/space_skybox/tex3_argb.png", Sampler.linearMirroredSampler);
	private final Texture2D skyboxPhong = Texture2D.fromResource("textures/space_skybox/phong.png", Sampler.linearMirroredSampler);
	private final Texture2D starSparrowTexture = Texture2D.fromResource("models/starSparrow/texture/StarSparrow_Green.png", Sampler.linearMirroredSampler);
	private final Texture2D starSparrowPhong = Texture2D.fromResource("models/starSparrow/phong.png", Sampler.linearMirroredSampler);
	private final Texture2D starSparrowNormal = Texture2D.fromResource("models/starSparrow/normal.png", Sampler.linearMirroredSampler);

	private final MeshRenderProgram meshRenderProgram = new MeshRenderProgram();
	private final SphereParticleProgram particleProgram = new SphereParticleProgram();
	private final PostProcessingProgram postProcessingProgram = new PostProcessingProgram();

	private final BaseCamera baseCamera = new PerspectiveCamera(90f, 0.1f, 20_000f);
	private final Vec2f mouseSensitivity = new Vec2f(2f);
	private final int cameraRotationButton = GLFW.GLFW_MOUSE_BUTTON_LEFT;
	private final int cameraForwardKey = GLFW.GLFW_KEY_I;
	private final int cameraBackwardKey = GLFW.GLFW_KEY_K;
	private final int cameraLeftKey = GLFW.GLFW_KEY_J;
	private final int cameraRightKey = GLFW.GLFW_KEY_L;
	private final int cameraUpKey = GLFW.GLFW_KEY_O;
	private final int cameraDownKey = GLFW.GLFW_KEY_U;
	private final Vec3f cameraMoveSensitivity = new Vec3f(2f);
	private final FollowCamera camera1 = new FollowCamera(baseCamera, new Vec3f(0f, 3f, -10f), Vec3f.zero(),
			cameraRotationButton, mouseSensitivity,
			cameraForwardKey, cameraBackwardKey, cameraUpKey, cameraDownKey, cameraLeftKey, cameraRightKey, cameraMoveSensitivity
	);
	private final FollowCamera camera2 = new FollowCamera(baseCamera, new Vec3f(0f, 2f, 10f), new Vec3f(0f, 180f, 0f),
			cameraRotationButton, mouseSensitivity,
			cameraForwardKey, cameraBackwardKey, cameraUpKey, cameraDownKey, cameraLeftKey, cameraRightKey, cameraMoveSensitivity
	);
	private final FollowCamera camera3 = new FollowCamera(baseCamera, new Vec3f(-5f, 3f, 3f), new Vec3f(60f, 135f, 0f),
			cameraRotationButton, mouseSensitivity,
			cameraForwardKey, cameraBackwardKey, cameraUpKey, cameraDownKey, cameraLeftKey, cameraRightKey, cameraMoveSensitivity
	);
	private FollowCamera activeCamera = camera1;

	private final Vec3f ambientLight = new Vec3f(1.4f);

	private final Random random = new Random();

	private final ArrayList<Entity> solarSystemEntities = new ArrayList<>();
	private final ArrayList<Entity> speedParticles = new ArrayList<>();
	private Entity skyboxEntity;
	private Entity playerShipEntity;

	private int currentPlayerThrottle = 0;
	private double cannonCoolDownSecondsLeft = 0;
	private int currentCannonIndex = 0;

	public SpaceSim() {
		ShaderDataManager.getInstance().lighting_ambientLight.setData(ambientLight);
		meshRenderProgram.forceBindData();
		particleProgram.forceBindData();
		postProcessingProgram.forceBindData();
		loadEntities();
		printControls();
	}

	private void spawnPlanet(Vec3f position, Vec3f scale, Vec3f velocity, double mass, float lightRange, Vec3f lightColor) {
		Entity planetEntity = EntityRegistry.getInstance().createEntity();
		planetEntity.meshComponent = new Mesh(smoothSphereInvertedMeshData, null, null, null, null);
		planetEntity.transform = new Transform(position, Quaternion.identity(), scale);
		planetEntity.velocityComponent = new Velocity(velocity);
		planetEntity.orbitalComponent = new OrbitalObject(mass);
		planetEntity.rotationalVelocity = new RotationalVelocity(new Vec3f(0f));
		planetEntity.lightComponent = Light.point(lightRange, lightColor, 1f);
		solarSystemEntities.add(planetEntity);
	}

	private void spawnThrusterParticle(Transform shipTransform, Velocity shipVelocity, Vec3f spawnOffset,
									   Vec3f ejectionDirection, Vec3f spreadDirectionA, Vec3f spreadDirectionB) {
		float speedValue = random.nextFloat(5f, 10f);
		Vec3f ejectionVector = new Vec3f();
		for (int i = 0; i < Vec3f.DATA_LEN; i++) {
			ejectionVector.data[i] = (ejectionDirection.data[i] * speedValue)
					+ (spreadDirectionA.data[i] * random.nextFloat(-0.5f, 0.5f))
					+ (spreadDirectionB.data[i] * random.nextFloat(-0.5f, 0.5f));
		}
		Vec3f velocity = QuaternionMathOOP.rotate(shipTransform.getRotation(), ejectionVector);
		Vec3f spawnPosition = new Vec3f(
				random.nextFloat(-0.1f, 0.1f),
				random.nextFloat(-0.1f, 0.1f),
				random.nextFloat(-0.1f, 0.1f)
		)
				.apply(Operator.Add, QuaternionMathIP.rotate(shipTransform.getRotation(), spawnOffset))
				.apply(Operator.Add, shipTransform.getPosition());
		float radius = random.nextFloat(-0.03f, -0.01f);
		Entity particleEntity = EntityRegistry.getInstance().createEntity();
		particleEntity.velocityComponent = new Velocity(velocity.apply(Operator.Add, shipVelocity.getVelocity()));
		particleEntity.lifeTimeComponent = new LifeTime(2f);
		particleEntity.transform = new Transform(spawnPosition, Quaternion.identity(), new Vec3f(Math.abs(radius)));
		particleEntity.sphereParticleComponent = new SphereParticle(radius, new Vec4f(1.6f, 1.6f, 1.6f, 1.0f), new Vec4f(1.0f, 1.0f, 1.0f, 1.0f));
	}

	private void spawnProjectile(Transform shipTransform, Vec3f spawnOffset, Vec3f velocity) {
		Entity projectileEntity = EntityRegistry.getInstance().createEntity();
		projectileEntity.meshComponent = new Mesh(lowPolyIcoSphereSmoothedInverted, null, null, null, null);
		projectileEntity.transform = new Transform(
				QuaternionMathIP.rotate(shipTransform.getRotation(), spawnOffset).apply(Operator.Add, shipTransform.getPosition()),
				shipTransform.getRotation().copy(),
				new Vec3f(0.1f)
		);
		projectileEntity.velocityComponent = new Velocity(velocity);
		projectileEntity.rotationalVelocity = new RotationalVelocity(new Vec3f(0f, 0f, (float) Math.toRadians(45)));
		projectileEntity.lightComponent = Light.point(50f, new Vec3f(1f, 1f, 0f), 3f);
		projectileEntity.scaleVelocity = new ScaleVelocity(new Vec3f(0f, 0f, 25f));
		projectileEntity.lifeTimeComponent = new LifeTime(1.25f);
		projectileEntity.lifeTimeLightIntensityComponent = new LifeTimeLightIntensity(3f, 0f);
	}

	public void loadEntities() {
		Vec3f playerPosition = new Vec3f(0f, 240f, 0f);
		playerShipEntity = EntityRegistry.getInstance().createEntity();
		playerShipEntity.transform = new Transform(playerPosition, Quaternion.identity(), new Vec3f(1f));
		playerShipEntity.meshComponent = new Mesh(starSparrowMeshData, starSparrowTexture, starSparrowPhong, starSparrowNormal, null);
		playerShipEntity.rotationalVelocity = new RotationalVelocity(new Vec3f(0f));
		playerShipEntity.velocityComponent = new Velocity(new Vec3f(0f, 0f, 5.5f));
		playerShipEntity.orbitalComponent = new OrbitalObject(10);

		camera1.trackEntity(playerShipEntity);
		camera2.trackEntity(playerShipEntity);
		camera3.trackEntity(playerShipEntity);

		// Central Sun
		spawnPlanet(new Vec3f(0f), new Vec3f(200f), new Vec3f(0f), 100_000_000, 2000f, new Vec3f(1f, 0f, 0f));
		// Inner Orbit 1
		spawnPlanet(new Vec3f(0f, -400f, 0f), new Vec3f(30f), new Vec3f(4.7f, 0f, 1f), 20_000, 300f, new Vec3f(0f, 1f, 0f));
		// Inner Orbit 2
		spawnPlanet(new Vec3f(0f, -500f, 0f), new Vec3f(40f), new Vec3f(3.9f, 0f, 1f), 20_000, 400f, new Vec3f(0f, 1f, 1f));
		// Medium Orbit
		spawnPlanet(new Vec3f(0f, 0f, -1200f), new Vec3f(75f), new Vec3f(1f, 2.5f, 0f), 100_000, 750f, new Vec3f(0.75f, 0.5f, 0f));
		// Medium Orbit - Moon 1
		spawnPlanet(new Vec3f(0f, 0f, -1400f), new Vec3f(15f), new Vec3f(1f, 2.1f, 0f), 1_000, 150f, new Vec3f(1f, 0.75f, 0f));
		// Outer Orbit
		spawnPlanet(new Vec3f(-3400f, 0f, 500f), new Vec3f(100f), new Vec3f(0f, -0.5f, 1.5f), 500_000, 1000f, new Vec3f(0.75f, 0f, 0.75f));
		// Outer Orbit - Moon 1
		spawnPlanet(new Vec3f(-3550f, 0f, 500f), new Vec3f(20f), new Vec3f(0f, -0.5f, 1.03f), 3_000, 200f, new Vec3f(1f, 0f, 1f));
		// Outer Orbit - Moon 2
		spawnPlanet(new Vec3f(-3720f, 0f, 500f), new Vec3f(30f), new Vec3f(0f, -0.5f, 1.15f), 10_000, 300f, new Vec3f(0.8f, 0.4f, 0.8f));

		skyboxEntity = EntityRegistry.getInstance().createEntity();
		skyboxEntity.meshComponent = new Mesh(sphereMeshData, skyboxTexture, skyboxPhong, null, null);
		skyboxEntity.transform = new Transform(Vec3f.zero(), Quaternion.identity(), new Vec3f(8000f));

		for (int x = -49; x < 50; x += 10) {
			for (int y = -49; y < 50; y += 10) {
				for (int z = -49; z < 50; z += 10) {
					Vec3f position = playerPosition.copy().apply(Operator.Add,
							x + random.nextFloat(-3f, 3f),
							y + random.nextFloat(-3f, 3f),
							z + random.nextFloat(-3f, 3f)
					);
					Entity speedParticle = EntityRegistry.getInstance().createEntity();
					speedParticle.transform = new Transform(position, Quaternion.identity(), new Vec3f(0.05f));
					speedParticle.sphereParticleComponent = new SphereParticle(-0.05f, new Vec4f(2f, 2f, 2f, 1f), new Vec4f(2f, 2f, 2f, 1f));
					speedParticles.add(speedParticle);
				}
			}
		}
	}

	public void printControls() {
		logger.info("SpaceSim Controls:");
		logger.info("- W/S = Pitch Down/Up");
		logger.info("- A/D = Roll Left/Right");
		logger.info("- Q/E = Yaw Left/Right");
		logger.info("- UP/DOWN = Strafe up/down");
		logger.info("- LEFT/RIGHT = Strafe left/right");
		logger.info("- R/F = Increase/Decrease throttle along Thrust Axis");
		logger.info("- X = Enable Rotation Dampeners");
		logger.info("- C = Enable Velocity Dampeners");
		logger.info("- Space-Bar = Fire Laser");
		logger.info("Camera Controls");
		logger.info("- press [1] for 3rd person follow camera");
		logger.info("- press [2] for 3rd person reverse camera");
		logger.info("- press [3] for landing camera");
		logger.info("- hold left-click and drag to adjust camera view direction");
		logger.info("- use I/J/K/L and U/O to adjust camera position");
		logger.info("- press [M] to reset active camera position and rotation");
		logger.info("State Controls");
		logger.info("- press [P] to exit this state");
	}

	private void checkHotkeys() {
		if (InputManager.getInstance().getKeyDown(GLFW.GLFW_KEY_1)) {
			activeCamera = camera1;
		}
		if (InputManager.getInstance().getKeyDown(GLFW.GLFW_KEY_2)) {
			activeCamera = camera2;
		}
		if (InputManager.getInstance().getKeyDown(GLFW.GLFW_KEY_3)) {
			activeCamera = camera3;
		}
		if (InputManager.getInstance().getKeyDown(GLFW.GLFW_KEY_M)) {
			activeCamera.resetCameraOffsets();
		}
		if (InputManager.getInstance().getKeyDown(GLFW.GLFW_KEY_P)) {
			logger.info("exiting SpaceSim State");
			isFinished = true;
		}
	}

	private void handleFlightControls() {
		Transform shipTransform = playerShipEntity.transform;
		Velocity shipVelocity = playerShipEntity.velocityComponent;
		RotationalVelocity shipRotationVelocity = playerShipEntity.rotationalVelocity;

		final float shipPitchSensitivity = 0.25f;
		final float shipRollSensitivity = 0.25f;
		final float shipYawSensitivity = 0.25f;
		final float shipStrafeSensitivity = 0.25f;
		final float shipThrustFactor = 1.0f;
		final float shipRotationDampFactor = 0.9f;
		final float shipVelocityDampFactor = 2.0f;
		final float projectileVelocityVal = 200f;

		if (InputManager.getInstance().getKey(GLFW.GLFW_KEY_W)) {
			shipRotationVelocity.eulerAngleVelocity.apply(Operator.Add, Time.physicsDeltaSecondsF * shipPitchSensitivity, 0f, 0f);
			spawnThrusterParticle(shipTransform, shipVelocity, new Vec3f(0f, 0.04f, 4.77f), Vec3f.up(), Vec3f.right(), Vec3f.forward());
			spawnThrusterParticle(shipTransform, shipVelocity, new Vec3f(0f, -0.93f, -3.9f), Vec3f.down(), Vec3f.right(), Vec3f.forward());
		}
		if (InputManager.getInstance().getKey(GLFW.GLFW_KEY_S)) {
			shipRotationVelocity.eulerAngleVelocity.apply(Operator.Sub, Time.physicsDeltaSecondsF * shipPitchSensitivity, 0f, 0f);
			spawnThrusterParticle(shipTransform, shipVelocity, new Vec3f(0f, -0.43f, 4.77f), Vec3f.down(), Vec3f.right(), Vec3f.forward());
			spawnThrusterParticle(shipTransform, shipVelocity, new Vec3f(0f, 0.3f, -3.9f), Vec3f.up(), Vec3f.right(), Vec3f.forward());
		}
		if (InputManager.getInstance().getKey(GLFW.GLFW_KEY_A)) {
			shipRotationVelocity.eulerAngleVelocity.apply(Operator.Add, 0f, 0f, Time.physicsDeltaSecondsF * shipRollSensitivity);
			spawnThrusterParticle(shipTransform, shipVelocity, new Vec3f(-3.16f, 0.33f, -1.0f), Vec3f.up(), Vec3f.right(), Vec3f.forward());
			spawnThrusterParticle(shipTransform, shipVelocity, new Vec3f(3.16f, -0.73f, -1.0f), Vec3f.down(), Vec3f.right(), Vec3f.forward());
		}
		if (InputManager.getInstance().getKey(GLFW.GLFW_KEY_D)) {
			shipRotationVelocity.eulerAngleVelocity.apply(Operator.Sub, 0f, 0f, Time.physicsDeltaSecondsF * shipRollSensitivity);
			spawnThrusterParticle(shipTransform, shipVelocity, new Vec3f(3.16f, 0.33f, -1.0f), Vec3f.up(), Vec3f.right(), Vec3f.forward());
			spawnThrusterParticle(shipTransform, shipVelocity, new Vec3f(-3.16f, -0.73f, -1.0f), Vec3f.down(), Vec3f.right(), Vec3f.forward());
		}
		if (InputManager.getInstance().getKey(GLFW.GLFW_KEY_Q)) {
			shipRotationVelocity.eulerAngleVelocity.apply(Operator.Sub, 0f, Time.physicsDeltaSecondsF * shipYawSensitivity, 0f);
			spawnThrusterParticle(shipTransform, shipVelocity, new Vec3f(-3.18f, -0.25f, -0.185f), Vec3f.forward(), Vec3f.right(), Vec3f.up());
			spawnThrusterParticle(shipTransform, shipVelocity, new Vec3f(3.18f, -0.25f, -4.21f), Vec3f.backward(), Vec3f.right(), Vec3f.up());
		}
		if (InputManager.getInstance().getKey(GLFW.GLFW_KEY_E)) {
			shipRotationVelocity.eulerAngleVelocity.apply(Operator.Add, 0f, Time.physicsDeltaSecondsF * shipYawSensitivity, 0f);
			spawnThrusterParticle(shipTransform, shipVelocity, new Vec3f(3.18f, -0.25f, -0.185f), Vec3f.forward(), Vec3f.right(), Vec3f.up());
			spawnThrusterParticle(shipTransform, shipVelocity, new Vec3f(-3.18f, -0.25f, -4.21f), Vec3f.backward(), Vec3f.right(), Vec3f.up());
		}

		if (InputManager.getInstance().getKey(GLFW.GLFW_KEY_UP)) {
			shipVelocity.getVelocity().apply(Operator.Add,
					QuaternionMathIP.rotate(shipTransform.getRotation(), new Vec3f(0f, Time.physicsDeltaSecondsF * shipStrafeSensitivity, 0f)));
			spawnThrusterParticle(shipTransform, shipVelocity, new Vec3f(3.16f, -0.73f, -1f), Vec3f.down(), Vec3f.right(), Vec3f.forward());
			spawnThrusterParticle(shipTransform, shipVelocity, new Vec3f(-3.16f, -0.73f, -1f), Vec3f.down(), Vec3f.right(), Vec3f.forward());
		}
		if (InputManager.getInstance().getKey(GLFW.GLFW_KEY_DOWN)) {
			shipVelocity.getVelocity().apply(Operator.Sub,
					QuaternionMathIP.rotate(shipTransform.getRotation(), new Vec3f(0f, Time.physicsDeltaSecondsF * shipStrafeSensitivity, 0f)));
			spawnThrusterParticle(shipTransform, shipVelocity, new Vec3f(-3.16f, 0.33f, -1f), Vec3f.up(), Vec3f.right(), Vec3f.forward());
			spawnThrusterParticle(shipTransform, shipVelocity, new Vec3f(3.16f, 0.33f, -1f), Vec3f.up(), Vec3f.right(), Vec3f.forward());
		}
		if (InputManager.getInstance().getKey(GLFW.GLFW_KEY_LEFT)) {
			shipVelocity.getVelocity().apply(Operator.Sub,
					QuaternionMathIP.rotate(shipTransform.getRotation(), new Vec3f(Time.physicsDeltaSecondsF * shipStrafeSensitivity, 0f, 0f)));
			spawnThrusterParticle(shipTransform, shipVelocity, new Vec3f(3.85f, -0.23f, -1f), Vec3f.right(), Vec3f.up(), Vec3f.forward());
		}
		if (InputManager.getInstance().getKey(GLFW.GLFW_KEY_RIGHT)) {
			shipVelocity.getVelocity().apply(Operator.Add,
					QuaternionMathIP.rotate(shipTransform.getRotation(), new Vec3f(Time.physicsDeltaSecondsF * shipStrafeSensitivity, 0f, 0f)));
			spawnThrusterParticle(shipTransform, shipVelocity, new Vec3f(-3.85f, -0.23f, -1f), Vec3f.left(), Vec3f.up(), Vec3f.forward());
		}

		if (InputManager.getInstance().getKeyDown(GLFW.GLFW_KEY_R)) {
			currentPlayerThrottle++;
			if (currentPlayerThrottle > 5) {
				logger.info("throttle maxed. (5)");
				currentPlayerThrottle = 5;
			} else {
				// TODO ship animation
				logger.info("throttle set to {}", currentPlayerThrottle);
			}
		}
		if (InputManager.getInstance().getKeyDown(GLFW.GLFW_KEY_F)) {
			currentPlayerThrottle--;
			if (currentPlayerThrottle < -2) {
				logger.info("throttle maxed. (-2)");
				currentPlayerThrottle = -2;
			} else {
				// TODO ship animation
				logger.info("throttle set to {}", currentPlayerThrottle);
			}
		}
		if (currentPlayerThrottle != 0) {
			float throttleFactor = new float[]{-3f, -1f, 0f, 1f, 2f, 4f, 6f, 12f}[currentPlayerThrottle + 2];
			shipVelocity.getVelocity().apply(Operator.Add, QuaternionMathIP.rotate(
					shipTransform.getRotation(),
					new Vec3f(0f, 0f, Time.physicsDeltaSecondsF * shipThrustFactor * throttleFactor)
			));
			if (currentPlayerThrottle < 0) {
				spawnThrusterParticle(shipTransform, shipVelocity, new Vec3f(-3.18f, -0.25f, -0.185f), Vec3f.forward(), Vec3f.right(), Vec3f.up());
				spawnThrusterParticle(shipTransform, shipVelocity, new Vec3f(3.18f, -0.25f, -0.185f), Vec3f.forward(), Vec3f.right(), Vec3f.up());
			}
		}

		if (InputManager.getInstance().getKey(GLFW.GLFW_KEY_X)) {
			shipRotationVelocity.eulerAngleVelocity.apply(Operator.Mul, shipRotationDampFactor);
		}
		if (InputManager.getInstance().getKey(GLFW.GLFW_KEY_C)) {
			Vec3f dampingThrust = shipVelocity.getVelocity().copy().normalize().apply(Operator.Mul, (-Time.physicsDeltaSecondsF) * shipVelocityDampFactor);
			shipVelocity.getVelocity().apply(Operator.Add, dampingThrust);
		}

		if (cannonCoolDownSecondsLeft > 0) {
			cannonCoolDownSecondsLeft -= Time.physicsDeltaSeconds;
		} else if (InputManager.getInstance().getKey(GLFW.GLFW_KEY_SPACE)) {
			cannonCoolDownSecondsLeft = 0.1;
			Vec3f cannonOffset_left = cannonOffsets[currentCannonIndex].copy();
			Vec3f cannonOffset_right = cannonOffset_left.copy().apply(Operator.Mul, -1f, 1f, 1f);
			Vec3f projectileVelocity = QuaternionMathIP.rotate(shipTransform.getRotation(), new Vec3f(0f, 0f, projectileVelocityVal))
					.apply(Operator.Add, shipVelocity.getVelocity());
			spawnProjectile(shipTransform, cannonOffset_left, projectileVelocity);
			spawnProjectile(shipTransform, cannonOffset_right, projectileVelocity);
			currentCannonIndex = (currentCannonIndex + 1) % 4;
		}
	}

	@Override
	public void update() {
		checkHotkeys();
		if (isFinished) {
			return;
		}
		handleFlightControls();

		// TODO shipAnimation

		Vec3f cameraPosition = activeCamera.getCamera().getPosition();
		Vec3f positionDelta = new Vec3f();
		for (Entity speedParticle : speedParticles) {
			Vec3f particlePosition = speedParticle.transform.getPosition();
			Operator.Sub.apply(positionDelta.data, 0, particlePosition.data, 0, cameraPosition.data, 0, Vec3f.DATA_LEN);
			boolean positionChanged = positionDelta.data[0] < -50f || positionDelta.data[0] > 50f
					|| positionDelta.data[1] < -50f || positionDelta.data[1] > 50f
					|| positionDelta.data[2] < -50f || positionDelta.data[2] > 50f;
			if (positionDelta.data[0] < -50f) particlePosition.data[0] += 100f;
			if (positionDelta.data[0] > 50f) particlePosition.data[0] -= 100f;
			if (positionDelta.data[1] < -50f) particlePosition.data[1] += 100f;
			if (positionDelta.data[1] > 50f) particlePosition.data[1] -= 100f;
			if (positionDelta.data[2] < -50f) particlePosition.data[2] += 100f;
			if (positionDelta.data[2] > 50f) particlePosition.data[2] -= 100f;
			if (positionChanged) {
				speedParticle.transform.setDirty();
			}
		}

		activeCamera.update();
	}

	@Override
	public void draw() {
		postProcessingProgram.bindFrameBuffer();
		meshRenderProgram.execute();
		particleProgram.execute();

		GL45.glBindFramebuffer(GL45.GL_FRAMEBUFFER, 0);
		postProcessingProgram.execute();
	}

	@Override
	public void onResume() {
		ShaderDataManager.getInstance().lighting_ambientLight.setData(ambientLight);
		meshRenderProgram.forceBindData();
		particleProgram.forceBindData();
		postProcessingProgram.forceBindData();
		printControls();
	}

	@Override
	public void onPause() {
		logger.info("===== SpaceSim State paused =====");
	}

	private static void deleteMeshData(MeshData meshData) {
		if (meshData != null) {
			meshData.delete();
		}
	}

	@Override
	public void onExit() {
		for (Entity entity : solarSystemEntities) {
			EntityRegistry.getInstance().eraseEntity(entity);
		}
		for (Entity entity : speedParticles) {
			EntityRegistry.getInstance().eraseEntity(entity);
		}
		EntityRegistry.getInstance().eraseEntity(skyboxEntity);
		EntityRegistry.getInstance().eraseEntity(playerShipEntity);

		deleteMeshData(sphereMeshData);
		deleteMeshData(smoothSphereInvertedMeshData);
		deleteMeshData(lowPolyIcoSphereSmoothedInverted);
		deleteMeshData(starSparrowMeshData);
		ITexture.deleteTexture(skyboxTexture);
		ITexture.deleteTexture(skyboxPhong);
		ITexture.deleteTexture(starSparrowTexture);
		ITexture.deleteTexture(starSparrowNormal);
		ITexture.deleteTexture(starSparrowNormal);

		meshRenderProgram.delete();
		particleProgram.delete();
		postProcessingProgram.delete();
	}
}
