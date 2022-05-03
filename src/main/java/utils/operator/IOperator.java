package utils.operator;

public interface IOperator {
	float apply(float a, float b);

	void apply(float[] data, int start, int length, float scalar);

	void apply(float[] data, int start, int length, float[] other);

	void apply(float[] target, int targetOffset, float[] a, int aOffset, float[] b, int bOffset, int length);
}
