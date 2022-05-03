package gamestate;

public class Time {
	public static final long physicsDeltaMilliseconds = 1000L / 30L;
	public static final double physicsDeltaSeconds = 1.0d / 30.0d;
	public static final float physicsDeltaSecondsF = 1.0f / 30.0f;
	public static final double physicsDeltaSecondsSquared = physicsDeltaSeconds * physicsDeltaSeconds;
	public static final float physicsDeltaSecondsSquaredF = physicsDeltaSecondsF * physicsDeltaSecondsF;

	public static final long graphicsDeltaMilliseconds = 1000L / 60L;
	public static final double graphicsDeltaSeconds = 1.0d / 60.0d;
	public static final double graphicsDeltaSecondsSquared = graphicsDeltaSeconds * graphicsDeltaSeconds;
}
