package utils.vector;

import utils.operator.Operator;

public class VectorMath {

	public static float distanceLinePoint(Vec3f rayOrigin, Vec3f rayDirectionNormalized, Vec3f point) {
		Vec3f pointToOrigin = rayOrigin.copy().apply(Operator.Sub, point);
		return rayDirectionNormalized.copy().cross(pointToOrigin).length();
	}

	/**
	 * @param pointInPlane a point that is in the plane
	 * @param planeNormal  a normalized vector in direction of the plane's normal vector
	 * @param point        point whose distance to the plane should be computed
	 * @return distance between point and plane, positive if point is on the side the normal vector points out of, negative otherwise
	 */
	public static float distancePlanePoint(Vec3f pointInPlane, Vec3f planeNormal, Vec3f point) {
		return planeNormal.dot(point.copy().apply(Operator.Sub, pointInPlane));
	}

	/**
	 * @param pointInPlane            a point that is in the plane
	 * @param planeNormal             a normalized vector in direction of the plane's normal vector
	 * @param lineOrigin              origin point of the line
	 * @param lineDirectionNormalized a normalized vector in direction of the line
	 * @return null if plane is parallel to line (you have to check whether the line is in the plane or not), otherwise intersection point
	 */
	public static Vec3f intersectionPlaneLine(Vec3f pointInPlane, Vec3f planeNormal, Vec3f lineOrigin, Vec3f lineDirectionNormalized) {
		float dotPlaneLine = planeNormal.dot(lineDirectionNormalized);
		if (dotPlaneLine == 0) {
			return null;
		}

		float k = (planeNormal.dot(pointInPlane) - planeNormal.dot(lineOrigin)) / dotPlaneLine;
		return lineDirectionNormalized.copy().apply(Operator.Mul, k).apply(Operator.Add, lineOrigin);
	}

}
