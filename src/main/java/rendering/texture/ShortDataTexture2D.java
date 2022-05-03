package rendering.texture;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferUShort;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import logging.LogbackLoggerProvider;
import org.lwjgl.opengl.GL45;
import org.slf4j.Logger;

public class ShortDataTexture2D implements ITexture {
	private static final Logger logger = LogbackLoggerProvider.getLogger(ShortDataTexture2D.class);

	public static ShortDataTexture2D fromResource(String resourcePath, Sampler sampler) {
		BufferedImage image;
		try {
			InputStream resourceAsStream = ShortDataTexture2D.class.getClassLoader().getResourceAsStream(resourcePath);
			if (resourceAsStream == null) {
				throw new FileNotFoundException("Resource-Stream was null.");
			}
			image = ImageIO.read(resourceAsStream);
		} catch (IOException e) {
			logger.error("Failed to load file: {}", resourcePath, e);
			return null;
		}

		return new ShortDataTexture2D(image, sampler);
	}

	private final int textureID;
	private final Sampler sampler;
	private boolean isDeleted = false;

	public ShortDataTexture2D(BufferedImage image, Sampler sampler) {
		textureID = GL45.glGenTextures();
		this.sampler = sampler;
		GL45.glBindTexture(GL45.GL_TEXTURE_2D, textureID);

		int height = image.getHeight();
		int width = image.getWidth();
		DataBufferUShort shortBuffer = (DataBufferUShort) image.getRaster().getDataBuffer();
		short[] data = shortBuffer.getData();

		GL45.glPixelStorei(GL45.GL_UNPACK_ALIGNMENT, 2);
		GL45.glTexImage2D(GL45.GL_TEXTURE_2D, 0, GL45.GL_R16I, width, height, 0, GL45.GL_RED_INTEGER, GL45.GL_SHORT, data);
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
