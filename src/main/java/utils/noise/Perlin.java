package utils.noise;

/**
 * 3D implementation based on https://en.wikipedia.org/wiki/Perlin_noise
 */
public class Perlin {
	private static final float sqrt2 = (float) Math.sqrt(2.0);
	private static final float sqrt3 = (float) Math.sqrt(3.0);

	private static float interpolate(float a, float b, float w) {
		//return (b - a) * w + a;
		return (b - a) * (3.0f - w * 2.0f) * w * w + a;
	}

	/**
	 * @return a random float value in range (-1,+1)
	 */
	private static float randomFloat(int seedA, int seedB) {
		final int halfIntBits = Integer.BYTES * 4;
		seedA *= 1136673795;
		seedB ^= (seedA << halfIntBits | seedA >> halfIntBits);
		seedB *= 1911520717;
		seedA ^= (seedB << halfIntBits | seedB >> halfIntBits);
		seedA *= 2048419325;
		float absoluteValue = Float.intBitsToFloat((seedA & 0x00_7F_FF_FF) | 0x3F_80_00_00) - 1f;
		int signBit = seedA & 0b1 << 31;
		return Float.intBitsToFloat(Float.floatToRawIntBits(absoluteValue) | signBit);
	}

	private static float dotGridGradient(int ix, int iy, float x, float y) {
		float normalX = randomFloat(ix, iy);
		float normalY = randomFloat(iy, ix);

		float dx = x - (float) ix;
		float dy = y - (float) iy;

		float normalLength = (float) Math.sqrt(normalX * normalX + normalY * normalY);
		return (dx * normalX / normalLength) + (dy * normalY / normalLength);
	}

	private static float dotGridGradient(int ix, int iy, int iz, float x, float y, float z) {
		float normalX = randomFloat(ix, iy);
		float normalY = randomFloat(iy, iz);
		float normalZ = randomFloat(iz, ix);

		float dx = x - (float) ix;
		float dy = y - (float) iy;
		float dz = z - (float) iz;

		float normalLength = (float) Math.sqrt(normalX * normalX + normalY * normalY + normalZ * normalZ);
		return (dx * normalX / normalLength) + (dy * normalY / normalLength) + (dz * normalZ / normalLength);
	}

	/**
	 * @return a pseudo-random float value in range (-sqrt(0.5) , +sqrt(0.5))
	 */
	public static float perlin(float x, float y) {
		int x0 = (int) x;
		int x1 = x0 + 1;
		int y0 = (int) y;
		int y1 = y0 + 1;

		float sx = x - ((float) x0);
		float sy = y - ((float) y0);

		float dot00 = dotGridGradient(x0, y0, x, y);
		float dot01 = dotGridGradient(x0, y1, x, y);
		float dot10 = dotGridGradient(x1, y0, x, y);
		float dot11 = dotGridGradient(x1, y1, x, y);

		float i0 = interpolate(dot00, dot10, sx);
		float i1 = interpolate(dot01, dot11, sx);
		return interpolate(i0, i1, sy);
	}

	/**
	 * @return a pseudo-random float value in range (-sqrt(0.75) , +sqrt(0.75))
	 */
	public static float perlin(float x, float y, float z) {
		int x0 = (int) x;
		int x1 = x0 + 1;
		int y0 = (int) y;
		int y1 = y0 + 1;
		int z0 = (int) z;
		int z1 = z0 + 1;

		float sx = x - ((float) x0);
		float sy = y - ((float) y0);
		float sz = z - ((float) z0);

		float xy0 = interpolate(
				interpolate(dotGridGradient(x0, y0, z0, x, y, z), dotGridGradient(x1, y0, z0, x, y, z), sx),
				interpolate(dotGridGradient(x0, y1, z0, x, y, z), dotGridGradient(x1, y1, z0, x, y, z), sx),
				sy
		);
		float xy1 = interpolate(
				interpolate(dotGridGradient(x0, y0, z1, x, y, z), dotGridGradient(x1, y0, z1, x, y, z), sx),
				interpolate(dotGridGradient(x0, y1, z1, x, y, z), dotGridGradient(x1, y1, z1, x, y, z), sx),
				sy
		);
		return interpolate(xy0, xy1, sz);
	}

	/**
	 * @return a pseudo-random float value in range (-1 , +1)
	 */
	public static float perlin01(float x, float y) {
		return (perlin(x, y) / sqrt2) + 0.5f;
	}

	/**
	 * @return a pseudo-random float value in range (-1 , +1)
	 */
	public static float perlin01(float x, float y, float z) {
		return (perlin(x, y, z) / sqrt3) + 0.5f;
	}

	/**
	 * @param xDim    amount of x values
	 * @param yDim    amount of y values
	 * @param zDim    amount of z values
	 * @param scale   amount of grid cells (1x1x1) to cover
	 * @param xOffset start offset in x direction
	 * @param yOffset start offset in y direction
	 * @param zOffset start offset in z direction
	 * @return a float[xDim][yDim][zDim] array of perlin noise values in range (-1, +1)
	 */
	public static float[][][] perlinGrid(int xDim, int yDim, int zDim, float scale, float xOffset, float yOffset, float zOffset) {
		float[][][] result = new float[xDim][yDim][zDim];
		float xStep = scale / xDim;
		float yStep = scale / yDim;
		float zStep = scale / zDim;

		float x = xOffset;
		for (int ix = 0; ix < xDim; ix++, x += xStep) {
			float[][] resultX = result[ix];
			float y = yOffset;
			for (int iy = 0; iy < yDim; iy++, y += yStep) {
				float[] resultY = resultX[iy];
				float z = zOffset;
				for (int iz = 0; iz < zDim; iz++, z += zStep) {
					resultY[iz] = (perlin(x, y, z) * 2.0f / sqrt3);
				}
			}
		}

		return result;
	}

	/**
	 * @param xDim     amount of x values
	 * @param yDim     amount of y values
	 * @param zDim     amount of z values
	 * @param scale    amount of grid cells (1x1x1) to cover
	 * @param xOffset  start offset in x direction
	 * @param yOffset  start offset in y direction
	 * @param zOffset  start offset in z direction
	 * @param minValue lower bound for each cell value
	 * @param maxValue upper bound for each cell value
	 * @return a float[xDim][yDim][zDim] array of perlin noise values in range (-1, +1)
	 */
	public static float[][][] perlinGrid(int xDim, int yDim, int zDim, float scale,
										 float xOffset, float yOffset, float zOffset,
										 float minValue, float maxValue) {
		float[][][] result = new float[xDim][yDim][zDim];
		float xStep = scale / xDim;
		float yStep = scale / yDim;
		float zStep = scale / zDim;
		float outputDelta = maxValue - minValue;

		float x = xOffset;
		for (int ix = 0; ix < xDim; ix++, x += xStep) {
			float[][] resultX = result[ix];
			float y = yOffset;
			for (int iy = 0; iy < yDim; iy++, y += yStep) {
				float[] resultY = resultX[iy];
				float z = zOffset;
				for (int iz = 0; iz < zDim; iz++, z += zStep) {
					resultY[iz] = (perlin01(x, y, z) * outputDelta) + minValue;
				}
			}
		}

		return result;
	}
}
