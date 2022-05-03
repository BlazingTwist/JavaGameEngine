package ecs.systems;

import ecs.Entity;
import ecs.UpdateSystem;

public class ComputeDataSystem implements UpdateSystem {
	private static ComputeDataSystem instance;

	public static ComputeDataSystem getInstance() {
		if (instance == null) {
			instance = new ComputeDataSystem();
		}
		return instance;
	}

	private ComputeDataSystem() {
	}

	@Override
	public boolean isTargetEntity_updateTick(Entity entity) {
		entity.transform.checkRecompute();
		return false;
	}

	@Override
	public void execute_updateTick(Entity entity) {

	}

	@Override
	public void onExecuteUpdateDone() {

	}
}
