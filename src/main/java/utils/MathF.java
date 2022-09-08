package utils;

import java.awt.Color;

public class MathF {
	public static float clamp(float value, float min, float max) {
		return Math.min(Math.max(value, min), max);
	}

	public static float clamp01(float value) {
		return value < 0 ? 0
				: value > 1 ? 1
				: value;
	}

	public static float squared(float value) {
		return value * value;
	}

	public static float sqrt(float value) {
		return (float) Math.sqrt(value);
	}

	/**
	 * Clamped linear interpolation
	 * e.g. (10, 20, 0.75) -> 17.5
	 * e.g. (10, 20, 1.00) -> 20
	 * e.g. (10, 20, 2.75) -> 20
	 *
	 * @param a      value if factor == 0
	 * @param b      value if factor == 1
	 * @param factor factor of linear interpolation from a to b
	 * @return a float value between a and b
	 */
	public static float lerp(float a, float b, float factor) {
		factor = clamp01(factor);
		return a + ((b - a) * factor);
	}

	/**
	 * Unclamped linear interpolation
	 * e.g. (10, 20, 0.75) -> 17.5
	 * e.g. (10, 20, 1.00) -> 20
	 * e.g. (10, 20, 2.75) -> 37.5
	 *
	 * @param a      value if factor == 0
	 * @param b      value if factor == 1
	 * @param factor factor of linear interpolation from a to b
	 * @return linear interpolation result
	 */
	public static float lerpUnclamped(float a, float b, float factor) {
		return a + ((b - a) * factor);
	}

	public static Color lerpColor(Color a, Color b, float factor) {
		float[] rgbA = a.getRGBColorComponents(null);
		float[] rgbB = b.getRGBColorComponents(null);
		factor = clamp01(factor);
		return new Color(
				lerpUnclamped(rgbA[0], rgbB[0], factor),
				lerpUnclamped(rgbA[1], rgbB[1], factor),
				lerpUnclamped(rgbA[2], rgbB[2], factor)
		);
	}

	/**
	 * @param a                 first value
	 * @param b                 second value
	 * @param smoothingInterval max difference between a and b where smoothing occurs (recommended <= 0.4)
	 * @return a smoothed minimum value that is <= min(a,b)
	 */
	public static float smoothMin(float a, float b, float smoothingInterval) {
		// see "polynomial smooth min 2" -> https://iquilezles.org/articles/smin/
		float k = Math.max(0, smoothingInterval);
		float h = Math.max(0f, 1f - (Math.abs(a - b) / k));
		return Math.min(a, b) - h * h * k * 0.25f;
	}

	/**
	 * @param a                 first value
	 * @param b                 second value
	 * @param smoothingInterval max difference between a and b where smoothing occurs (recommended <= 0.4)
	 * @return a smoothed maximum value that is >= max(a,b)
	 */
	public static float smoothMax(float a, float b, float smoothingInterval) {
		float k = Math.max(0, smoothingInterval);
		float h = Math.max(0f, 1f - (Math.abs(a - b) / k));
		return Math.max(a, b) + h * h * k * 0.25f;
	}

	public static float smoothStep(float a, float b, float factor) {
		factor = clamp01(factor);
		factor = -2f * factor * factor * factor + 3f * factor * factor;
		return b * factor + a * (1f - factor);
	}
}
