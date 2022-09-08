package gamestate.states;

import gamestate.BaseGameState;
import gamestate.DefaultGameState;
import gamestate.states.perlindemo.NebulaLayer;
import gamestate.states.perlindemo.NebulaMath;
import gamestate.states.perlindemo.ProgressReporter;
import gamestate.states.perlindemo.RGBFloatTexture;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import logging.LogbackLoggerProvider;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import rendering.programs.ImageRenderProgram;
import rendering.texture.Sampler;
import utils.MathF;
import utils.WindowInfo;
import utils.input.InputManager;
import utils.matrix.Mat4f;
import utils.matrix.MatrixArithmeticIP;
import utils.matrix.MatrixMath;
import utils.noise.NoiseGenerator;
import utils.noise.twodim.INoiseLayer2D;
import utils.noise.twodim.IVoxelGrid2D;
import utils.noise.twodim.LayerStack2D;
import utils.noise.twodim.PerlinLayer2D;
import utils.noise.twodim.VoxelGrid2D;
import utils.vector.Vec3f;
import utils.vector.Vec4f;
import utils.vector.VectorMath;

public class PerlinDemo extends BaseGameState {

	public static class Sun {
		public enum SunType {
			SolarStar(400, 1f, 1.25f, 4f),
			HotBlueStar(8, 1.2f, 1.25f, 15f),
			RedDwarf(200, 1.25f, 1.5f, 1f),
			RedGiant(18, 1.5f, 2f, 76f);

			public final int occurrenceValue;
			public final float colorRedShift;
			public final float fallOffIntensity;
			public final float referenceRadius;
			public final Color coreColor;
			public final Color lightColor;

			SunType(int occurrenceValue, float colorRedShift, float fallOffIntensity, float referenceRadius) {
				this.occurrenceValue = occurrenceValue;
				this.colorRedShift = colorRedShift;
				this.fallOffIntensity = fallOffIntensity;
				this.referenceRadius = referenceRadius;
				coreColor = getStarlightColor(colorRedShift, fallOffIntensity * 0.6f);
				lightColor = getStarlightColor(colorRedShift, fallOffIntensity);
			}
		}

		public static Color getStarlightColor(float redShiftFactor01, float fallOffIntensity) {
			final float blueWavelength = 450f;
			final float greenWavelength = 550f;
			final float redWavelength = 700f;
			final float squishFactor = 1250f / fallOffIntensity;
			final float s1 = MathF.lerpUnclamped(2f * blueWavelength, 2f * redWavelength, redShiftFactor01);

			Function<Float, Float> getWaveLengthIntensity = (wavelength) -> {
				float horizontalShiftFactor = s1 * wavelength;
				float quadraticTerm = (0f - (wavelength * wavelength)) + (0f - (s1 * s1 * 0.25f));
				return (100f + (horizontalShiftFactor + quadraticTerm) / squishFactor) / 100f;
			};

			return new Color(
					MathF.clamp01(getWaveLengthIntensity.apply(redWavelength)),
					MathF.clamp01(getWaveLengthIntensity.apply(greenWavelength)),
					MathF.clamp01(getWaveLengthIntensity.apply(blueWavelength))
			);
		}

		private static final Random random = new Random();

		public SunType sunType;
		public float lightIntensity;
		public Vec3f position;
		public float radius;

		public Sun(SunType sunType, float lightIntensity, Vec3f position) {
			this.sunType = sunType;
			this.lightIntensity = lightIntensity;
			this.position = position;
			this.radius = (random.nextFloat() + 0.5f) * sunType.referenceRadius;
		}
	}

	private static final int xResolution = WindowInfo.getInstance().getWindowWidth();
	private static final int yResolution = WindowInfo.getInstance().getWindowHeight();

	protected static final Logger logger = LogbackLoggerProvider.getLogger(DefaultGameState.class);
	protected static final String stateName = "PerlinDemo";

	protected final ImageRenderProgram imageRenderProgram;
	protected final RGBFloatTexture rgbTexture;
	protected final IVoxelGrid2D voxelGrid;

	private final LayerStack2D layerStack2D = new LayerStack2D();
	private final List<NebulaLayer> nebulaLayers = new ArrayList<>();
	private final List<Sun> stars = new ArrayList<>();

	public PerlinDemo() {
		imageRenderProgram = new ImageRenderProgram();
		rgbTexture = new RGBFloatTexture(Sampler.linearMirroredSampler, xResolution, yResolution);
		int maxRes = Math.max(xResolution, yResolution);
		voxelGrid = new VoxelGrid2D(maxRes * 2, maxRes * 2);

		nebulaLayers.add(new NebulaLayer(100f, 1f, new Color(57, 19, 84), new Color(194, 48, 192), new Color(169, 35, 255)));
		nebulaLayers.add(new NebulaLayer(200f, 0.9f, new Color(19, 52, 84), new Color(48, 194, 99), new Color(52, 182, 160)));
		nebulaLayers.add(new NebulaLayer(300f, 0.75f, new Color(60, 84, 19), new Color(255, 244, 40), new Color(178, 70, 51)));
		nebulaLayers.add(new NebulaLayer(400f, 0.5f, new Color(84, 24, 19), new Color(194, 92, 48), new Color(255, 35, 101)));
		nebulaLayers.sort((a, b) -> Float.compare(b.distance, a.distance));

		generateStars();
		recomputeTexture();
		printControls();
	}

	public void generateStars() {
		final int xQuadrants = 0;
		final int numStars = 96;
		final int positionRetries = 5;

		Random random = new Random();

		List<Sun.SunType> sunTypesByRadiusDescending = Arrays.stream(Sun.SunType.values())
				.sorted((a, b) -> Float.compare(b.referenceRadius, a.referenceRadius))
				.collect(Collectors.toList());
		int sunSum = sunTypesByRadiusDescending.stream().map(sun -> sun.occurrenceValue).reduce(0, Integer::sum);
		float[] sunSpawnFactor = new float[sunTypesByRadiusDescending.size()];
		for (int i = 0; i < sunTypesByRadiusDescending.size(); i++) {
			Sun.SunType sunType = sunTypesByRadiusDescending.get(i);
			sunSpawnFactor[i] = (i == 0 ? 0 : sunSpawnFactor[i - 1]) + ((float) sunType.occurrenceValue / sunSum);
		}
		logger.info("sunSpawnFactors: {}", Arrays.toString(sunSpawnFactor));

		HashMap<Sun.SunType, Integer> spawnedSuns = new HashMap<>();
		Vec3f position = new Vec3f(0f);

		for (int quadrant = -xQuadrants; quadrant <= xQuadrants; quadrant++) {
			for (int i = 0; i < numStars; i++) {

				Sun.SunType sunSpawn = null;
				float randomVal = random.nextFloat();
				for (int sunIndex = 0; sunIndex < sunSpawnFactor.length; sunIndex++) {
					if (randomVal <= sunSpawnFactor[sunIndex]) {
						sunSpawn = sunTypesByRadiusDescending.get(sunIndex);
						break;
					}
				}

				if (sunSpawn == null) {
					continue;
				}

				boolean positionFound = false;
				for (int retry = 0; retry < positionRetries; retry++) {
					float z = MathF.squared(5f + sunSpawn.referenceRadius + random.nextFloat() * 100f);
					position.set((random.nextFloat() - 0.5f) * z * 3f + z * quadrant, (random.nextFloat() - 0.5f) * z * 1.5f, z + 1000);

					float closestStarDistanceSquared = Float.POSITIVE_INFINITY;
					for (Sun star : stars) {
						closestStarDistanceSquared = Math.min(closestStarDistanceSquared, position.distanceSquared(star.position) - star.radius);
					}

					if (closestStarDistanceSquared >= MathF.squared(10f * sunSpawn.referenceRadius)) {
						positionFound = true;
						break;
					}
				}

				if (!positionFound) {
					continue;
				}

				stars.add(new Sun(sunSpawn, 1f, position.copy()));
				spawnedSuns.put(sunSpawn, spawnedSuns.getOrDefault(sunSpawn, 0) + 1);
			}
		}

		logger.info("generated {} stars", stars.size());
		for (Map.Entry<Sun.SunType, Integer> spawnEntry : spawnedSuns.entrySet()) {
			logger.info("spawned {} {}", spawnEntry.getValue(), spawnEntry.getKey().name());
		}
	}

	public void printControls() {
		logger.info("{} Controls: ", stateName);
		logger.info("- press [1] to exit");
		logger.info("===== Layer Management =====");
		logger.info("- press [KP_0]  to print all layer parameters");
		logger.info("- press [KP_1]  save to image");
		logger.info("===== Modify Layer =====");
		logger.info("- hold  E/Q     to +/- modify layer scale");
		logger.info("- hold  D/A     to +/- modify layer xOffset");
		logger.info("- hold  W/S     to +/- modify layer yOffset");
	}

	public void recomputeTexture() {
		rgbTexture.setColor(new Color(4, 16, 31));

		Mat4f inversePerspective = MatrixMath.inversePerspective(50f, (float) xResolution / yResolution, 1f, 300_000f);
		Vec3f cameraPosition = Vec3f.zero();
		final float halfXRes = xResolution / 2f;
		final float halfYRes = yResolution / 2f;

		// sort by distance to camera descending
		stars.sort((a, b) -> Float.compare(b.position.lengthSquared(), a.position.length()));

		NebulaLayer nebulaLayer = nebulaLayers.get(0);
		nebulaLayer.prepareCompute(voxelGrid);
		Vec3f nebulaPlanePoint = new Vec3f(0f, 0f, 100f);
		Vec3f nebulaPlaneNormal = new Vec3f(0f, 0f, -1f);

		long startTime = System.currentTimeMillis();

		ProgressReporter progressReporter = new ProgressReporter(xResolution * yResolution);

		IntStream.range(0, xResolution).parallel()
				.forEach(x -> {
					Vec4f viewDirVector = new Vec4f();
					Vec3f viewDirNormal = new Vec3f();

					for (int y = 0; y < yResolution; y++) {
						viewDirVector.set((x - halfXRes) / halfXRes, (y - halfYRes) / halfYRes, 1f, 1f);
						MatrixArithmeticIP.mul(inversePerspective, viewDirVector);
						viewDirNormal.set(viewDirVector.data).normalize();

						Vec3f nebulaPoint = VectorMath.intersectionPlaneLine(nebulaPlanePoint, nebulaPlaneNormal, cameraPosition, viewDirNormal);
						assert nebulaPoint != null;

						float distanceNebulaClosestStar = Float.POSITIVE_INFINITY;
						for (Sun star : stars) {
							distanceNebulaClosestStar = Float.min(distanceNebulaClosestStar, nebulaPoint.distance(star.position) - star.radius);

							float distanceToStar = VectorMath.distanceLinePoint(cameraPosition, viewDirNormal, star.position);
							if (distanceToStar <= star.radius) {
								float distanceFactor = distanceToStar / star.radius;
								rgbTexture.setColor(x, y, MathF.lerpColor(star.sunType.coreColor, star.sunType.lightColor, distanceFactor));
							}
						}

						float nebulaValue = nebulaLayer.computeValue(
								(int) (nebulaPoint.data[0] * 100f),
								(int) (nebulaPoint.data[1] * 100f)
						);
						if (nebulaValue > 0f) {
							Color color = NebulaMath.inScatterLight(nebulaValue, rgbTexture.getColor(x, y), viewDirNormal, nebulaPoint, stars);
							rgbTexture.setColor(x, y, color);
						}

						synchronized (progressReporter) {
							progressReporter.stepComplete();
						}
					}
				});
		logger.info("drawing stars took: {} ms", (System.currentTimeMillis() - startTime));

		rgbTexture.recompute();
	}

	@Override
	public void update() {
		InputManager inputManager = InputManager.getInstance();
		if (inputManager.getKeyDown(GLFW.GLFW_KEY_1)) {
			logger.info("exiting {} state", stateName);
			isFinished = true;
			return;
		}

		if (inputManager.getKeyDown(GLFW.GLFW_KEY_KP_0)) {
			NoiseGenerator.printLayoutParameters2D(logger, layerStack2D.layers);
		}

		boolean regenPerlin = false;

		boolean upScale = inputManager.getKey(GLFW.GLFW_KEY_E);
		boolean downScale = inputManager.getKey(GLFW.GLFW_KEY_Q);
		boolean upX = inputManager.getKey(GLFW.GLFW_KEY_D);
		boolean downX = inputManager.getKey(GLFW.GLFW_KEY_A);
		boolean upY = inputManager.getKey(GLFW.GLFW_KEY_W);
		boolean downY = inputManager.getKey(GLFW.GLFW_KEY_S);
		if (upScale || downScale) {
			for (INoiseLayer2D layer : layerStack2D.layers) {
				if (layer instanceof PerlinLayer2D perlinLayer) {
					perlinLayer.scale *= (1f + (downScale ? (-0.05f) : 0.05f));
				}
			}
			regenPerlin = true;
		}
		if (upX || downX) {
			for (INoiseLayer2D layer : layerStack2D.layers) {
				if (layer instanceof PerlinLayer2D perlinLayer) {
					perlinLayer.xOffset += (downX ? (-0.05f) : 0.05f) * perlinLayer.scale;
				}
			}
			regenPerlin = true;
		}
		if (upY || downY) {
			for (INoiseLayer2D layer : layerStack2D.layers) {
				if (layer instanceof PerlinLayer2D perlinLayer) {
					perlinLayer.yOffset += (downY ? (-0.05f) : 0.05f) * perlinLayer.scale;
				}
			}
			regenPerlin = true;
		}
		if (inputManager.getKey(GLFW.GLFW_KEY_R)) {
			for (INoiseLayer2D layer : layerStack2D.layers) {
				if (layer instanceof PerlinLayer2D perlinLayer) {
					perlinLayer.xOffset += (-0.05f);
				}
			}
			regenPerlin = true;
		}
		if (inputManager.getKey(GLFW.GLFW_KEY_F)) {
			for (INoiseLayer2D layer : layerStack2D.layers) {
				if (layer instanceof PerlinLayer2D perlinLayer) {
					perlinLayer.xOffset += (0.05f);
				}
			}
			regenPerlin = true;
		}

		if (regenPerlin) {
			recomputeTexture();
		}
	}

	@Override
	public void draw() {
		imageRenderProgram.drawImage(rgbTexture);

		/*for (NebulaLayer nebulaLayer : nebulaLayers) {
			nebulaLayer.animate();
		}
		recomputeTexture();*/
	}

	@Override
	public void onResume() {
		printControls();
	}

	@Override
	public void onPause() {
		logger.info("===== {} State paused =====", stateName);
	}

	@Override
	public void onExit() {
		imageRenderProgram.delete();
	}
}
