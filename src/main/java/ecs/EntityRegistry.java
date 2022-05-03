package ecs;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityRegistry {
	private static final Logger logger = LoggerFactory.getLogger(EntityRegistry.class);

	private static EntityRegistry instance;

	public static EntityRegistry getInstance() {
		if (instance == null) {
			instance = new EntityRegistry();
		}
		return instance;
	}

	private final ArrayList<Entity> entities = new ArrayList<>();
	private final ArrayList<UpdateSystem> updateSystems = new ArrayList<>();
	private final ArrayList<DrawSystem> drawSystems = new ArrayList<>();
	private final ArrayList<EntityEraseListener> entityEraseListeners = new ArrayList<>();

	private final ArrayBlockingQueue<Entity> entitiesToErase = new ArrayBlockingQueue<>(128);
	// if the arrayBlockingQueue is full, fallback to LinkedBlockingQueue, so we don't drop erasure-events
	private final LinkedBlockingQueue<Entity> entitiesToEraseBackup = new LinkedBlockingQueue<>();

	private EntityRegistry() {
	}

	public void reset() {
		entities.clear();
		updateSystems.clear();
		drawSystems.clear();
		entityEraseListeners.clear();
		entitiesToErase.clear();
		entitiesToEraseBackup.clear();
	}

	public Entity createEntity() {
		Entity entity = new Entity(entities.size());
		entities.add(entity);
		return entity;
	}

	public void enqueueEraseEntity(Entity entity) {
		if (!entitiesToErase.offer(entity)) {
			try {
				if (!entitiesToEraseBackup.offer(entity, 8, TimeUnit.MILLISECONDS)) {
					logger.error("Failed to enqueue Entity erasure, backupQueue was blocked for more than 8ms");
				}
			} catch (InterruptedException e) {
				logger.error("Failed to enqueue Entity erasure", e);
			}
		}
	}

	public void eraseEntity(Entity entity) {
		if (entity.isExpired) {
			return;
		}

		for (EntityEraseListener entityEraseListener : entityEraseListeners) {
			entityEraseListener.onErase(entity);
		}

		entity.isExpired = true;
		int lastIndex = entities.size() - 1;
		if (entity.ecsIndex != lastIndex) {
			// move last entity, so we can get correct indices after deletion in O(1)
			Entity lastEntity = entities.get(lastIndex);
			lastEntity.ecsIndex = entity.ecsIndex;
			entities.set(lastEntity.ecsIndex, lastEntity);
		}
		entities.remove(lastIndex);
	}

	public void registerSystem(Object system) {
		if (system instanceof EntityEraseListener) {
			entityEraseListeners.add((EntityEraseListener) system);
		}
		if (system instanceof DrawSystem) {
			drawSystems.add((DrawSystem) system);
		}
		if (system instanceof UpdateSystem) {
			updateSystems.add((UpdateSystem) system);
		}
	}

	public void executeUpdateSystem(UpdateSystem system) {
		entities.parallelStream()
				.filter(entity -> !entity.isExpired && system.isTargetEntity_updateTick(entity))
				.forEach(system::execute_updateTick);
		system.onExecuteUpdateDone();
	}

	public void executeUpdate() {
		for (UpdateSystem system : updateSystems) {
			entities.parallelStream()
					.filter(entity -> !entity.isExpired && system.isTargetEntity_updateTick(entity))
					.forEach(system::execute_updateTick);
			system.onExecuteUpdateDone();
		}
		for (Entity entity : entitiesToErase) {
			eraseEntity(entity);
		}
		entitiesToErase.clear();
		for (Entity entity : entitiesToEraseBackup) {
			eraseEntity(entity);
		}
		entitiesToEraseBackup.clear();
	}

	public void executeDraw() {
		for (DrawSystem drawSystem : drawSystems) {
			entities.stream()
					.filter(entity -> !entity.isExpired && drawSystem.isTargetEntity_drawTick(entity))
					.forEach(drawSystem::execute_drawTick);
			drawSystem.onExecuteDrawDone();
		}
	}

	public void executeDrawSystem(DrawSystem system) {
		entities.stream()
				.filter(entity -> !entity.isExpired && system.isTargetEntity_drawTick(entity))
				.forEach(system::execute_drawTick);
		system.onExecuteDrawDone();
	}
}
