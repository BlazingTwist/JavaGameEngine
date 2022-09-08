package utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

public class ImageUtils {

	public static BufferedImage createGrayscaleImage(float[][] dataXY, float min, float max) {
		if (dataXY == null || dataXY.length <= 0) {
			return null;
		}

		final int imgMaxValue = 0xFFFF;
		float dataSpread = max - min;
		int width = dataXY.length;
		int height = dataXY[0].length;
		BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_USHORT_GRAY);
		for (int x = 0; x < width; x++) {
			float[] dataY = dataXY[x];
			for (int y = 0; y < height; y++) {
				float data01 = MathF.clamp01((dataY[y] - min) / dataSpread);
				int imgValue = Math.round(data01 * imgMaxValue);
				result.getRaster().setDataElements(x, y, new short[]{(short) imgValue});
			}
		}
		return result;
	}

	public static void writePngFile(String path, BufferedImage image) throws IOException {
		Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("png");
		ImageWriter writer = writers.next();

		File outputFile = new File(path);
		if (!outputFile.exists()) {
			if (!outputFile.createNewFile()) {
				System.err.println("file '" + path + "' create failed!");
				return;
			}
		}
		FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
		ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(fileOutputStream);
		writer.setOutput(imageOutputStream);

		ImageWriteParam writerParameters = writer.getDefaultWriteParam();
		writerParameters.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		writerParameters.setCompressionQuality(0.1f);
		writer.write(null, new IIOImage(image, null, null), writerParameters);

		fileOutputStream.close();
		imageOutputStream.close();
		writer.dispose();
	}
}
