package ecs;

public interface UpdateSystem {
	boolean isTargetEntity_updateTick(Entity entity);

	void execute_updateTick(Entity entity);

	void onExecuteUpdateDone();
}
