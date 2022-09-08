package gamestate.states.perlindemo;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import org.lwjgl.opengl.GL45;
import rendering.texture.ITexture;
import rendering.texture.Sampler;

public class RGBFloatTexture implements ITexture {

	public final int width;
	public final int height;

	private final int textureID;
	private final Sampler sampler;
	private final int[] rgbArray;
	private final Color[][] colors;

	private boolean isDeleted = false;

	public RGBFloatTexture(Sampler sampler, int width, int height) {
		textureID = GL45.glGenTextures();
		this.sampler = sampler;

		this.width = width;
		this.height = height;
		rgbArray = new int[width * height];
		colors = new Color[height][width];
	}

	public Color getColor(int x, int y) {
		return colors[y][x];
	}

	public void setColor(int x, int y, Color color) {
		colors[y][x] = color;
	}

	public void setColor(Color color) {
		for (Color[] colorRow : colors) {
			Arrays.fill(colorRow, color);
		}
	}

	public void recompute() {
		if (isDeleted) {
			throw new IllegalStateException("cannot call 'recompute' on deleted Texture!");
		}

		GL45.glBindTexture(GL45.GL_TEXTURE_2D, textureID);
		int offset = 0;
		for (int y = 0; y < height; y++, offset += width) {
			Color[] colorRow = colors[y];
			for (int x = 0; x < width; x++) {
				Color color = colorRow[x];
				rgbArray[offset + x] = color.getRed() | (color.getGreen() << 8) | (color.getBlue() << 16);
			}
		}
		GL45.glTexImage2D(GL45.GL_TEXTURE_2D, 0, GL45.GL_RGBA8, width, height, 0, GL45.GL_RGBA, GL45.GL_UNSIGNED_INT_8_8_8_8_REV, rgbArray);
		GL45.glGenerateMipmap(GL45.GL_TEXTURE_2D);
	}

	public BufferedImage toImage() {
		BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		for (int y = 0; y < height; y++) {
			Color[] colorRow = colors[y];
			for (int x = 0; x < width; x++) {
				Color color = colorRow[x];
				result.getRaster().setDataElements(x, y, new int[]{color.getRGB()});
			}
		}
		return result;
	}

	@Override
	public int getTextureID() {
		if (isDeleted) {
			throw new IllegalStateException("cannot call 'getTextureID' of deleted Texture!");
		}
		return textureID;
	}

	@Override
	public void bindTexture(int slot) {
		if (isDeleted) {
			throw new IllegalStateException("cannot call 'bindTexture' of deleted Texture!");
		}
		GL45.glActiveTexture(GL45.GL_TEXTURE0 + slot);
		GL45.glBindTexture(GL45.GL_TEXTURE_2D, textureID);
		sampler.bind(slot);
	}

	@Override
	public void deleteTexture() {
		if (isDeleted) {
			return;
		}
		GL45.glBindTexture(GL45.GL_TEXTURE_2D, 0);
		GL45.glDeleteTextures(textureID);
		isDeleted = true;
	}
}
