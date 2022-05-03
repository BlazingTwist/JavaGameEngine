package gamestate.states;

import ecs.Entity;
import ecs.EntityRegistry;
import ecs.components.Light;
import ecs.components.Mesh;
import ecs.components.Shockwave;
import ecs.components.ShockwaveExpandingAnimator;
import ecs.components.SphereParticle;
import ecs.components.Transform;
import gamestate.DefaultGameState;
import java.util.ArrayList;
import java.util.Random;
import org.lwjgl.glfw.GLFW;
import rendering.mesh.MeshData;
import rendering.texture.ITexture;
import rendering.texture.Sampler;
import rendering.texture.Texture2D;
import utils.input.InputManager;
import utils.operator.Operator;
import utils.quaternion.Quaternion;
import utils.quaternion.QuaternionMathOOP;
import utils.vector.Vec3f;
import utils.vector.Vec4f;

public class LightDemo extends DefaultGameState {

	private static final Random random = new Random();

	public static Vec3f getRandomColor() {
		return new Vec3f(random.nextFloat(), random.nextFloat(), random.nextFloat());
	}

	private final MeshData xyPlaneMeshData = MeshData.loadFromFile("models/xyplane.obj", false);
	private final MeshData starSparrowMeshData = MeshData.loadFromFile("models/starSparrow/mesh01.obj", false);
	private final MeshData smoothSphereInvertedMeshData = MeshData.loadFromFile("models/midPolyUVSphereSmoothed.obj", false);

	private final Texture2D woodTexture = Texture2D.fromResource("textures/woodlogwall/texture.png", Sampler.linearMirroredSampler);
	private final Texture2D woodPhong = Texture2D.fromResource("textures/woodlogwall/phong.png", Sampler.linearMirroredSampler);
	private final Texture2D woodNormal = Texture2D.fromResource("textures/woodlogwall/normal.png", Sampler.linearMirroredSampler);
	private final Texture2D woodHeightMap = Texture2D.fromResource("textures/woodlogwall/heightmap.png", Sampler.linearMirroredSampler);
	private final Texture2D bwCheckerTexture = Texture2D.fromResource("textures/bw_checker.png", Sampler.linearMirroredSampler);
	private final Texture2D starSparrowTexture = Texture2D.fromResource("models/starSparrow/texture/StarSparrow_Green.png", Sampler.linearMirroredSampler);
	private final Texture2D starSparrowPhong = Texture2D.fromResource("models/starSparrow/phong.png", Sampler.linearMirroredSampler);
	private final Texture2D starSparrowNormal = Texture2D.fromResource("models/starSparrow/normal.png", Sampler.linearMirroredSampler);

	private final ArrayList<Entity> activeLights = new ArrayList<>();

	private Entity woodEntity = null;
	private Entity bwCheckerEntity = null;
	private Entity shipEntity = null;
	private Entity sphereEntity = null;

	public LightDemo() {
		super("LightDemo", new Vec3f(1.4f, 1.4f, 1.4f), new Vec3f(0f, 0f, -3f),
				true, true, true, true);
		super.init();
	}

	@Override
	public void update() {
		if (InputManager.getInstance().getKeyDown(GLFW.GLFW_KEY_1)) {
			Vec3f position = cameraControls.getCamera().getPosition().copy();
			Quaternion rotation = QuaternionMathOOP.lookAtToQuaternion(cameraControls.getCamera().getLookAtMatrix());
			Vec3f color = getRandomColor();
			Entity lightEntity = EntityRegistry.getInstance().createEntity();
			lightEntity.lightComponent = Light.spot(50f, 22.5f, color, 0.75f);
			lightEntity.transform = new Transform(position, rotation, new Vec3f(0.1f));
			lightEntity.sphereParticleComponent = new SphereParticle(
					-0.15f,
					new Vec4f(color.copy().apply(Operator.Add, 0.5f), 1f),
					new Vec4f(color, 1f)
			);
			activeLights.add(lightEntity);
		}

		if (InputManager.getInstance().getKeyDown(GLFW.GLFW_KEY_2)) {
			if (activeLights.isEmpty()) {
				logger.info("No lights left to be destroyed! Try spawning one first.");
			} else {
				Vec3f cameraPosition = cameraControls.getCamera().getPosition();
				Entity nearestLight = activeLights.stream()
						.min((a, b) -> Float.compare(
								a.transform.getPosition().distanceSquared(cameraPosition),
								b.transform.getPosition().distanceSquared(cameraPosition)
						)).orElse(null);
				if (nearestLight != null) {
					activeLights.remove(nearestLight);
					EntityRegistry.getInstance().eraseEntity(nearestLight);
				}
			}
		}

		if (InputManager.getInstance().getKeyDown(GLFW.GLFW_KEY_3)) {
			logger.info("exiting {} state", stateName);
			isFinished = true;
			return;
		}

		if (InputManager.getInstance().getKeyDown(GLFW.GLFW_KEY_F)) {
			for (Entity activeLight : activeLights) {
				Vec3f newColor = new Vec3f(1f).apply(Operator.Sub, activeLight.lightComponent.getColor());
				activeLight.lightComponent.setColor(newColor);
				activeLight.sphereParticleComponent.setColorInner(new Vec4f(newColor.copy().apply(Operator.Add, 0.5f), 1f));
				activeLight.sphereParticleComponent.setColorOuter(new Vec4f(newColor, 1f));
			}
		}

		if (InputManager.getInstance().getKeyDown(GLFW.GLFW_KEY_SPACE)) {
			Vec3f cameraForward = cameraControls.getCamera().getForward();
			float zDelta = cameraControls.getCamera().getPosition().data[2] / cameraForward.data[2];
			Vec3f spawnPosition = cameraForward.copy().apply(Operator.Mul, -zDelta).apply(Operator.Add, cameraControls.getCamera().getPosition());
			Entity shockwaveEntity = EntityRegistry.getInstance().createEntity();
			shockwaveEntity.shockwaveComponent = new Shockwave(0f, -0.16f, 8f);
			shockwaveEntity.shockwaveExpandingAnimator = new ShockwaveExpandingAnimator(0.9f, 8f, -0.16f, 2f);
			shockwaveEntity.transform = new Transform(spawnPosition, Quaternion.identity(), new Vec3f(1f));
		}

		cameraControls.update();
	}

	@Override
	public void onExit() {
		EntityRegistry.getInstance().eraseEntity(woodEntity);
		EntityRegistry.getInstance().eraseEntity(bwCheckerEntity);
		EntityRegistry.getInstance().eraseEntity(shipEntity);
		EntityRegistry.getInstance().eraseEntity(sphereEntity);
		for (Entity activeLight : activeLights) {
			EntityRegistry.getInstance().eraseEntity(activeLight);
		}
		if (xyPlaneMeshData != null) {
			xyPlaneMeshData.delete();
		}
		if (starSparrowMeshData != null) {
			starSparrowMeshData.delete();
		}
		if (smoothSphereInvertedMeshData != null) {
			smoothSphereInvertedMeshData.delete();
		}
		ITexture.deleteTexture(woodTexture);
		ITexture.deleteTexture(woodPhong);
		ITexture.deleteTexture(woodNormal);
		ITexture.deleteTexture(woodHeightMap);
		ITexture.deleteTexture(bwCheckerTexture);
		ITexture.deleteTexture(starSparrowTexture);
		ITexture.deleteTexture(starSparrowPhong);
		ITexture.deleteTexture(starSparrowNormal);
		super.onExit();
	}

	@Override
	public void printControls() {
		super.printControls();
		logger.info("- WASD + EQ = Camera Movement");
		logger.info("- Mouse = Camera Yaw/Pitch");
		logger.info("- press [Space] to Spawn a Shockwave");
		logger.info("- press [F] to invert all light-source colors");
		logger.info("- press [1] to spawn a spotlight at your position");
		logger.info("- press [2] to destroy the spotlight with least distance to your position");
		logger.info("- press [3] to exit this state");
	}

	@Override
	public void loadEntities() {
		woodEntity = EntityRegistry.getInstance().createEntity();
		woodEntity.transform = new Transform(Vec3f.zero(), Quaternion.identity(), new Vec3f(1f));
		woodEntity.meshComponent = new Mesh(xyPlaneMeshData, woodTexture, woodPhong, woodNormal, woodHeightMap);

		bwCheckerEntity = EntityRegistry.getInstance().createEntity();
		bwCheckerEntity.transform = new Transform(new Vec3f(-3f, 0f, 0f), Quaternion.identity(), new Vec3f(1f));
		bwCheckerEntity.meshComponent = new Mesh(xyPlaneMeshData, bwCheckerTexture, null, null, null);

		shipEntity = EntityRegistry.getInstance().createEntity();
		shipEntity.transform = new Transform(new Vec3f(10f, 0f, 0f), Quaternion.identity(), new Vec3f(1f));
		shipEntity.meshComponent = new Mesh(starSparrowMeshData, starSparrowTexture, starSparrowPhong, starSparrowNormal, null);

		sphereEntity = EntityRegistry.getInstance().createEntity();
		sphereEntity.transform = new Transform(new Vec3f(-12f, 0f, 0f), QuaternionMathOOP.eulerDeg(new Vec3f(0f, 0f, 0f)), new Vec3f(7f));
		sphereEntity.meshComponent = new Mesh(smoothSphereInvertedMeshData, null, null, null, null);
	}
}
