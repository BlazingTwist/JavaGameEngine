package gamestate.states;

import com.aparapi.Range;
import com.aparapi.device.Device;
import com.aparapi.internal.kernel.KernelManager;
import gamestate.BaseGameState;
import gamestate.DefaultGameState;
import gamestate.states.perlindemo.NebulaLayer;
import gamestate.states.perlindemo.NebulaMath;
import gamestate.states.perlindemo.PerlinDemoImageKernel;
import gamestate.states.perlindemo.ProgressReporter;
import gamestate.states.perlindemo.RGBFloatTexture;
import java.awt.Color;
import java.io.IOException;
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
import utils.ImageUtils;
import utils.MathF;
import utils.WindowInfo;
import utils.input.InputManager;
import utils.matrix.Mat4f;
import utils.matrix.MatrixArithmeticIP;
import utils.matrix.MatrixMath;
import utils.noise.NoiseGenerator;
import utils.noise.twodim.IGridDimensions2D;
import utils.noise.twodim.INoiseLayer2D;
import utils.noise.twodim.LayerStack2D;
import utils.noise.twodim.PerlinLayer2D;
import utils.vector.Vec3f;
import utils.vector.Vec4f;
import utils.vector.VectorMath;

public class PerlinDemo extends BaseGameState {

	public static class Sun {
		public enum SunType {
			SolarStar(400, 1f, 1.25f, 4f),
			HotBlueStar(8, 0f, 1.25f, 15f),
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
				coreColor = getStarlightColor(colorRedShift, fallOffIntensity * 0.8f);
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

	private static final int xResolution = WindowInfo.getInstance().getWindowWidth() * 3;
	private static final int yResolution = WindowInfo.getInstance().getWindowHeight() * 3;
	private static final int maxKernelDataSize = 0x7F_FFFF; // ~ 8 million pixels

	protected static final Logger logger = LogbackLoggerProvider.getLogger(DefaultGameState.class);
	protected static final String stateName = "PerlinDemo";

	protected final ImageRenderProgram imageRenderProgram;
	protected final RGBFloatTexture rgbTexture;
	protected final IGridDimensions2D voxelGrid;

	private final LayerStack2D layerStack2D = new LayerStack2D();
	private final List<NebulaLayer> nebulaLayers = new ArrayList<>();
	private final List<Sun> stars = new ArrayList<>();

	private final PerlinDemoImageKernel pdiKernel;
	private final Range pdiRange;
	private final Vec3f cameraPosition = new Vec3f(0f);

	private final int kernelRangeSize;
	private final int numKernelChunks;

	public PerlinDemo() {
		imageRenderProgram = new ImageRenderProgram();
		rgbTexture = new RGBFloatTexture(Sampler.linearMirroredSampler, xResolution, yResolution);
		voxelGrid = new IGridDimensions2D() {
			@Override
			public int xDimension() {
				return 1366 * 2;
			}

			@Override
			public int yDimension() {
				return 1366 * 2;
			}
		};

		nebulaLayers.add(new NebulaLayer(9, 100f, 1f, new Color(57, 19, 84), new Color(194, 48, 192), new Color(169, 35, 255)));
		nebulaLayers.add(new NebulaLayer(25, 200f, 0.9f, new Color(19, 52, 84), new Color(48, 194, 99), new Color(52, 182, 160)));
		nebulaLayers.add(new NebulaLayer(3, 200f, 0.75f, new Color(60, 84, 19), new Color(255, 244, 40), new Color(178, 70, 51)));
		nebulaLayers.add(new NebulaLayer(9, 400f, 0.5f, new Color(84, 24, 19), new Color(194, 92, 48), new Color(255, 35, 101)));
		nebulaLayers.sort((a, b) -> Float.compare(b.distance, a.distance));

		pdiKernel = new PerlinDemoImageKernel(xResolution, yResolution);
		pdiKernel.setScatterStrength(0.5f);
		int kernelDataSize = xResolution * yResolution;
		if (kernelDataSize > maxKernelDataSize) {
			int minNumKernelChunks = (int) Math.ceil((double) kernelDataSize / maxKernelDataSize);
			// ensure that the number of kernel chunks evenly divides the data size
			for (int i = minNumKernelChunks; ; i++) {
				if (kernelDataSize % i == 0) {
					this.numKernelChunks = i;
					break;
				}
			}
			this.kernelRangeSize = kernelDataSize / numKernelChunks;
		} else {
			this.numKernelChunks = 1;
			this.kernelRangeSize = kernelDataSize;
		}
		Device targetDevice = KernelManager.instance().getDefaultPreferences().getPreferredDevices(null).get(1);
		pdiRange = targetDevice.createRange(this.kernelRangeSize, findMaxLocalWidth(this.kernelRangeSize, 256));

		generateStars();
		recomputeNebulaData();
		recomputeTexture();
		printControls();
	}

	public void generateStars() {
		final int xQuadrants = 4;
		final int numStars = 256;
		final int positionRetries = 5;

		Random random = new Random();
		stars.clear();

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
					float z = MathF.squared(5f + sunSpawn.referenceRadius + random.nextFloat() * 120f);
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

		// sort by distance to camera descending
		stars.sort((a, b) -> Float.compare(cameraPosition.distanceSquared(b.position), cameraPosition.distanceSquared(a.position)));

		logger.info("generated {} stars", stars.size());
		for (Map.Entry<Sun.SunType, Integer> spawnEntry : spawnedSuns.entrySet()) {
			logger.info("spawned {} {}", spawnEntry.getValue(), spawnEntry.getKey().name());
		}

		pdiKernel.setStarData(stars);
	}

	public void recomputeNebulaData() {
		NebulaLayer nebulaLayer = nebulaLayers.get(0);
		nebulaLayer.prepareCompute(voxelGrid);
		pdiKernel.setNebulaData(cameraPosition,
				new Vec3f(0f, 0f, 100f), new Vec3f(0f, 0f, -1f),
				MatrixMath.inversePerspective(50f, (float) xResolution / yResolution, 1f, 300_000f),
				nebulaLayer
		);
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

	public void recomputeCPU(float halfXRes, float halfYRes, Mat4f inversePerspective, Vec3f nebulaPlanePoint, Vec3f nebulaPlaneNormal, Vec3f cameraPosition, NebulaLayer nebulaLayer) {
		long startTime = System.currentTimeMillis();
		IntStream.range(0, xResolution).parallel()
				.forEach(x -> {
					Vec4f viewDirVector = new Vec4f();
					Vec3f viewDirNormal = new Vec3f();

					for (int y = 0; y < yResolution; y++) {
						viewDirVector.set((x - halfXRes) / halfXRes, (y - halfYRes) / halfYRes, 1f, 1f);
						MatrixArithmeticIP.mul(inversePerspective, viewDirVector);
						viewDirNormal.set(viewDirVector.data).normalize();

						for (Sun star : stars) {
							float distanceToStar = VectorMath.distanceLinePoint(cameraPosition, viewDirNormal, star.position);
							if (distanceToStar <= star.radius) {
								float distanceFactor = distanceToStar / star.radius;
								rgbTexture.setColor(x, y, MathF.lerpColor(star.sunType.coreColor, star.sunType.lightColor, distanceFactor));
							}
						}

						Vec3f nebulaPoint = VectorMath.intersectionPlaneLine(nebulaPlanePoint, nebulaPlaneNormal, cameraPosition, viewDirNormal);
						assert nebulaPoint != null;


						float nebulaValue = nebulaLayer.computeValue(
								(int) (nebulaPoint.data[0] * 100f),
								(int) (nebulaPoint.data[1] * 100f)
						);
						if (nebulaValue > 0f) {
							Color color = NebulaMath.inScatterLight(nebulaValue, rgbTexture.getColor(x, y), viewDirNormal, nebulaPoint, stars);
							if (color != null) {
								rgbTexture.setColor(x, y, color);
							}
						}
					}
				});
		logger.info("drawing stars took: {} ms", (System.currentTimeMillis() - startTime));
	}

	public int findMaxLocalWidth(int globalWidth, int localWidthLimit) {
		for (int i = localWidthLimit; i >= 2; i--) {
			if (globalWidth % i == 0) {
				return i;
			}
		}
		return 1;
	}

	public void recomputeTexture() {
		long kernelStartTime = System.currentTimeMillis();
		Arrays.fill(pdiKernel.redChannelOut, 4 / 255.999f);
		Arrays.fill(pdiKernel.greenChannelOut, 16 / 255.999f);
		Arrays.fill(pdiKernel.blueChannelOut, 31 / 255.999f);

		ProgressReporter progressReporter = new ProgressReporter(this.numKernelChunks);
		progressReporter.start();
		for (int i = 0; i < this.numKernelChunks; i++) {
			pdiKernel.gidOffset = i * this.kernelRangeSize;
			pdiKernel.execute(pdiRange);
			progressReporter.stepComplete();
		}
		logger.info("GPU chunks drawn! took {} ms", progressReporter.getRuntimeMillis());

		for (int i = 0; i < rgbTexture.rgbArray.length; i++) {
			int color = RGBFloatTexture.rgbToColor(pdiKernel.redChannelOut[i], pdiKernel.greenChannelOut[i], pdiKernel.blueChannelOut[i]);
			rgbTexture.rgbArray[i] = color;
		}

		rgbTexture.recompute();

		long kernelEndTime = System.currentTimeMillis();
		logger.info("perlin demo image kernel execution took: {} ms", kernelEndTime - kernelStartTime);
	}

	private float acesToneMapper(float x) {
		double pow = Math.pow(x, 1.2f);
		x = (float) pow;
		final float a = 2.51f;
		final float b = 0.03f;
		final float c = 2.43f;
		final float d = 0.59f;
		final float e = 0.14f;
		return MathF.clamp01((x * (a * x + b)) / (x * (c * x + d) + e));
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
		if (inputManager.getKeyDown(GLFW.GLFW_KEY_KP_1)) {
			try {
				ImageUtils.writePngFile("F:\\skyTexture_scatter_1_0.png", rgbTexture.toImage());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (inputManager.getKeyDown(GLFW.GLFW_KEY_KP_2)) {
			float halfXRes = xResolution / 2f;
			float halfYRes = yResolution / 2f;

			int numLayers = 5;
			NebulaLayer brightnessLayer = new NebulaLayer(15, 200f, 0.9f, null, null, null);
			brightnessLayer.prepareCompute(voxelGrid);
			List<NebulaLayer> lightLayers = new ArrayList<>();
			List<NebulaLayer> maskLayers = new ArrayList<>();
			for (int i = 0; i < numLayers; i++) {
				NebulaLayer nebulaLayer = new NebulaLayer(15, 200f, 0.9f, new Color(19, 52, 84), new Color(48, 194, 99), new Color(52, 182, 160));
				NebulaLayer maskLayer = new NebulaLayer(5, 200f, 0.75f, new Color(60, 84, 19), new Color(255, 244, 40), new Color(178, 70, 51));
				nebulaLayer.prepareCompute(voxelGrid);
				maskLayer.prepareCompute(voxelGrid);
				lightLayers.add(nebulaLayer);
				maskLayers.add(maskLayer);
			}
			Vec3f nebulaPlanePoint = new Vec3f(0f, 0f, 20f);
			Vec3f nebulaPlaneNormal = new Vec3f(0f, 0f, -1f);
			Mat4f inversePerspective = MatrixMath.inversePerspective(50f, (float) xResolution / yResolution, 1f, 300_000f);

			ProgressReporter progressReporter = new ProgressReporter(yResolution * xResolution);
			progressReporter.start();
			IntStream.range(0, yResolution).parallel().forEach(y -> {
				int yOffset = y * xResolution;

				Vec4f viewDirVector = new Vec4f();
				Vec3f viewDirNormal = new Vec3f();

				for (int x = 0; x < xResolution; x++) {
					viewDirVector.set((x - halfXRes) / halfXRes, (y - halfYRes) / halfYRes, 1f, 1f);
					MatrixArithmeticIP.mul(inversePerspective, viewDirVector);
					viewDirNormal.set(viewDirVector.data).normalize();

					Vec3f nebulaPoint = VectorMath.intersectionPlaneLine(nebulaPlanePoint, nebulaPlaneNormal, cameraPosition, viewDirNormal);
					assert nebulaPoint != null;

					int color = rgbTexture.rgbArray[yOffset + x];
					int red = 0xFF & color;
					int green = 0xFF & (color >> 8);
					int blue = 0xFF & (color >> 16);

					float brightnessValue = brightnessLayer.computeValue(
							(int) (nebulaPoint.data[0] * 100f),
							(int) (nebulaPoint.data[1] * 100f)
					) * 0.1f;
					brightnessValue *= brightnessValue;
					green += (int) (brightnessValue * 128f);
					blue += (int) (brightnessValue * 256f);

					for (int i = 0; i < numLayers; i++) {
						NebulaLayer nebulaLayer = lightLayers.get(i);
						NebulaLayer maskLayer = maskLayers.get(i);

						float maskValue = maskLayer.computeValue(
								(int) (nebulaPoint.data[0] * 100f),
								(int) (nebulaPoint.data[1] * 100f)
						) * 2f;
						maskValue -= 1f;
						maskValue *= 1.5f;
						maskValue -= 0.5f;

						maskValue = Math.min(3f, Math.max(0f, maskValue));
						float maskFactor = 1f - (2f * Math.abs(maskValue - ((int) maskValue) - 0.5f));

						float nebulaValue = nebulaLayer.computeValue(
								(int) (nebulaPoint.data[0] * 100f),
								(int) (nebulaPoint.data[1] * 100f)
						) * 0.2f * maskFactor;

						nebulaValue = nebulaValue * nebulaValue * 0.15f;
						if (maskValue < 1f) {
							red += (int) (nebulaValue * 256f);
							green += (int) (nebulaValue * 0f);
							blue += (int) (nebulaValue * 128f);
						} else if (maskValue < 2f) {
							red += (int) (nebulaValue * 0f);
							green += (int) (nebulaValue * 175f);
							blue += (int) (nebulaValue * 256f);
						} else {
							red += (int) (nebulaValue * 96f);
							green += (int) (nebulaValue * 256f);
							blue += (int) (nebulaValue * 0f);
						}
					}

					red = Math.min(255, (int) (acesToneMapper(red / 256f) * 256f));
					green = Math.min(255, (int) (acesToneMapper(green / 256f) * 256f));
					blue = Math.min(255, (int) (acesToneMapper(blue / 256f) * 256f));
					rgbTexture.rgbArray[yOffset + x] = red | (green << 8) | (blue << 16);

					progressReporter.stepComplete();
				}
			});
			logger.info("took {} ms", progressReporter.getRuntimeMillis());

			rgbTexture.recompute();
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
		pdiKernel.dispose();
	}
}
