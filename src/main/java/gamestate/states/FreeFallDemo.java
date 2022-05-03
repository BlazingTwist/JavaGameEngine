package gamestate.states;

import ecs.Entity;
import ecs.EntityRegistry;
import ecs.components.Light;
import ecs.components.Mesh;
import ecs.components.Transform;
import ecs.components.Velocity;
import gamestate.DefaultGameState;
import gamestate.Time;
import java.util.ArrayList;
import java.util.Random;
import org.lwjgl.glfw.GLFW;
import rendering.mesh.MeshData;
import rendering.texture.Sampler;
import rendering.texture.Texture2D;
import utils.input.InputManager;
import utils.operator.Operator;
import utils.quaternion.Quaternion;
import utils.quaternion.QuaternionMathOOP;
import utils.vector.Vec3f;

public class FreeFallDemo extends DefaultGameState {

	private final MeshData sphereMeshData = MeshData.loadFromFile("models/sphere.obj", false);
	private final Texture2D planetTexture = Texture2D.fromResource("textures/planet1.png", Sampler.linearMirroredSampler);
	private final Texture2D planetPhong = Texture2D.fromResource("textures/Planet1_phong.png", Sampler.linearMirroredSampler);

	private final Random random = new Random();

	private final ArrayList<Entity> fallingEntities = new ArrayList<>();
	private double nextEntitySpawnInSeconds = 0;

	private Entity lightSource = null;

	public FreeFallDemo() {
		super("Free Fall Demo", new Vec3f(1.4f, 1.4f, 1.4f), new Vec3f(0f, 0f, -7f),
				true, false, false, false);
		super.init();
	}

	private void checkObjectSpawnTimer() {
		nextEntitySpawnInSeconds -= Time.physicsDeltaSeconds;
		if (nextEntitySpawnInSeconds <= 0) {
			nextEntitySpawnInSeconds = 0.25;

			Vec3f position = new Vec3f(random.nextFloat(-4f, 4f), 15, random.nextFloat(-4f, 4f));
			Vec3f scale = new Vec3f(1f).apply(Operator.Mul, random.nextFloat(0.1f, 1.5f));
			Entity entity = EntityRegistry.getInstance().createEntity();
			entity.transform = new Transform(position, Quaternion.identity(), scale);
			entity.meshComponent = new Mesh(sphereMeshData, planetTexture, planetPhong, null, null);
			entity.velocityComponent = new Velocity();
			fallingEntities.add(entity);
		}
	}

	@Override
	public void update() {
		if (InputManager.getInstance().getKeyDown(GLFW.GLFW_KEY_1)) {
			logger.info("exiting Free Fall Demo state");
			isFinished = true;
			return;
		}

		final Vec3f gravityAcceleration = new Vec3f(0f, -1f, 0f);
		for (int i = fallingEntities.size() - 1; i >= 0; i--) {
			Entity entity = fallingEntities.get(i);
			entity.velocityComponent.applyAccelerationWithPositionUpdate(gravityAcceleration, entity.transform);
			if (entity.transform.getPosition().data[1] <= -20f) {
				EntityRegistry.getInstance().eraseEntity(entity);
				fallingEntities.remove(i);
			}
		}
		checkObjectSpawnTimer();
	}

	@Override
	public void onExit() {
		for (Entity fallingEntity : fallingEntities) {
			EntityRegistry.getInstance().eraseEntity(fallingEntity);
		}
		fallingEntities.clear();
		EntityRegistry.getInstance().eraseEntity(lightSource);
		if (sphereMeshData != null) {
			sphereMeshData.delete();
		}
		if (planetTexture != null) {
			planetTexture.deleteTexture();
		}
		if (planetPhong != null) {
			planetPhong.deleteTexture();
		}
		super.onExit();
	}


	@Override
	public void printControls() {
		super.printControls();
		logger.info("- press [1] to exit this state");
	}

	@Override
	public void loadEntities() {
		lightSource = EntityRegistry.getInstance().createEntity();
		lightSource.lightComponent = Light.directional(new Vec3f(1f, 1f, 0.8f), 0.75f);
		lightSource.transform = new Transform(Vec3f.zero(), QuaternionMathOOP.eulerDeg(new Vec3f(45f, -60f, 0f)), new Vec3f(1f));
	}
}
