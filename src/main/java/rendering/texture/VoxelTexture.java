package rendering.texture;

import java.util.Map;
import org.lwjgl.opengl.GL45;
import utils.MathF;
import utils.noise.twodim.IVoxelGrid2D;

public class VoxelTexture implements ITexture {

	private static float getRed01(int color) {
		int red = color & 0xFF;
		return red / 255f;
	}

	private static float getGreen01(int color) {
		int green = (color >> 8) & 0xFF;
		return green / 255f;
	}

	private static float getBlue01(int color) {
		int blue = (color >> 16) & 0xFF;
		return blue / 255f;
	}

	private static int colorFromComponents01(float red, float green, float blue) {
		int redInt = Math.round(red * 255);
		int greenInt = Math.round(green * 255);
		int blueInt = Math.round(blue * 255);
		return (blueInt << 16) | (greenInt << 8) | (redInt);
	}

	private final IVoxelGrid2D voxelGrid;
	private final int textureID;
	private final Sampler sampler;

	private boolean isDeleted = false;

	public VoxelTexture(IVoxelGrid2D voxelGrid, Sampler sampler) {
		this.voxelGrid = voxelGrid;
		textureID = GL45.glGenTextures();
		this.sampler = sampler;
	}

	/**
	 * @param colorMapping maps from voxel value to display color, interpolates linearly between each point
	 */
	public void recompute(Map<Float, Integer> colorMapping) {
		if (isDeleted) {
			throw new IllegalStateException("cannot call 'recompute' on deleted Texture!");
		}

		assert !colorMapping.isEmpty();

		GL45.glBindTexture(GL45.GL_TEXTURE_2D, textureID);

		float[] mappedVoxelValues = new float[colorMapping.size()];
		int[] mappedColorValues = new int[colorMapping.size()];
		{
			int i = 0;
			for (Map.Entry<Float, Integer> colorEntry : colorMapping.entrySet()) {
				mappedVoxelValues[i] = colorEntry.getKey();
				mappedColorValues[i] = colorEntry.getValue();
				i++;
			}
		}

		int width = voxelGrid.xDimension();
		int height = voxelGrid.yDimension();
		int[] rgbArray = new int[width * height];
		int offset = 0;
		for (int y = 0; y < height; y++, offset += width) {
			for (int x = 0; x < width; x++) {
				float voxelValue = voxelGrid.voxels2D()[x][y];

				float nearestValueBelow = Float.NEGATIVE_INFINITY;
				float nearestValueAbove = Float.POSITIVE_INFINITY;
				int indexNearestBelow = -1;
				int indexNearestAbove = -1;
				for (int i = 0; i < mappedVoxelValues.length; i++) {
					float mappedVoxelValue = mappedVoxelValues[i];
					if (mappedVoxelValue > nearestValueBelow && mappedVoxelValue <= voxelValue) {
						nearestValueBelow = mappedVoxelValue;
						indexNearestBelow = i;
					}
					if (mappedVoxelValue < nearestValueAbove && mappedVoxelValue >= voxelValue) {
						nearestValueAbove = mappedVoxelValue;
						indexNearestAbove = i;
					}
				}

				if (indexNearestBelow == -1) {
					rgbArray[offset + x] = mappedColorValues[indexNearestAbove];
				} else if (indexNearestAbove == -1 || indexNearestAbove == indexNearestBelow) {
					rgbArray[offset + x] = mappedColorValues[indexNearestBelow];
				} else {
					int colorAbove = mappedColorValues[indexNearestAbove];
					int colorBelow = mappedColorValues[indexNearestBelow];
					float nearestValueSpread = nearestValueAbove - nearestValueBelow;
					float lerpFactorFromBelowTowardsAbove = (voxelValue - nearestValueBelow) / nearestValueSpread;
					float redLerp = MathF.lerp(getRed01(colorBelow), getRed01(colorAbove), lerpFactorFromBelowTowardsAbove);
					float greenLerp = MathF.lerp(getGreen01(colorBelow), getGreen01(colorAbove), lerpFactorFromBelowTowardsAbove);
					float blueLerp = MathF.lerp(getBlue01(colorBelow), getBlue01(colorAbove), lerpFactorFromBelowTowardsAbove);
					rgbArray[offset + x] = colorFromComponents01(redLerp, greenLerp, blueLerp);
				}
			}
		}

		GL45.glTexImage2D(GL45.GL_TEXTURE_2D, 0, GL45.GL_RGBA8, width, height, 0, GL45.GL_RGBA, GL45.GL_UNSIGNED_INT_8_8_8_8_REV, rgbArray);
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
