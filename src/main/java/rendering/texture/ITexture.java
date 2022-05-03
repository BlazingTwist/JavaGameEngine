package rendering.texture;

import java.awt.image.DataBuffer;
import java.awt.image.Raster;

public interface ITexture {
	static void deleteTexture(ITexture texture) {
		if (texture != null) {
			texture.deleteTexture();
		}
	}

	static Object getImageRasterDataObject(Raster raster) {
		// copy-pasta of BufferedImage::getRGB
		Object data;
		int nBands = raster.getNumBands();
		int dataType = raster.getDataBuffer().getDataType();
		data = switch (dataType) {
			case DataBuffer.TYPE_BYTE -> new byte[nBands];
			case DataBuffer.TYPE_USHORT -> new short[nBands];
			case DataBuffer.TYPE_INT -> new int[nBands];
			case DataBuffer.TYPE_FLOAT -> new float[nBands];
			case DataBuffer.TYPE_DOUBLE -> new double[nBands];
			default -> throw new IllegalArgumentException("Unknown data buffer type: " +
					dataType);
		};
		return data;
	}

	int getTextureID();

	void bindTexture(int slot);

	void deleteTexture();
}
