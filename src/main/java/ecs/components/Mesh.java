package ecs.components;

import org.lwjgl.opengl.GL45;
import rendering.mesh.MeshData;
import rendering.texture.FallbackTextures;
import rendering.texture.ITexture;

public class Mesh {
	private MeshData meshData;
	private ITexture textureData;
	private ITexture phongData;
	private ITexture normalData;
	private ITexture heightData;
	private boolean isEnabled = true;

	public Mesh(MeshData meshData, ITexture textureData, ITexture phongData, ITexture normalData, ITexture heightData) {
		this.meshData = meshData;
		setTextureData(textureData);
		setPhongData(phongData);
		setNormalData(normalData);
		setHeightData(heightData);
	}

	public MeshData getMeshData() {
		return meshData;
	}

	public Mesh setMeshData(MeshData meshData) {
		this.meshData = meshData;
		return this;
	}

	public ITexture getTextureData() {
		return textureData;
	}

	public Mesh setTextureData(ITexture textureData) {
		this.textureData = textureData;
		return this;
	}

	public ITexture getPhongData() {
		return phongData;
	}

	public Mesh setPhongData(ITexture phongData) {
		this.phongData = phongData;
		return this;
	}

	public ITexture getNormalData() {
		return normalData;
	}

	public Mesh setNormalData(ITexture normalData) {
		this.normalData = normalData;
		return this;
	}

	public ITexture getHeightData() {
		return heightData;
	}

	public Mesh setHeightData(ITexture heightData) {
		this.heightData = heightData;
		return this;
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	public Mesh setEnabled(boolean enabled) {
		isEnabled = enabled;
		return this;
	}

	public void bindTextures(int textureSlot, int phongSlot, int normalSlot, int heightSlot,
							 int glsl_textureFlags) {
		int textureFlag = ((normalData != null) ? 1 : 0) | ((heightData != null) ? 2 : 0);
		GL45.glUniform1i(glsl_textureFlags, textureFlag);

		(textureData == null ? FallbackTextures.getFallbackTexture() : textureData).bindTexture(textureSlot);
		(phongData == null ? FallbackTextures.getFallbackPhong() : phongData).bindTexture(phongSlot);

		if (normalData != null) {
			normalData.bindTexture(normalSlot);
		}
		if (heightData != null) {
			heightData.bindTexture(heightSlot);
		}
	}
}
