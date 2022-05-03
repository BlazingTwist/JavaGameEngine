package ecs.systems;

import ecs.Entity;
import ecs.UpdateSystem;

public class ApplyScaleVelocitySystem implements UpdateSystem {
	private static ApplyScaleVelocitySystem instance;

	public static ApplyScaleVelocitySystem getInstance() {
		if (instance == null) {
			instance = new ApplyScaleVelocitySystem();
		}
		return instance;
	}

	private ApplyScaleVelocitySystem() {
	}


	@Override
	public boolean isTargetEntity_updateTick(Entity entity) {
		return entity.transform != null && entity.scaleVelocity != null;
	}

	@Override
	public void execute_updateTick(Entity entity) {
		entity.scaleVelocity.applyScale(entity.transform);
	}

	@Override
	public void onExecuteUpdateDone() {

	}
}
