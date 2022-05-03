package ecs.systems;

import ecs.Entity;
import ecs.UpdateSystem;

public class ShockwaveExpandingAnimatorSystem implements UpdateSystem {
	private static ShockwaveExpandingAnimatorSystem instance;

	public static ShockwaveExpandingAnimatorSystem getInstance() {
		if (instance == null) {
			instance = new ShockwaveExpandingAnimatorSystem();
		}
		return instance;
	}

	private ShockwaveExpandingAnimatorSystem() {
	}

	@Override
	public boolean isTargetEntity_updateTick(Entity entity) {
		return entity.shockwaveComponent != null && entity.shockwaveExpandingAnimator != null;
	}

	@Override
	public void execute_updateTick(Entity entity) {
		entity.shockwaveExpandingAnimator.apply(entity);
	}

	@Override
	public void onExecuteUpdateDone() {

	}
}
