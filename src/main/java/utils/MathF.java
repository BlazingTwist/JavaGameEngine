package utils;

public class MathF {
	public static float clamp01(float value) {
		return value < 0 ? 0
				: value > 1 ? 1
				: value;
	}

	/**
	 * Clamped linear interpolation
	 *   e.g. (10, 20, 0.75) -> 17.5
	 *   e.g. (10, 20, 1.00) -> 20
	 *   e.g. (10, 20, 2.75) -> 20
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
	 *   e.g. (10, 20, 0.75) -> 17.5
	 *   e.g. (10, 20, 1.00) -> 20
	 *   e.g. (10, 20, 2.75) -> 37.5
	 *
	 * @param a      value if factor == 0
	 * @param b      value if factor == 1
	 * @param factor factor of linear interpolation from a to b
	 * @return linear interpolation result
	 */
	public static float lerpUnclamped(float a, float b, float factor) {
		return a + ((b - a) * factor);
	}
}
