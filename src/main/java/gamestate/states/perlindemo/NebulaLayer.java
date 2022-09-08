package gamestate.states.perlindemo;

import gamestate.Time;
import java.awt.Color;
import java.util.Random;
import utils.noise.twodim.INoiseLayer2D;
import utils.noise.twodim.IVoxelGrid2D;
import utils.noise.twodim.LayerStack2D;
import utils.noise.twodim.PerlinLayer2D;

public class NebulaLayer implements INoiseLayer2D {

	private static final Random random = new Random();

	public final LayerStack2D perlinLayerStack = new LayerStack2D();
	public final LayerStack2D maskLayerStack = new LayerStack2D();
	public final float distance;
	public final float distanceFactor;
	public final Color baseColor;
	public final Color brightColor;
	public final Color highlightColor;

	public NebulaLayer(float distance, float distanceFactor, Color baseColor, Color brightColor, Color highlightColor) {
		this.distance = distance;
		this.distanceFactor = distanceFactor;
		this.baseColor = baseColor;
		this.brightColor = brightColor;
		this.highlightColor = highlightColor;

		generatePerlinLayers(perlinLayerStack, 0.75f, 9, 1000f * random.nextFloat(), 1f);
		generatePerlinLayers(maskLayerStack, 0.1f, 3, 1000f * (1f + random.nextFloat()), -0.15f);
	}

	private void generatePerlinLayers(LayerStack2D stack, float scaleFactor, int perlinLayers, float offset, float valueShift) {
		final float baseScale = 1.33f * scaleFactor * (1f + (distance / 400f)) * (1f + (distance / 400f));
		final float scaleGainFactor = 1.59f;
		final float baseXOffset = 123.456f + offset + distance;
		final float baseYOffset = 345.987f + (2 * offset) + (2 * distance);
		final float xOffsetGain = 150.1333f * (1f + (distance / 1000f));
		final float yOffsetGain = 200.1333f * (1f + (distance / 1000f));
		final float baseMinValue = -1f;
		final float baseMaxValue = 1f;

		float currentScale = baseScale;
		float currentX = baseXOffset;
		float currentY = baseYOffset;

		for (int i = 0; i < perlinLayers; i++) {
			stack.layers.add(new PerlinLayer2D(currentScale,
					currentX + currentScale, currentY + currentScale,
					baseMinValue / (i + 1), baseMaxValue / (i + 1), valueShift / (i + 1))
			);
			currentScale *= scaleGainFactor;
			currentX += xOffsetGain;
			currentY += yOffsetGain;
		}
	}

	public void animate() {
		for (INoiseLayer2D layer : perlinLayerStack.layers) {
			if (layer instanceof PerlinLayer2D perlinLayer) {
				perlinLayer.xOffset += (random.nextFloat() * Time.graphicsDeltaSeconds);
				perlinLayer.yOffset += (random.nextFloat() * Time.graphicsDeltaSeconds);
			}
		}
	}

	@Override
	public void prepareCompute(IVoxelGrid2D grid) {
		perlinLayerStack.prepareCompute(grid);
		maskLayerStack.prepareCompute(grid);
	}

	@Override
	public float computeValue(int x, int y) {
		x += 100_000;
		y += 100_000;

		float perlinValue = perlinLayerStack.computeValue(x, y);
		if (perlinValue > 0) {
			return perlinValue * 0.5f;
		}
		return 0f;
	}
}
