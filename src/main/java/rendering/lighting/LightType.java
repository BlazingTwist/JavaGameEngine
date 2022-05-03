package rendering.lighting;

public enum LightType {
	directional(0),
	spot(1),
	point(2);

	LightType(int glsl_type) {
		this.glsl_type = glsl_type;
	}

	public final int glsl_type;
}
