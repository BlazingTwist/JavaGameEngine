package ecs.systems;

import ecs.Entity;
import ecs.EntityRegistry;
import ecs.UpdateSystem;
import gamestate.Time;

public class LifeTimeSystem implements UpdateSystem {
	private static LifeTimeSystem instance;

	public static LifeTimeSystem getInstance() {
		if (instance == null) {
			instance = new LifeTimeSystem();
		}
		return instance;
	}

	private LifeTimeSystem() {
	}

	@Override
	public boolean isTargetEntity_updateTick(Entity entity) {
		return entity.lifeTimeComponent != null;
	}

	@Override
	public void execute_updateTick(Entity entity) {
		entity.lifeTimeComponent.lifeTime -= Time.physicsDeltaSecondsF;
		if (entity.lifeTimeComponent.lifeTime < 0) {
			EntityRegistry.getInstance().enqueueEraseEntity(entity);
		}
	}

	@Override
	public void onExecuteUpdateDone() {

	}
}
