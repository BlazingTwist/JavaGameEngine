package rendering.texture;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import logging.LogbackLoggerProvider;
import org.lwjgl.opengl.GL45;
import org.slf4j.Logger;

public class Texture2D implements ITexture {
	private static final Logger logger = LogbackLoggerProvider.getLogger(Texture2D.class);

	public static Texture2D fromResource(String resourcePath, Sampler sampler) {
		BufferedImage image;
		try {
			InputStream resourceAsStream = Texture2D.class.getClassLoader().getResourceAsStream(resourcePath);
			if (resourceAsStream == null) {
				throw new FileNotFoundException("Resource-Stream was null.");
			}
			image = ImageIO.read(resourceAsStream);
		} catch (IOException e) {
			logger.error("Failed to load file: {}", resourcePath, e);
			return null;
		}

		return new Texture2D(image, sampler);
	}

	private final int textureID;
	private final Sampler sampler;
	private boolean isDeleted = false;

	private Texture2D(BufferedImage image, Sampler sampler) {
		textureID = GL45.glGenTextures();
		this.sampler = sampler;
		GL45.glBindTexture(GL45.GL_TEXTURE_2D, textureID);

		//int[] rgb = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
		ColorModel colorModel = image.getColorModel();
		WritableRaster raster = image.getRaster();
		Object data = ITexture.getImageRasterDataObject(raster);
		int height = image.getHeight();
		int width = image.getWidth();
		int[] rgbArray = new int[height * width];
		int offset = 0;
		for (int y = 0; y < height; y++, offset += width) {
			for (int x = 0; x < width; x++) {
				rgbArray[offset + x] = (colorModel.getAlpha(raster.getDataElements(x, y, data)) << 24)
						| (colorModel.getBlue(raster.getDataElements(x, y, data)) << 16)
						| (colorModel.getGreen(raster.getDataElements(x, y, data)) << 8)
						| colorModel.getRed(raster.getDataElements(x, y, data));
			}
		}

		GL45.glTexImage2D(GL45.GL_TEXTURE_2D, 0, GL45.GL_RGBA8, image.getWidth(), image.getHeight(), 0, GL45.GL_RGBA, GL45.GL_UNSIGNED_INT_8_8_8_8_REV, rgbArray);
		GL45.glGenerateMipmap(GL45.GL_TEXTURE_2D);
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
