package gamestate.states;

import ecs.Entity;
import ecs.EntityRegistry;
import ecs.UpdateSystem;
import ecs.components.AABBCollider;
import ecs.components.Light;
import ecs.components.Mesh;
import ecs.components.RotationalVelocity;
import ecs.components.SphereParticle;
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
import utils.collision.AxisAlignedBoundingBox;
import utils.collision.ITreeProcessor;
import utils.collision.OcTree;
import utils.input.InputManager;
import utils.operator.Operator;
import utils.quaternion.Quaternion;
import utils.quaternion.QuaternionMathOOP;
import utils.vector.Vec3f;
import utils.vector.Vec4f;

public class CollisionDemo extends DefaultGameState {

	public static final float boxSize = 20f;

	private final MeshData sphereMeshData = MeshData.loadFromFile("models/sphere.obj", false);
	private final Texture2D planetTexture = Texture2D.fromResource("textures/planet1.png", Sampler.linearMirroredSampler);
	private final Texture2D planetPhong = Texture2D.fromResource("textures/Planet1_phong.png", Sampler.linearMirroredSampler);

	private final Random random = new Random();
	private final OcTree<Entity> collisionTree = new OcTree<>(16f);

	private final ArrayList<Entity> targetEntities = new ArrayList<>();
	private double nextTargetSpawnInSeconds = 0;
	private final ArrayList<Entity> projectileEntities = new ArrayList<>();
	private double bulletCoolDownSeconds = 0;

	private Entity lightSource = null;

	public CollisionDemo() {
		super("CollisionDemo", new Vec3f(1.4f, 1.4f, 1.4f), new Vec3f(0f, 0f, -7f),
				true, true, false, false);
		super.init();
	}

	private void createBullet() {
		Vec3f spawnPosition = cameraControls.getCamera().getPosition().copy();
		Vec3f bulletDirection = cameraControls.getCamera().getForward().copy();
		Entity bullet = EntityRegistry.getInstance().createEntity();
		bullet.transform = new Transform(spawnPosition, Quaternion.identity(), new Vec3f(0.1f));
		bullet.sphereParticleComponent = new SphereParticle(-0.1f, new Vec4f(1.6f, 1.6f, 0f, 1f), new Vec4f(0.7f, 0.8f, 0f, 1f));
		bullet.aabbCollider = AABBCollider.unitBounds();
		bullet.velocityComponent = new Velocity(bulletDirection.apply(Operator.Mul, 10f));
		projectileEntities.add(bullet);
	}

	private void checkSpawnTarget() {
		if (targetEntities.size() >= 20) {
			return;
		}

		nextTargetSpawnInSeconds -= Time.physicsDeltaSeconds;
		if (nextTargetSpawnInSeconds <= 0) {
			nextTargetSpawnInSeconds = 1f;
			Vec3f position = new Vec3f(random.nextInt(0, 10), random.nextInt(0, 10), random.nextInt(0, 10));
			Vec3f direction = new Vec3f(random.nextInt(-4, 4), random.nextInt(-4, 4), random.nextInt(-4, 4));
			Vec3f angularVelocity = new Vec3f(random.nextFloat(-1, 1), random.nextFloat(-1, 1), random.nextFloat(-1, 1));
			Entity targetEntity = EntityRegistry.getInstance().createEntity();
			targetEntity.transform = new Transform(position, Quaternion.identity(), new Vec3f(1f));
			targetEntity.meshComponent = new Mesh(sphereMeshData, planetTexture, planetPhong, null, null);
			targetEntity.velocityComponent = new Velocity(direction);
			targetEntity.rotationalVelocity = new RotationalVelocity(angularVelocity);
			targetEntity.aabbCollider = AABBCollider.unitBounds();
			targetEntities.add(targetEntity);
		}
	}

	@Override
	public void update() {
		if (InputManager.getInstance().getKeyDown(GLFW.GLFW_KEY_1)) {
			logger.info("exiting Collision Demo state");
			isFinished = true;
			return;
		}

		if (!targetEntities.isEmpty()) {
			EntityRegistry.getInstance().executeUpdateSystem(new UpdateSystem() {
				@Override
				public boolean isTargetEntity_updateTick(Entity entity) {
					return entity.transform != null && entity.velocityComponent != null;
				}

				@Override
				public void execute_updateTick(Entity entity) {
					Vec3f pos = entity.transform.getPosition();
					for (int i = 0; i < Vec3f.DATA_LEN; i++) {
						if (pos.data[i] < -boxSize || pos.data[i] > boxSize) {
							Vec3f velocity = entity.velocityComponent.getVelocity();
							velocity.data[i] = -velocity.data[i];
						}
					}
				}

				@Override
				public void onExecuteUpdateDone() {
				}
			});

			for (Entity targetEntity : targetEntities) {
				collisionTree.insert(targetEntity.aabbCollider.getAABB(targetEntity.transform), targetEntity);
			}
			for (Entity projectileEntity : projectileEntities) {
				AxisAlignedBoundingBox bulletAABB = projectileEntity.aabbCollider.getAABB(projectileEntity.transform);
				collisionTree.traverse(new ITreeProcessor<>() {
					@Override
					public boolean descend(AxisAlignedBoundingBox nodeBox) {
						return nodeBox.isIntersecting(bulletAABB);
					}

					@Override
					public void process(Entity hit) {
						if (hit.aabbCollider.getOldAABB().isIntersecting(bulletAABB)) {
							EntityRegistry.getInstance().enqueueEraseEntity(hit);
						}
					}
				});
			}
			collisionTree.clear();
			targetEntities.removeIf(Entity::isExpired);
		}

		checkSpawnTarget();
		cameraControls.update();

		if (bulletCoolDownSeconds > 0) {
			bulletCoolDownSeconds -= Time.physicsDeltaSeconds;
		} else if (InputManager.getInstance().getMouseButton(GLFW.GLFW_MOUSE_BUTTON_RIGHT)) {
			bulletCoolDownSeconds = 0.0;
			createBullet();
		}
	}

	@Override
	public void onExit() {
		for (Entity targetEntity : targetEntities) {
			EntityRegistry.getInstance().eraseEntity(targetEntity);
		}
		for (Entity projectileEntity : projectileEntities) {
			EntityRegistry.getInstance().eraseEntity(projectileEntity);
		}
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
		logger.info("- WASD + EQ = Camera Movement");
		logger.info("- Mouse = Camera Yaw/Pitch");
		logger.info("- RightClick to spawn a projectile in view direction");
		logger.info("- press [1] to exit this state");
	}

	@Override
	public void loadEntities() {
		lightSource = EntityRegistry.getInstance().createEntity();
		lightSource.transform = new Transform(Vec3f.zero(), QuaternionMathOOP.eulerDeg(new Vec3f(45f, -60f, 0f)), new Vec3f(1f));
		lightSource.lightComponent = Light.directional(new Vec3f(1f, 1f, 0.8f), 0.75f);
	}
}
