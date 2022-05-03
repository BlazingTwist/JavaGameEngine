package gamestate.states;

import ecs.Entity;
import ecs.EntityRegistry;
import ecs.components.MarchingCubesMesh;
import ecs.components.Transform;
import gamestate.DefaultGameState;
import org.lwjgl.glfw.GLFW;
import rendering.marchingcubes.VoxelGrid3D;
import utils.input.InputManager;
import utils.noise.PerlinGenerator;
import utils.noise.PerlinParameters;
import utils.quaternion.Quaternion;
import utils.vector.Vec3f;
import utils.vector.Vec4f;

public class MarchingCubesDemo extends DefaultGameState {

	private final PerlinGenerator perlinGenerator = new PerlinGenerator(
			new PerlinParameters(1f, 1f, 0f, 0f, -1.5f, 1.5f, 0f),
			new PerlinParameters(4f, 1f, 0f, 0f, -1f, 1f, 0f),
			new PerlinParameters(10f, 1f, 0f, 0f, -0.5f, 0.5f, 0f)
	);

	private int currentPerlinLayerIndex = 0;
	private PerlinParameters currentPerlinLayer;
	private boolean forceManifold = false;

	private Entity marchingCubesEntity = null;

	public MarchingCubesDemo() {
		super("MarchingCubesDemo", new Vec3f(1.4f, 1.4f, 1.4f), new Vec3f(0f, 0f, -3f),
				false, false, false, true);
		super.init();
		currentPerlinLayer = perlinGenerator.layers.get(currentPerlinLayerIndex);
	}

	private void generateMesh() {
		perlinGenerator.generate(marchingCubesEntity.marchingCubesMesh.voxelGrid());
		if (forceManifold) {
			marchingCubesEntity.marchingCubesMesh.voxelGrid().makeManifold();
		}
	}

	@Override
	public void update() {
		InputManager inputManager = InputManager.getInstance();
		if (inputManager.getKeyDown(GLFW.GLFW_KEY_1)) {
			logger.info("exiting {} state", stateName);
			isFinished = true;
			return;
		}

		cameraControls.update();

		boolean regenPerlin = false;
		boolean invertPerlinKeys = inputManager.getKey(GLFW.GLFW_KEY_SPACE);
		if (inputManager.getKey(GLFW.GLFW_KEY_UP)) {
			logger.info("+bumping voxel values by 0.01");
			regenPerlin = true;
			perlinGenerator.layers.get(0).valueOffset += 0.03f;
		}
		if (inputManager.getKey(GLFW.GLFW_KEY_DOWN)) {
			logger.info("-bumping voxel values by 0.01");
			regenPerlin = true;
			perlinGenerator.layers.get(0).valueOffset -= 0.03f;
		}
		if (inputManager.getKeyDown(GLFW.GLFW_KEY_C)) {
			forceManifold = !forceManifold;
			regenPerlin = true;
			logger.info("force-manifold is now: {}", forceManifold);
		}
		if (inputManager.getKeyDown(GLFW.GLFW_KEY_LEFT)) {
			int numLayers = perlinGenerator.layers.size();
			currentPerlinLayerIndex = (currentPerlinLayerIndex + (numLayers - 1)) % numLayers;
			currentPerlinLayer = perlinGenerator.layers.get(currentPerlinLayerIndex);
			logger.info("now editing layer: {}", currentPerlinLayerIndex);
		}
		if (inputManager.getKeyDown(GLFW.GLFW_KEY_RIGHT)) {
			int numLayers = perlinGenerator.layers.size();
			currentPerlinLayerIndex = (currentPerlinLayerIndex + 1) % numLayers;
			currentPerlinLayer = perlinGenerator.layers.get(currentPerlinLayerIndex);
			logger.info("now editing layer: {}", currentPerlinLayerIndex);
		}

		if (inputManager.getKey(GLFW.GLFW_KEY_KP_8)) {
			currentPerlinLayer.scale += (invertPerlinKeys ? (-0.05f) : 0.05f);
			logger.info("layer scale now is: {}", currentPerlinLayer.scale);
			regenPerlin = true;
		}
		if (inputManager.getKey(GLFW.GLFW_KEY_KP_4)) {
			currentPerlinLayer.xOffset += (invertPerlinKeys ? (-0.05f) : 0.05f);
			logger.info("layer xOffset now is: {}", currentPerlinLayer.xOffset);
			regenPerlin = true;
		}
		if (inputManager.getKey(GLFW.GLFW_KEY_KP_5)) {
			currentPerlinLayer.yOffset += (invertPerlinKeys ? (-0.05f) : 0.05f);
			logger.info("layer yOffset now is: {}", currentPerlinLayer.yOffset);
			regenPerlin = true;
		}
		if (inputManager.getKey(GLFW.GLFW_KEY_KP_6)) {
			currentPerlinLayer.zOffset += (invertPerlinKeys ? (-0.05f) : 0.05f);
			logger.info("layer zOffset now is: {}", currentPerlinLayer.zOffset);
			regenPerlin = true;
		}
		if (inputManager.getKey(GLFW.GLFW_KEY_KP_7)) {
			currentPerlinLayer.minValue += (invertPerlinKeys ? (-0.05f) : 0.05f);
			logger.info("layer minValue now is: {}", currentPerlinLayer.minValue);
			regenPerlin = true;
		}
		if (inputManager.getKey(GLFW.GLFW_KEY_KP_9)) {
			currentPerlinLayer.maxValue += (invertPerlinKeys ? (-0.05f) : 0.05f);
			logger.info("layer maxValue now is: {}", currentPerlinLayer.maxValue);
			regenPerlin = true;
		}
		if (inputManager.getKeyDown(GLFW.GLFW_KEY_KP_0)) {
			perlinGenerator.printLayoutParameters(logger);
		}
		if (regenPerlin) {
			generateMesh();
		}
	}

	@Override
	public void onExit() {
		EntityRegistry.getInstance().eraseEntity(marchingCubesEntity);
	}

	@Override
	public void printControls() {
		super.printControls();
		logger.info("- WASD + EQ = Camera Movement");
		logger.info("- Mouse = Camera Yaw/Pitch");
		logger.info("- ===== Global Perlin Parameters =====");
		logger.info("- hold  [UP]    to increase every voxel value");
		logger.info("- hold  [DOWN]  to decrease every voxel value");
		logger.info("- press [C]     to toggle force-manifolding of shape");
		logger.info("- ===== Layer Perlin Parameters =====");
		logger.info("- press [LEFT]  to select previous layer");
		logger.info("- press [RIGHT] to select next layer");
		logger.info("- hold  [SPACE] to invert all perlin operations (decrease instead of increase)");
		logger.info("- hold  [KP_8]  to modify layer scale");
		logger.info("- hold  [KP_4]  to modify layer xOffset");
		logger.info("- hold  [KP_5]  to modify layer yOffset");
		logger.info("- hold  [KP_6]  to modify layer zOffset");
		logger.info("- hold  [KP_7]  to modify layer minValue");
		logger.info("- hold  [KP_9]  to modify layer maxValue");
		logger.info("- press [KP_0]  to print all layer parameters");
	}

	@Override
	public void loadEntities() {
		marchingCubesEntity = EntityRegistry.getInstance().createEntity();
		marchingCubesEntity.transform = new Transform(new Vec3f(-20f, 0f, 15f), Quaternion.identity(), new Vec3f(3f));
		marchingCubesEntity.marchingCubesMesh = new MarchingCubesMesh(
				new VoxelGrid3D(25, 25, 25),
				new Vec4f(0.2f, 0.5f, 0.25f, 0.1f)
		);
		generateMesh();
	}
}
