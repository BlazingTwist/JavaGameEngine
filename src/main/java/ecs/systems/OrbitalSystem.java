package ecs.systems;

import ecs.Entity;
import ecs.UpdateSystem;
import java.util.ArrayList;
import utils.operator.Operator;
import utils.vector.Vec3f;

public class OrbitalSystem implements UpdateSystem {
	private static OrbitalSystem instance;

	public static OrbitalSystem getInstance() {
		if (instance == null) {
			instance = new OrbitalSystem();
		}
		return instance;
	}

	private static final double gravConstant = 6.6743e-5;
	private final ArrayList<Entity> orbitalEntities = new ArrayList<>();

	private OrbitalSystem() {
	}

	@Override
	public boolean isTargetEntity_updateTick(Entity entity) {
		return entity.transform != null && entity.velocityComponent != null && entity.orbitalComponent != null;
	}

	@Override
	public synchronized void execute_updateTick(Entity entity) {
		// TODO this could probably be multithreaded a bit more nicely
		orbitalEntities.parallelStream().forEach(other -> {
			Vec3f vecToOther = other.transform.getPosition().copy().apply(Operator.Sub, entity.transform.getPosition());
			Vec3f vecToOtherNormal = vecToOther.copy().normalize();

			float distanceSquared = vecToOther.dot(vecToOther);
			double acceleration = gravConstant * other.orbitalComponent.getMass() / distanceSquared;
			double otherAcceleration = gravConstant * entity.orbitalComponent.getMass() / distanceSquared;

			entity.velocityComponent.applyAcceleration(vecToOtherNormal, (float) acceleration);
			synchronized (orbitalEntities) {
				other.velocityComponent.applyAcceleration(vecToOtherNormal, (float) (-otherAcceleration));
			}
		});
		orbitalEntities.add(entity);
	}

	@Override
	public void onExecuteUpdateDone() {
		orbitalEntities.clear();
	}
}
