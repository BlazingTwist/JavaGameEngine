package utils.noise.threedim;

import java.util.List;
import java.util.stream.Stream;
import utils.MathF;

public class PlanetLayer3D implements INoiseLayer3D {
	public final List<INoiseLayer3D> continentNoiseLayers;
	public final List<INoiseLayer3D> continentMaskNoiseLayers;
	public final List<INoiseLayer3D> ridgeNoiseLayers;
	public final SphereLayer3D outerLayer;
	public final SphereLayer3D middleLayer;
	public final SphereLayer3D innerLayer;
	public float maskCutoff = 0.4f;
	public float ridgeCutoff = 0.7f;

	public PlanetLayer3D(List<INoiseLayer3D> continentNoiseLayers, List<INoiseLayer3D> continentMaskNoiseLayers,
						 List<INoiseLayer3D> ridgeNoiseLayers,
						 SphereLayer3D outerLayer, SphereLayer3D middleLayer, SphereLayer3D innerLayer) {
		this.continentNoiseLayers = continentNoiseLayers;
		this.continentMaskNoiseLayers = continentMaskNoiseLayers;
		this.ridgeNoiseLayers = ridgeNoiseLayers;
		this.outerLayer = outerLayer;
		this.middleLayer = middleLayer;
		this.innerLayer = innerLayer;
	}

	public void animate(float step) {
		Stream.concat(Stream.concat(continentNoiseLayers.stream(), continentMaskNoiseLayers.stream()), ridgeNoiseLayers.stream())
				.forEach(layer -> {
					if (layer instanceof PerlinLayer3D perlinLayer) {
						perlinLayer.xOffset += step;
						perlinLayer.yOffset += step;
						perlinLayer.zOffset += step;
					}
				});
	}

	@Override
	public void prepareCompute(IGridDimensions3D gridDimensions) {
		continentNoiseLayers.forEach(x -> x.prepareCompute(gridDimensions));
		continentMaskNoiseLayers.forEach(x -> x.prepareCompute(gridDimensions));
		ridgeNoiseLayers.forEach(x -> x.prepareCompute(gridDimensions));
		outerLayer.prepareCompute(gridDimensions);
		middleLayer.prepareCompute(gridDimensions);
		innerLayer.prepareCompute(gridDimensions);
	}

	@Override
	public float computeValue(float x, float y, float z) {
		float continentMask = continentMaskNoiseLayers.stream().map(layer -> layer.computeValue(x, y, z)).reduce(0f, Float::sum);
		float oceanFactor = continentNoiseLayers.stream().map(layer -> layer.computeValue(x, y, z)).reduce(0f, Float::sum)
				* (continentMask < maskCutoff ? 0f : 1f);
		float ridgeFactor = ridgeNoiseLayers.stream().map(layer -> layer.computeValue(x, y, z)).reduce(0f, Float::sum);
		float maskedRidgeFactor = ridgeFactor * (continentMask > ridgeCutoff ? 0f : 1f);
		float outerShape = outerLayer.computeValue(x, y, z);
		float middleShape = middleLayer.computeValue(x, y, z);
		float innerShape = innerLayer.computeValue(x, y, z);
		return MathF.lerp(MathF.lerp(middleShape, innerShape, oceanFactor), outerShape, maskedRidgeFactor) + (ridgeFactor * 0.05f);
	}
}
