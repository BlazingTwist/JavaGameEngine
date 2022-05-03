package ecs.systems;

import ecs.Entity;
import ecs.UpdateSystem;

public class RotationalVelocitySystem implements UpdateSystem {
	private static RotationalVelocitySystem instance;

	public static RotationalVelocitySystem getInstance() {
		if (instance == null) {
			instance = new RotationalVelocitySystem();
		}
		return instance;
	}

	private RotationalVelocitySystem() {
	}

	@Override
	public boolean isTargetEntity_updateTick(Entity entity) {
		return entity.transform != null && entity.rotationalVelocity != null;
	}

	@Override
	public void execute_updateTick(Entity entity) {
		entity.rotationalVelocity.applyRotation(entity.transform);
	}

	@Override
	public void onExecuteUpdateDone() {

	}
}
