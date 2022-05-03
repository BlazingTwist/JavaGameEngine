package rendering.texture;

public class FallbackTextures {
	private static Texture2D fallbackTexture = null;
	private static Texture2D fallbackPhong = null;

	public static Texture2D getFallbackTexture() {
		if (fallbackTexture == null) {
			fallbackTexture = Texture2D.fromResource("textures/fallback/texture.png", Sampler.linearMirroredSampler);
		}
		return fallbackTexture;
	}

	public static Texture2D getFallbackPhong() {
		if (fallbackPhong == null) {
			fallbackPhong = Texture2D.fromResource("textures/fallback/phong.png", Sampler.linearMirroredSampler);
		}
		return fallbackPhong;
	}

}
