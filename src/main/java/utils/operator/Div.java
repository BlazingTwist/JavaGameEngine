package utils.operator;

public class Div implements IOperator {
	@Override
	public float apply(float a, float b) {
		return a / b;
	}

	@Override
	public void apply(float[] data, int start, int length, float scalar) {
		int end = start + length;
		for(int i = start; i < end; i++){
			data[i] /= scalar;
		}
	}

	@Override
	public void apply(float[] data, int start, int length, float[] other) {
		int end = start + length;
		for(int i = start; i < end; i++){
			data[i] /= other[i];
		}
	}

	@Override
	public void apply(float[] target, int targetOffset, float[] a, int aOffset, float[] b, int bOffset, int length) {
		for (int i = 0; i < length; i++) {
			target[i + targetOffset] = a[i + aOffset] / b[i + bOffset];
		}
	}
}
