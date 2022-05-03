package ecs.systems;

import ecs.Entity;
import ecs.EntityEraseListener;
import ecs.UpdateSystem;
import ecs.components.Shockwave;
import ecs.components.Transform;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import org.lwjgl.opengl.GL45;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShockwaveManagerSystem implements UpdateSystem, EntityEraseListener {

	private static final Logger logger = LoggerFactory.getLogger(ShockwaveManagerSystem.class);
	private static final int ShockwaveCountField_Bytes = 16;

	private static ShockwaveManagerSystem instance;

	public static ShockwaveManagerSystem getInstance() {
		if (instance == null) {
			instance = new ShockwaveManagerSystem();
		}
		return instance;
	}

	private final ArrayList<Entity> boundShockwaves = new ArrayList<>();
	private final ArrayList<Entity> changedShockwaves = new ArrayList<>();
	private final int[] singleIntArray = new int[]{0};
	private final int glShockwaveSSBO;
	private final FloatBuffer singleShockwaveBuffer = ByteBuffer.allocateDirect(Shockwave.BYTES).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer();

	private boolean boundShockwaveCountChanged = false;
	private int currentPossibleShockwaveCount = 0;

	private ShockwaveManagerSystem() {
		GL45.glGenBuffers(singleIntArray);
		glShockwaveSSBO = singleIntArray[0];
		GL45.glBindBuffer(GL45.GL_SHADER_STORAGE_BUFFER, glShockwaveSSBO);
		GL45.glBufferData(GL45.GL_SHADER_STORAGE_BUFFER, ShockwaveCountField_Bytes, GL45.GL_DYNAMIC_DRAW);
		singleIntArray[0] = 0;
		GL45.glBufferSubData(GL45.GL_SHADER_STORAGE_BUFFER, 0, singleIntArray);
	}

	public void bindShaderData(int bindIndex) {
		GL45.glBindBuffer(GL45.GL_SHADER_STORAGE_BUFFER, glShockwaveSSBO);
		GL45.glBindBufferBase(GL45.GL_SHADER_STORAGE_BUFFER, bindIndex, glShockwaveSSBO);
	}

	@Override
	public boolean isTargetEntity_updateTick(Entity entity) {
		Transform transform = entity.transform;
		Shockwave shockwave = entity.shockwaveComponent;
		return transform != null && shockwave != null
				&& (shockwave.shockwaveDataChanged || shockwave.trackedTransformChangeID != transform.getChangeID());
	}

	@Override
	public synchronized void execute_updateTick(Entity entity) {
		Shockwave shockwave = entity.shockwaveComponent;
		if (shockwave.shockwaveManagerIndex < 0) {
			shockwave.shockwaveManagerIndex = boundShockwaves.size();
			boundShockwaves.add(entity);
			boundShockwaveCountChanged = true;
		}
		if (shockwave.trackedTransformChangeID != entity.transform.getChangeID()) {
			shockwave.trackedTransformChangeID = entity.transform.getChangeID();
		}
		changedShockwaves.add(entity);
	}

	@Override
	public void onExecuteUpdateDone() {
		if (!changedShockwaves.isEmpty() || boundShockwaveCountChanged) {
			GL45.glBindBuffer(GL45.GL_SHADER_STORAGE_BUFFER, glShockwaveSSBO);
			boolean bufferWasResized = false;
			if (boundShockwaveCountChanged) {
				if (boundShockwaves.size() > currentPossibleShockwaveCount) {
					bufferWasResized = true;
					performResize();
				}
				singleIntArray[0] = boundShockwaves.size();
				GL45.glBufferSubData(GL45.GL_SHADER_STORAGE_BUFFER, 0, singleIntArray);
			}
			if (!bufferWasResized && !changedShockwaves.isEmpty()) {
				for (Entity shockwaveEntity : changedShockwaves) {
					Shockwave shockwave = shockwaveEntity.shockwaveComponent;
					shockwave.shockwaveDataChanged = false;
					shockwave.writeToBuffer(singleShockwaveBuffer, shockwaveEntity.transform);
					singleShockwaveBuffer.rewind();
					GL45.glBufferSubData(
							GL45.GL_SHADER_STORAGE_BUFFER,
							(long) ShockwaveCountField_Bytes + ((long) shockwave.shockwaveManagerIndex * Shockwave.BYTES),
							singleShockwaveBuffer
					);
				}
			}
			changedShockwaves.clear();
		}
	}

	@Override
	public void onErase(Entity entity) {
		if (entity.shockwaveComponent == null || entity.shockwaveComponent.shockwaveManagerIndex < 0) {
			return;
		}
		int removeIndex = entity.shockwaveComponent.shockwaveManagerIndex;
		if (boundShockwaves.size() <= removeIndex) {
			logger.error("Tried to remove shockwave with index: {}, but only {} shockwaves bound", removeIndex, boundShockwaves.size());
			return;
		}

		GL45.glBindBuffer(GL45.GL_SHADER_STORAGE_BUFFER, glShockwaveSSBO);
		int lastBoundShockwaveIndex = boundShockwaves.size() - 1;
		singleIntArray[0] = lastBoundShockwaveIndex;
		GL45.glBufferSubData(GL45.GL_SHADER_STORAGE_BUFFER, 0, singleIntArray);
		if (removeIndex != lastBoundShockwaveIndex) {
			Entity movedShockwaveEntity = boundShockwaves.get(lastBoundShockwaveIndex);
			Shockwave movedShockwave = movedShockwaveEntity.shockwaveComponent;
			boundShockwaves.set(removeIndex, movedShockwaveEntity);
			movedShockwave.shockwaveManagerIndex = removeIndex;
			movedShockwave.writeToBuffer(singleShockwaveBuffer, movedShockwaveEntity.transform);
			singleShockwaveBuffer.rewind();
			GL45.glBufferSubData(
					GL45.GL_SHADER_STORAGE_BUFFER,
					ShockwaveCountField_Bytes + ((long) removeIndex * Shockwave.BYTES),
					singleShockwaveBuffer
			);
		}
		boundShockwaves.remove(lastBoundShockwaveIndex);
	}

	private void performResize() {
		currentPossibleShockwaveCount = (int) ((boundShockwaves.size() + 1) * 1.3f);
		int shockwaveDataBytes = currentPossibleShockwaveCount * Shockwave.BYTES;
		GL45.glBufferData(GL45.GL_SHADER_STORAGE_BUFFER, ShockwaveCountField_Bytes + shockwaveDataBytes, GL45.GL_DYNAMIC_DRAW);
		FloatBuffer buffer = ByteBuffer.allocateDirect(shockwaveDataBytes).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer();
		for (Entity boundEntity : boundShockwaves) {
			Shockwave shockwave = boundEntity.shockwaveComponent;
			shockwave.writeToBuffer(buffer, boundEntity.transform);
			shockwave.shockwaveDataChanged = false;
		}
		buffer.rewind();
		GL45.glBufferSubData(GL45.GL_SHADER_STORAGE_BUFFER, ShockwaveCountField_Bytes, buffer);
	}
}
