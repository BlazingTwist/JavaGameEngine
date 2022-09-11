package gamestate.states.perlindemo;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import org.lwjgl.opengl.GL45;
import rendering.texture.ITexture;
import rendering.texture.Sampler;

public class RGBFloatTexture implements ITexture {

	public static int rgbToColor(float red, float green, float blue) {
		int redInt = (int) (red * 255.999f);
		int greenInt = (int) (green * 255.999f);
		int blueInt = (int) (blue * 255.999f);
		return redInt | (greenInt << 8) | (blueInt << 16);
	}

	public static int convertOpenGLRGB(int rgb) {
		int r = 0xFF & rgb;
		int g = 0xFF & (rgb >> 8);
		int b = 0xFF & (rgb >> 16);
		return b | (g << 8) | (r << 16);
	}

	public final int width;
	public final int height;
	public final int[] rgbArray;

	private final int textureID;
	private final Sampler sampler;

	private boolean isDeleted = false;

	public RGBFloatTexture(Sampler sampler, int width, int height) {
		textureID = GL45.glGenTextures();
		this.sampler = sampler;

		this.width = width;
		this.height = height;
		rgbArray = new int[width * height];
	}

	public Color getColor(int x, int y) {
		int intColor = rgbArray[(y * width) + x];
		return new Color(0xFF & intColor, 0xFF & (intColor >> 8), 0xFF & (intColor >> 16));
	}

	public void setColor(int x, int y, Color color) {
		int intColor = color.getRed() | (color.getGreen() << 8) | (color.getBlue() << 16);
		rgbArray[(y * width) + x] = intColor;
	}

	public void setColor(int x, int y, int glIntColor) {
		rgbArray[(y * width) + x] = glIntColor;
	}

	public void setColor(Color color) {
		int intColor = color.getRed() | (color.getGreen() << 8) | (color.getBlue() << 16);
		Arrays.fill(rgbArray, intColor);
	}

	public void recompute() {
		if (isDeleted) {
			throw new IllegalStateException("cannot call 'recompute' on deleted Texture!");
		}

		GL45.glBindTexture(GL45.GL_TEXTURE_2D, textureID);
		GL45.glTexImage2D(GL45.GL_TEXTURE_2D, 0, GL45.GL_RGBA8, width, height, 0, GL45.GL_RGBA, GL45.GL_UNSIGNED_INT_8_8_8_8_REV, rgbArray);
		GL45.glGenerateMipmap(GL45.GL_TEXTURE_2D);
	}

	public BufferedImage toImage() {
		BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		int offset = 0;
		for (int y = 0; y < height; y++, offset += width) {
			for (int x = 0; x < width; x++) {
				result.getRaster().setDataElements(x, y, new int[]{convertOpenGLRGB(rgbArray[offset + x])});
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
