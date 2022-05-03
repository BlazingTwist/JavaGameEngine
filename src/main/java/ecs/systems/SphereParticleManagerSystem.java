package ecs.systems;

import ecs.Entity;
import ecs.EntityEraseListener;
import ecs.UpdateSystem;
import ecs.components.SphereParticle;
import ecs.components.Transform;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import org.lwjgl.opengl.GL45;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rendering.GeometryBuffer;

public class SphereParticleManagerSystem implements UpdateSystem, EntityEraseListener {

	private static final Logger logger = LoggerFactory.getLogger(SphereParticleManagerSystem.class);

	private static SphereParticleManagerSystem instance;

	public static SphereParticleManagerSystem getInstance() {
		if (instance == null) {
			instance = new SphereParticleManagerSystem();
		}
		return instance;
	}

	private final GeometryBuffer particleBuffer = new GeometryBuffer(GL45.GL_POINTS, 0, new int[]{4, 4, 4}, false, 0);
	private final ArrayList<Entity> boundParticles = new ArrayList<>();
	private final ArrayList<Entity> changedParticles = new ArrayList<>();

	public GeometryBuffer getParticleBuffer() {
		return particleBuffer;
	}

	private SphereParticleManagerSystem() {
	}

	@Override
	public boolean isTargetEntity_updateTick(Entity entity) {
		Transform transform = entity.transform;
		SphereParticle particle = entity.sphereParticleComponent;
		return transform != null && particle != null
				&& (particle.particleDataChanged || particle.trackedTransformChangeID != transform.getChangeID());
	}

	@Override
	public synchronized void execute_updateTick(Entity entity) {
		SphereParticle particle = entity.sphereParticleComponent;
		if (particle.particleManagerIndex < 0) {
			particle.particleManagerIndex = boundParticles.size();
			boundParticles.add(entity);
		}
		if (particle.trackedTransformChangeID != entity.transform.getChangeID()) {
			particle.trackedTransformChangeID = entity.transform.getChangeID();
		}
		changedParticles.add(entity);
	}

	@Override
	public void onExecuteUpdateDone() {
		if (!changedParticles.isEmpty()) {
			FloatBuffer buffer = ByteBuffer.allocateDirect(SphereParticle.BYTES).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer();
			for (Entity changedParticleEntity : changedParticles) {
				SphereParticle particle = changedParticleEntity.sphereParticleComponent;
				Transform transform = changedParticleEntity.transform;
				particle.particleDataChanged = false;
				particle.writeToBuffer(buffer, transform);
				buffer.rewind();
				particleBuffer.setSubData(buffer, 1, particle.particleManagerIndex);
			}
			changedParticles.clear();
		}
	}

	@Override
	public void onErase(Entity entity) {
		if (entity.sphereParticleComponent == null || entity.sphereParticleComponent.particleManagerIndex < 0) {
			return;
		}
		int removeIndex = entity.sphereParticleComponent.particleManagerIndex;
		if (boundParticles.size() <= removeIndex) {
			logger.error("Tried to remove sphereParticle with index: {}, but only {} particles bound", removeIndex, boundParticles.size());
			return;
		}

		int lastBoundIndex = boundParticles.size() - 1;
		if (removeIndex != lastBoundIndex) {
			Entity movedEntity = boundParticles.get(lastBoundIndex);
			boundParticles.set(removeIndex, movedEntity);
			movedEntity.sphereParticleComponent.particleManagerIndex = removeIndex;
			FloatBuffer buffer = ByteBuffer.allocateDirect(SphereParticle.BYTES).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer();
			movedEntity.sphereParticleComponent.writeToBuffer(buffer, movedEntity.transform);
			buffer.rewind();
			particleBuffer.setSubData(buffer, 1, removeIndex); // overwrite data at removed index
		}
		particleBuffer.popLastVertex();
		boundParticles.remove(lastBoundIndex);
	}
}
