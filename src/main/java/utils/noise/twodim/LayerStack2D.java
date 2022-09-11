package utils.noise.twodim;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class LayerStack2D implements INoiseLayer2D {

	public final ArrayList<INoiseLayer2D> layers = new ArrayList<>();

	public LayerStack2D() {
	}

	public LayerStack2D(INoiseLayer2D... layers) {
		this.layers.addAll(Arrays.asList(layers));
	}

	@Override
	public void prepareCompute(IGridDimensions2D gridDimensions) {
		layers.forEach(layer -> layer.prepareCompute(gridDimensions));
	}

	@Override
	public float computeValue(int x, int y) {
		return layers.stream()
				.map(layer -> layer.computeValue(x, y))
				.reduce(0f, Float::sum);
	}

	@Override
	public String toString() {
		return "LayerStack2D{"
				+ "layers: [" + layers.stream().map(Object::toString).collect(Collectors.joining(", ")) + "]"
				+ '}';
	}
}
