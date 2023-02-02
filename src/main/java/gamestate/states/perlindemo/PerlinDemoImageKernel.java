package gamestate.states.perlindemo;

import com.aparapi.Kernel;
import gamestate.states.PerlinDemo;
import java.util.List;
import utils.matrix.Mat4f;
import utils.matrix.MatrixArithmeticIP;
import utils.noise.twodim.INoiseLayer2D;
import utils.vector.Vec3f;
import utils.vector.Vec4f;
import utils.vector.VectorMath;

@SuppressWarnings("ManualArrayCopy")
public class PerlinDemoImageKernel extends Kernel {

	@Constant
	public static final float redWavelength = 440;

	@Constant
	public static final float greenWavelength = 530;

	@Constant
	public static final float blueWavelength = 770;

	public int gidOffset = 0;
	public float scatterStrength = 1f;
	public float scatterRed = (float) Math.pow(400d / redWavelength, 4) * scatterStrength;
	public float scatterGreen = (float) Math.pow(400d / greenWavelength, 4) * scatterStrength;
	public float scatterBlue = (float) Math.pow(400d / blueWavelength, 4) * scatterStrength;

	public final float[] redChannelOut;
	public final float[] greenChannelOut;
	public final float[] blueChannelOut;

	@Constant
	final static int starSize = 3 + 1 + 3 + 3; // contains { f3 position, f radius, f3 coreColor, f3 lightColor }

	@Constant
	final int xResolution;

	@Constant
	final int yResolution;

	@Constant
	final float halfXRes;

	@Constant
	final float halfYRes;

	final float[] cameraPosition;
	final float[] inversePerspectiveMatrix;
	final float[] nebulaPlanePoint;
	final float[] nebulaPlaneNormal;
	final float[] nebulaValues;

	int numStars;
	float[] starsData;

	public PerlinDemoImageKernel(int xResolution, int yResolution) {
		this.xResolution = xResolution;
		this.yResolution = yResolution;
		this.halfXRes = xResolution / 2f;
		this.halfYRes = yResolution / 2f;
		this.cameraPosition = new float[Vec3f.DATA_LEN];
		this.inversePerspectiveMatrix = new float[Mat4f.DATA_LEN];
		this.nebulaValues = new float[xResolution * yResolution];
		this.nebulaPlanePoint = new float[Vec3f.DATA_LEN];
		this.nebulaPlaneNormal = new float[Vec3f.DATA_LEN];

		redChannelOut = new float[xResolution * yResolution];
		greenChannelOut = new float[xResolution * yResolution];
		blueChannelOut = new float[xResolution * yResolution];
	}

	public void setNebulaData(Vec3f cameraPosition, Vec3f nebulaPlanePoint, Vec3f nebulaPlaneNormal, Mat4f inversePerspective, INoiseLayer2D nebulaLayer) {
		System.arraycopy(cameraPosition.data, 0, this.cameraPosition, 0, Vec3f.DATA_LEN);
		System.arraycopy(nebulaPlanePoint.data, 0, this.nebulaPlanePoint, 0, Vec3f.DATA_LEN);
		System.arraycopy(nebulaPlaneNormal.data, 0, this.nebulaPlaneNormal, 0, Vec3f.DATA_LEN);
		System.arraycopy(inversePerspective.data, 0, this.inversePerspectiveMatrix, 0, Mat4f.DATA_LEN);

		Vec4f viewDirVector = new Vec4f();
		Vec3f viewDirNormal = new Vec3f();
		for (int y = 0; y < yResolution; y++) {
			int yOffset = y * xResolution;
			for (int x = 0; x < xResolution; x++) {
				viewDirVector.set((x - halfXRes) / halfXRes, (y - halfYRes) / halfYRes, 1f, 1f);
				MatrixArithmeticIP.mul(inversePerspective, viewDirVector);
				viewDirNormal.set(viewDirVector.data).normalize();

				Vec3f nebulaPoint = VectorMath.intersectionPlaneLine(nebulaPlanePoint, nebulaPlaneNormal, cameraPosition, viewDirNormal);
				assert nebulaPoint != null;

				nebulaValues[yOffset + x] = nebulaLayer.computeValue(
						(int) (nebulaPoint.data[0] * 100f),
						(int) (nebulaPoint.data[1] * 100f)
				);
			}
		}
	}

	public void setStarData(List<PerlinDemo.Sun> stars) {
		numStars = stars.size();
		starsData = new float[numStars * starSize];
		float[] _coreColorRGB = new float[3];
		float[] _lightColorRGB = new float[3];
		for (int i = 0; i < stars.size(); i++) {
			PerlinDemo.Sun star = stars.get(i);
			star.sunType.coreColor.getRGBColorComponents(_coreColorRGB);
			star.sunType.lightColor.getRGBColorComponents(_lightColorRGB);

			int starOffset = i * starSize;
			System.arraycopy(star.position.data, 0, starsData, starOffset, 3);
			starsData[starOffset + 3] = star.radius;
			System.arraycopy(_coreColorRGB, 0, starsData, starOffset + 4, 3);
			System.arraycopy(_lightColorRGB, 0, starsData, starOffset + 7, 3);
		}
	}

	public void setScatterStrength(float strength) {
		this.scatterStrength = strength;
		scatterRed = (float) Math.pow(400d / redWavelength, 4) * scatterStrength;
		scatterGreen = (float) Math.pow(400d / greenWavelength, 4) * scatterStrength;
		scatterBlue = (float) Math.pow(400d / blueWavelength, 4) * scatterStrength;
	}

	@Override
	public void run() {
		int gid = getGlobalId() + gidOffset;
		int y = gid / xResolution;
		int x = gid % xResolution;

		float[] viewDirNormal = new float[3];
		float[] _viewDirVector = new float[4];
		_viewDirVector[0] = (x - halfXRes) / halfXRes;
		_viewDirVector[1] = (y - halfYRes) / halfYRes;
		_viewDirVector[2] = 1f;
		_viewDirVector[3] = 1f;

		for (int i = 0; i < 3; i++) {
			int matrixOffset = i * 4;
			for (int i2 = 0; i2 < 4; i2++) {
				viewDirNormal[i] += (inversePerspectiveMatrix[matrixOffset + i2] * _viewDirVector[i2]);
			}
		}

		float len = 0f;
		for (int i = 0; i < 3; i++) {
			len += (viewDirNormal[i] * viewDirNormal[i]);
		}
		float normalizeFactor = 1f / sqrt(len);
		for (int i = 0; i < 3; i++) {
			viewDirNormal[i] *= normalizeFactor;
		}

		float[] starPosition = new float[3];
		float[] starCoreColor = new float[3];
		float[] starLightColor = new float[3];
		for (int starIndex = 0; starIndex < numStars; starIndex++) {
			for (int i = 0; i < 3; i++) {
				starPosition[i] = starsData[i + (starIndex * starSize)];
			}

			for (int i = 0; i < 3; i++) {
				starCoreColor[i] = starsData[i + ((starIndex * starSize) + 4)];
			}

			for (int i = 0; i < 3; i++) {
				starLightColor[i] = starsData[i + ((starIndex * starSize) + 7)];
			}

			float starRadius = starsData[(starIndex * starSize) + 3];

			float pointToRayX = cameraPosition[0] - starPosition[0];
			float pointToRayY = cameraPosition[1] - starPosition[1];
			float pointToRayZ = cameraPosition[2] - starPosition[2];
			float crossX = viewDirNormal[1] * pointToRayZ - viewDirNormal[2] * pointToRayY;
			float crossY = viewDirNormal[2] * pointToRayX - viewDirNormal[0] * pointToRayZ;
			float crossZ = viewDirNormal[0] * pointToRayY - viewDirNormal[1] * pointToRayX;
			float distanceToStar = sqrt(crossX * crossX + crossY * crossY + crossZ * crossZ);

			if (distanceToStar <= starRadius) {
				float distanceFactor = distanceToStar / starRadius;

				redChannelOut[gid] = starCoreColor[0] + ((starLightColor[0] - starCoreColor[0]) * distanceFactor);
				greenChannelOut[gid] = starCoreColor[1] + ((starLightColor[1] - starCoreColor[1]) * distanceFactor);
				blueChannelOut[gid] = starCoreColor[2] + ((starLightColor[2] - starCoreColor[2]) * distanceFactor);
			}
		}

		{
			float nebulaDensity = nebulaValues[gid];
			if (nebulaDensity <= 0f) {
				return;
			}

			float[] nebulaPoint = new float[3];
			float x1 = nebulaPlaneNormal[0] * nebulaPlanePoint[0]
					+ nebulaPlaneNormal[1] * nebulaPlanePoint[1]
					+ nebulaPlaneNormal[2] * nebulaPlanePoint[2];
			float x2 = nebulaPlaneNormal[0] * cameraPosition[0]
					+ nebulaPlaneNormal[1] * cameraPosition[1]
					+ nebulaPlaneNormal[2] * cameraPosition[2];
			float x3 = nebulaPlaneNormal[0] * viewDirNormal[0]
					+ nebulaPlaneNormal[1] * viewDirNormal[1]
					+ nebulaPlaneNormal[2] * viewDirNormal[2];

			float k = (x1 - x2) / x3;
			for (int i = 0; i < 3; i++) {
				nebulaPoint[i] = (viewDirNormal[i] * k) + cameraPosition[i];
			}

			float inScatterRed = 0f;
			float inScatterGreen = 0f;
			float inScatterBlue = 0f;

			for (int starIndex = 0; starIndex < numStars; starIndex++) {
				for (int i = 0; i < 3; i++) {
					starPosition[i] = starsData[i + (starIndex * starSize)];
				}

				float starRadius = starsData[(starIndex * starSize) + 3];

				float pointToRayX = nebulaPoint[0] - starPosition[0];
				float pointToRayY = nebulaPoint[1] - starPosition[1];
				float pointToRayZ = nebulaPoint[2] - starPosition[2];
				float crossX = viewDirNormal[1] * pointToRayZ - viewDirNormal[2] * pointToRayY;
				float crossY = viewDirNormal[2] * pointToRayX - viewDirNormal[0] * pointToRayZ;
				float crossZ = viewDirNormal[0] * pointToRayY - viewDirNormal[1] * pointToRayX;
				float distanceToStar = sqrt(crossX * crossX + crossY * crossY + crossZ * crossZ);

				float opticalDepth = (max(-0.75f, distanceToStar - starRadius) * nebulaDensity) + nebulaDensity;
				opticalDepth /= max(1f, min(starRadius, 10f));
				float transmittanceRed = exp((-opticalDepth) * scatterRed);
				float transmittanceGreen = exp((-opticalDepth) * scatterGreen);
				float transmittanceBlue = exp((-opticalDepth) * scatterBlue);
				inScatterRed += transmittanceRed * nebulaDensity * scatterRed;
				inScatterGreen += transmittanceGreen * nebulaDensity * scatterGreen;
				inScatterBlue += transmittanceBlue * nebulaDensity * scatterBlue;
			}

			float originalTransmittance = exp(-(0.1f * nebulaDensity));
			redChannelOut[gid] = max(0f, min(1f, inScatterRed + (redChannelOut[gid] * originalTransmittance)));
			greenChannelOut[gid] = max(0f, min(1f, inScatterGreen + (greenChannelOut[gid] * originalTransmittance)));
			blueChannelOut[gid] = max(0f, min(1f, inScatterBlue + (blueChannelOut[gid] * originalTransmittance)));
		}
	}
}
