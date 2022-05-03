package ecs.systems;

import ecs.Entity;
import ecs.UpdateSystem;
import ecs.components.LifeTimeLightIntensity;
import utils.MathF;

public class LifeTimeLightIntensitySystem implements UpdateSystem {
	private static LifeTimeLightIntensitySystem instance;

	public static LifeTimeLightIntensitySystem getInstance() {
		if (instance == null) {
			instance = new LifeTimeLightIntensitySystem();
		}
		return instance;
	}

	private LifeTimeLightIntensitySystem() {
	}

	@Override
	public boolean isTargetEntity_updateTick(Entity entity) {
		return entity.lifeTimeComponent != null && entity.lifeTimeLightIntensityComponent != null
				&& entity.lightComponent != null;
	}

	@Override
	public void execute_updateTick(Entity entity) {
		LifeTimeLightIntensity intensity = entity.lifeTimeLightIntensityComponent;
		entity.lightComponent.setIntensity(MathF.lerp(intensity.endLightIntensity, intensity.startLightIntensity,
				entity.lifeTimeComponent.lifeTime / entity.lifeTimeComponent.initialLifeTime));
	}

	@Override
	public void onExecuteUpdateDone() {
	}
}
