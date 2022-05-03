package ecs;

public interface DrawSystem {
	boolean isTargetEntity_drawTick(Entity entity);

	void execute_drawTick(Entity entity);

	void onExecuteDrawDone();
}
