package rendering.texture;

import org.lwjgl.opengl.GL45;

public class Sampler {

	public enum Filter {
		Point,
		Linear
	}

	public enum Border {
		Repeat(GL45.GL_REPEAT),
		Mirror(GL45.GL_MIRRORED_REPEAT),
		Clamp(GL45.GL_CLAMP_TO_EDGE),
		Border(GL45.GL_CLAMP_TO_BORDER);

		public final int glWrapType;

		Border(int glWrapType) {
			this.glWrapType = glWrapType;
		}
	}

	public static Sampler linearMirroredSampler = new Sampler(Filter.Linear, Filter.Linear, Filter.Linear, Border.Mirror, true);
	public static Sampler intDataSampler = new Sampler(Filter.Point, Filter.Point, Filter.Point, Border.Repeat, false);

	private final int samplerID;
	private boolean isDeleted = false;

	public int getSamplerID() {
		if (isDeleted) {
			throw new IllegalStateException("Cannot call 'getSamplerID' on deleted Sampler");
		}
		return samplerID;
	}

	public Sampler(Filter minFilter, Filter magFilter, Filter mipFilter, Border borderHandling, boolean mipMap) {
		samplerID = GL45.glGenSamplers();
		GL45.glSamplerParameteri(samplerID, GL45.GL_TEXTURE_WRAP_S, borderHandling.glWrapType);
		GL45.glSamplerParameteri(samplerID, GL45.GL_TEXTURE_WRAP_T, borderHandling.glWrapType);
		GL45.glSamplerParameteri(samplerID, GL45.GL_TEXTURE_WRAP_R, borderHandling.glWrapType);

		int glMinFilter = mipMap ?
				minFilter == Filter.Point
						? mipFilter == Filter.Point ? GL45.GL_NEAREST_MIPMAP_NEAREST : GL45.GL_NEAREST_MIPMAP_LINEAR
						: mipFilter == Filter.Point ? GL45.GL_LINEAR_MIPMAP_NEAREST : GL45.GL_LINEAR_MIPMAP_LINEAR
				: minFilter == Filter.Point ? GL45.GL_NEAREST : GL45.GL_LINEAR;
		GL45.glSamplerParameteri(samplerID, GL45.GL_TEXTURE_MIN_FILTER, glMinFilter);
		GL45.glSamplerParameteri(samplerID, GL45.GL_TEXTURE_MAG_FILTER, magFilter == Filter.Point ? GL45.GL_NEAREST : GL45.GL_LINEAR);
	}

	public void bind(int textureSlot) {
		GL45.glBindSampler(textureSlot, samplerID);
	}

	public void deleteSampler() {
		if (isDeleted) {
			return;
		}
		GL45.glDeleteSamplers(samplerID);
		isDeleted = true;
	}
}
