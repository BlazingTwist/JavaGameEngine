package ecs.systems;

import ecs.Entity;
import ecs.UpdateSystem;

public class ApplyVelocitySystem implements UpdateSystem {
	private static ApplyVelocitySystem instance;

	public static ApplyVelocitySystem getInstance() {
		if (instance == null) {
			instance = new ApplyVelocitySystem();
		}
		return instance;
	}

	private ApplyVelocitySystem() {
	}

	@Override
	public boolean isTargetEntity_updateTick(Entity entity) {
		return entity.transform != null && entity.velocityComponent != null;
	}

	@Override
	public void execute_updateTick(Entity entity) {
		entity.velocityComponent.applyVelocity(entity.transform);
	}

	@Override
	public void onExecuteUpdateDone() {
	}
}
