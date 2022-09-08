package gamestate.states.perlindemo;

import gamestate.states.PerlinDemo;
import java.awt.Color;
import java.util.List;
import utils.MathF;
import utils.vector.Vec3f;
import utils.vector.VectorMath;

public class NebulaMath {

	public static final float scatterStrength = 4f;
	public static final float redWavelength = 440;
	public static final float greenWavelength = 530;
	public static final float blueWavelength = 770;
	public static final float scatterRed = (float) Math.pow(400d / redWavelength, 4) * scatterStrength;
	public static final float scatterGreen = (float) Math.pow(400d / greenWavelength, 4) * scatterStrength;
	public static final float scatterBlue = (float) Math.pow(400d / blueWavelength, 4) * scatterStrength;

	public static Color inScatterLight(float nebulaDensity, Color originalColor, Vec3f viewDirNormal, Vec3f nebulaPoint, List<PerlinDemo.Sun> suns) {
		if (nebulaDensity <= 0f) {
			return null;
		}

		float inScatterRed = 0f;
		float inScatterGreen = 0f;
		float inScatterBlue = 0f;
		for (PerlinDemo.Sun sun : suns) {
			if(nebulaPoint.data[2] > sun.position.data[2]) {
				continue;
			}

			float distanceToSun = VectorMath.distanceLinePoint(nebulaPoint, viewDirNormal, sun.position);
			float opticalDepth = opticalDepth(Math.max(-0.75f, distanceToSun - (sun.radius * 2f)), nebulaDensity)
					+ opticalDepth(1f, nebulaDensity);
			float transmittanceRed = (float) Math.exp((-opticalDepth) * scatterRed);
			float transmittanceGreen = (float) Math.exp((-opticalDepth) * scatterGreen);
			float transmittanceBlue = (float) Math.exp((-opticalDepth) * scatterBlue);
			inScatterRed += transmittanceRed * nebulaDensity * scatterRed;
			inScatterGreen += transmittanceGreen * nebulaDensity * scatterGreen;
			inScatterBlue += transmittanceBlue * nebulaDensity * scatterBlue;
		}

		float originalTransmittance = (float) Math.exp(-opticalDepth(1f, nebulaDensity));
		float[] originalRGB = originalColor.getRGBColorComponents(null);

		return new Color(
				MathF.clamp01(inScatterRed + originalRGB[0] * originalTransmittance),
				MathF.clamp01(inScatterGreen + originalRGB[1] * originalTransmittance),
				MathF.clamp01(inScatterBlue + originalRGB[2] * originalTransmittance)
		);
	}

	public static float opticalDepth(float rayLength, float nebulaDensity) {
		return rayLength * nebulaDensity;
	}

}
