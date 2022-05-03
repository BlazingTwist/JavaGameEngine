package ecs.components;

import utils.collision.AxisAlignedBoundingBox;
import utils.operator.Operator;
import utils.vector.Vec3f;

public class AABBCollider {

	public static AABBCollider unitBounds() {
		return new AABBCollider(new Vec3f(1f), new Vec3f(-1f));
	}

	private final Vec3f positiveBoundOffset;
	private final Vec3f negativeBoundOffset;

	private final AxisAlignedBoundingBox boundingBox = new AxisAlignedBoundingBox(Vec3f.zero(), Vec3f.zero());
	private byte trackedTransformChangeID = 0;

	public AABBCollider(Vec3f positiveBoundOffset, Vec3f negativeBoundOffset) {
		this.positiveBoundOffset = positiveBoundOffset;
		this.negativeBoundOffset = negativeBoundOffset;
	}

	public AxisAlignedBoundingBox getAABB(Transform transform) {
		if(transform.getChangeID() != trackedTransformChangeID){
			trackedTransformChangeID = transform.getChangeID();
			Vec3f absScale = transform.getScale().copy().apply(Math::abs);
			Vec3f pointA = absScale.copy().apply(Operator.Mul, negativeBoundOffset).apply(Operator.Add, transform.getPosition());
			Vec3f pointB = absScale.apply(Operator.Mul, positiveBoundOffset).apply(Operator.Add, transform.getPosition());
			boundingBox.min().set(pointA.data);
			boundingBox.max().set(pointB.data);
		}
		return boundingBox;
	}

	public AxisAlignedBoundingBox getOldAABB(){
		return boundingBox;
	}
}
