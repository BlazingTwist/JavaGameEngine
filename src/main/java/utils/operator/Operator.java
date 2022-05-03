package utils.operator;

public enum Operator implements IOperator {
	Add(new Add()),
	Sub(new Sub()),
	Mul(new Mul()),
	Div(new Div());

	Operator(IOperator operator) {
		this.operator = operator;
	}

	public IOperator operator;

	@Override
	public float apply(float a, float b) {
		return operator.apply(a, b);
	}

	@Override
	public void apply(float[] data, int start, int length, float scalar) {
		operator.apply(data, start, length, scalar);
	}

	@Override
	public void apply(float[] data, int start, int length, float[] other) {
		operator.apply(data, start, length, other);
	}

	@Override
	public void apply(float[] target, int targetOffset, float[] a, int aOffset, float[] b, int bOffset, int length) {
		operator.apply(target, targetOffset, a, aOffset, b, bOffset, length);
	}
}
