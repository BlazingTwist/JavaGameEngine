package gamestate.states;

import ecs.Entity;
import ecs.EntityRegistry;
import ecs.components.Light;
import ecs.components.MarchingCubesMesh;
import ecs.components.Mesh;
import ecs.components.SphereParticle;
import ecs.components.Transform;
import gamestate.DefaultGameState;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.glfw.GLFW;
import rendering.marchingcubes.MarchingCubesGrid3D;
import rendering.mesh.MeshData;
import utils.input.InputManager;
import utils.noise.threedim.INoiseLayer3D;
import utils.noise.NoiseGenerator;
import utils.noise.threedim.PerlinLayer3D;
import utils.noise.threedim.PlanetLayer3D;
import utils.noise.threedim.SphereLayer3D;
import utils.quaternion.Quaternion;
import utils.vector.Vec3f;
import utils.vector.Vec4f;

public class MarchingCubesDemo extends DefaultGameState {

	private final SphereLayer3D ridgeLayer = new SphereLayer3D(1.0f, 2f, -10f, 10f, 0f);
	private final SphereLayer3D continentLayer = new SphereLayer3D(0.75f, 1f, -10f, 10f, 0f);
	private final SphereLayer3D oceanLayer = new SphereLayer3D(0.4f, 1f, -1f, 1f, 0f);
	private final PerlinLayer3D[] oceanLayers = new PerlinLayer3D[]{
			new PerlinLayer3D(5.3f, 39.55f, 2.55f, 6.5f, -3.0f, 2.0f, 0f),
			new PerlinLayer3D(13.15f, 2.7f, 1.5f, 0.15f, -0.1f, 0.1f, 0f),
			new PerlinLayer3D(19.05f, 4.3f, 3f, 4.85f, -0.2f, 0.2f, 0f)
	};
	private final PerlinLayer3D[] ridgeLayers = new PerlinLayer3D[]{
			new PerlinLayer3D(5.3f, 43.55f, 5.55f, 6.5f, -0.7f, 0.7f, 0f),
			new PerlinLayer3D(13.15f, 4.7f, 3.5f, 0.15f, -0.5f, 0.5f, 0f),
			new PerlinLayer3D(19.05f, 8.3f, 9f, 4.85f, -0.3f, 0.3f, 0f)
	};
	private final PerlinLayer3D[] maskLayers = new PerlinLayer3D[]{
			new PerlinLayer3D(3.3f, 10f, 11f, 12f, 0f, 1f, 0f)
	};
	private final PlanetLayer3D planetLayer = new PlanetLayer3D(List.of(oceanLayers), List.of(maskLayers), List.of(ridgeLayers), ridgeLayer, continentLayer, oceanLayer);

	private int currentPerlinLayerIndex = 0;
	private PerlinLayer3D currentPerlinLayer;
	private boolean forceManifold = false;

	private Entity marchingCubesEntity = null;
	private Entity lightEntity = null;

	public MarchingCubesDemo() {
		super("MarchingCubesDemo", new Vec3f(1.4f, 1.4f, 1.4f), new Vec3f(0f, 0f, -3f),
				false, false, false, true);
		currentPerlinLayer = maskLayers[0];

		super.init();
	}

	private void generateMesh() {
		NoiseGenerator.generate3D(marchingCubesEntity.marchingCubesMesh.voxelGrid(), planetLayer);
		marchingCubesEntity.marchingCubesMesh.voxelGrid().setDirty();
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
		if (inputManager.getKey(GLFW.GLFW_KEY_UP)) {
			logger.info("+bumping voxel values by 0.01");
			regenPerlin = true;
			oceanLayers[0].valueOffset += 0.03f;
		}
		if (inputManager.getKey(GLFW.GLFW_KEY_DOWN)) {
			logger.info("-bumping voxel values by 0.01");
			regenPerlin = true;
			oceanLayers[0].valueOffset -= 0.03f;
		}
		if (inputManager.getKeyDown(GLFW.GLFW_KEY_C)) {
			forceManifold = !forceManifold;
			regenPerlin = true;
			logger.info("force-manifold is now: {}", forceManifold);
		}
		if (inputManager.getKeyDown(GLFW.GLFW_KEY_LEFT)) {
			int numLayers = oceanLayers.length;
			currentPerlinLayerIndex = (currentPerlinLayerIndex + (numLayers - 1)) % numLayers;
			currentPerlinLayer = oceanLayers[currentPerlinLayerIndex];
			logger.info("now editing layer: {}", currentPerlinLayerIndex);
		}
		if (inputManager.getKeyDown(GLFW.GLFW_KEY_RIGHT)) {
			int numLayers = oceanLayers.length;
			currentPerlinLayerIndex = (currentPerlinLayerIndex + 1) % numLayers;
			currentPerlinLayer = oceanLayers[currentPerlinLayerIndex];
			logger.info("now editing layer: {}", currentPerlinLayerIndex);
		}

		boolean upScale = inputManager.getKey(GLFW.GLFW_KEY_R);
		boolean downScale = inputManager.getKey(GLFW.GLFW_KEY_F);
		boolean upX = inputManager.getKey(GLFW.GLFW_KEY_L);
		boolean downX = inputManager.getKey(GLFW.GLFW_KEY_J);
		boolean upY = inputManager.getKey(GLFW.GLFW_KEY_O);
		boolean downY = inputManager.getKey(GLFW.GLFW_KEY_U);
		boolean upZ = inputManager.getKey(GLFW.GLFW_KEY_I);
		boolean downZ = inputManager.getKey(GLFW.GLFW_KEY_K);
		boolean upMinValue = inputManager.getKey(GLFW.GLFW_KEY_T);
		boolean downMinValue = inputManager.getKey(GLFW.GLFW_KEY_G);
		boolean upMaxValue = inputManager.getKey(GLFW.GLFW_KEY_Y);
		boolean downMaxValue = inputManager.getKey(GLFW.GLFW_KEY_H);
		if (upScale || downScale) {
			currentPerlinLayer.scale += (downScale ? (-0.05f) : 0.05f);
			logger.info("layer scale now is: {}", currentPerlinLayer.scale);
			regenPerlin = true;
		}
		if (upX || downX) {
			currentPerlinLayer.xOffset += (downX ? (-0.05f) : 0.05f);
			logger.info("layer xOffset now is: {}", currentPerlinLayer.xOffset);
			regenPerlin = true;
		}
		if (upY || downY) {
			currentPerlinLayer.yOffset += (downY ? (-0.05f) : 0.05f);
			logger.info("layer yOffset now is: {}", currentPerlinLayer.yOffset);
			regenPerlin = true;
		}
		if (upZ || downZ) {
			currentPerlinLayer.zOffset += (downZ ? (-0.05f) : 0.05f);
			logger.info("layer zOffset now is: {}", currentPerlinLayer.zOffset);
			regenPerlin = true;
		}
		if (upMinValue || downMinValue) {
			currentPerlinLayer.minValue += (downMinValue ? (-0.05f) : 0.05f);
			logger.info("layer minValue now is: {}", currentPerlinLayer.minValue);
			regenPerlin = true;
		}
		if (upMaxValue || downMaxValue) {
			currentPerlinLayer.maxValue += (downMaxValue ? (-0.05f) : 0.05f);
			logger.info("layer maxValue now is: {}", currentPerlinLayer.maxValue);
			regenPerlin = true;
		}
		if (inputManager.getKeyDown(GLFW.GLFW_KEY_KP_0)) {
			ArrayList<INoiseLayer3D> layers = new ArrayList<>();
			layers.add(continentLayer);
			layers.add(oceanLayer);
			layers.add(planetLayer);
			layers.addAll(List.of(oceanLayers));
			NoiseGenerator.printLayoutParameters3D(logger, layers);
		}
		if (inputManager.getKeyDown(GLFW.GLFW_KEY_KP_1)) {
			currentPerlinLayer.toggle();
			logger.info("layer is now {}", currentPerlinLayer.enabled ? "enabled" : "disabled");
			regenPerlin = true;
		}
		if(InputManager.getInstance().getKey(GLFW.GLFW_KEY_KP_7)){
			planetLayer.maskCutoff += 0.01f;
			regenPerlin = true;
		}
		if(InputManager.getInstance().getKey(GLFW.GLFW_KEY_KP_4)){
			planetLayer.maskCutoff -= 0.01f;
			regenPerlin = true;
		}
		if(InputManager.getInstance().getKey(GLFW.GLFW_KEY_KP_8)){
			planetLayer.ridgeCutoff += 0.01f;
			regenPerlin = true;
		}
		if(InputManager.getInstance().getKey(GLFW.GLFW_KEY_KP_5)){
			planetLayer.ridgeCutoff -= 0.01f;
			regenPerlin = true;
		}
		if (regenPerlin) {
			generateMesh();
		}
	}

	@Override
	public void onExit() {
		EntityRegistry.getInstance().eraseEntity(marchingCubesEntity);
		EntityRegistry.getInstance().eraseEntity(lightEntity);
		super.onExit();
	}

	@Override
	public void printControls() {
		super.printControls();
		logger.info("- WASD + EQ = Camera Movement");
		logger.info("- Mouse     = Camera Yaw/Pitch");
		logger.info("- [1]       = Exit");
		logger.info("- ===== Global Perlin Parameters =====");
		logger.info("- hold  [UP]    to increase every voxel value");
		logger.info("- hold  [DOWN]  to decrease every voxel value");
		logger.info("- press [C]     to toggle force-manifolding of shape");
		logger.info("- ===== Layer Perlin Parameters =====");
		logger.info("- press [LEFT]  to select previous layer");
		logger.info("- press [RIGHT] to select next layer");
		logger.info("- hold  R/F     to +/- modify layer scale");
		logger.info("- hold  L/J     to +/- modify layer xOffset");
		logger.info("- hold  O/U     to +/- modify layer yOffset");
		logger.info("- hold  I/K     to +/- modify layer zOffset");
		logger.info("- hold  T/G     to +/- modify layer minValue");
		logger.info("- hold  Z/H     to +/- modify layer maxValue");
		logger.info("- press [KP_0]  to print all layer parameters");
	}

	@Override
	public void loadEntities() {
		marchingCubesEntity = EntityRegistry.getInstance().createEntity();
		marchingCubesEntity.transform = new Transform(new Vec3f(0f, 0f, 0f), Quaternion.identity(), new Vec3f(15f));
		marchingCubesEntity.marchingCubesMesh = new MarchingCubesMesh(
				new MarchingCubesGrid3D(35, 35, 35),
				new Vec4f(0.2f, 0.8f, 0.075f, 0.05f)
		);
		generateMesh();

		for (int x = -5; x <= 5; x++) {
			for (int y = -5; y <= 5; y++) {
				for (int z = -5; z <= 5; z++) {
					Entity entity = EntityRegistry.getInstance().createEntity();
					entity.transform = new Transform(new Vec3f(x, y, z), Quaternion.identity(), new Vec3f(1f));
					entity.sphereParticleComponent = new SphereParticle(0.01f, new Vec4f(1f), new Vec4f(1f));
				}
			}
		}

		Entity sphereEntity = EntityRegistry.getInstance().createEntity();
		sphereEntity.transform = new Transform(new Vec3f(0f), Quaternion.identity(), new Vec3f(4.3f));
		sphereEntity.meshComponent = new Mesh(MeshData.loadFromFile("models/midPolyUVSphereSmoothed.obj", false), null, null, null, null);

		lightEntity = EntityRegistry.getInstance().createEntity();
		lightEntity.transform = new Transform(Vec3f.zero(), Quaternion.identity(), new Vec3f(1f));
		lightEntity.lightComponent = Light.directional(new Vec3f(1.0f, 0.9f, 0.7f), 1.7f);
	}
}
