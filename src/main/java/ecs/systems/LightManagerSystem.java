package ecs.systems;

import ecs.Entity;
import ecs.EntityEraseListener;
import ecs.UpdateSystem;
import ecs.components.Light;
import ecs.components.Transform;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import org.lwjgl.opengl.GL45;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LightManagerSystem implements UpdateSystem, EntityEraseListener {

	private static final Logger logger = LoggerFactory.getLogger(LightManagerSystem.class);
	private static final int LightCountField_Bytes = 16;

	private static LightManagerSystem instance;

	public static LightManagerSystem getInstance() {
		if (instance == null) {
			instance = new LightManagerSystem();
		}
		return instance;
	}

	private final ArrayList<Entity> boundLights = new ArrayList<>();
	private final ArrayList<Entity> changedLights = new ArrayList<>();
	private final int[] singleIntArray = new int[]{0};
	private final int glLightSSBO;
	private boolean boundLightCountChanged = false;
	private int currentPossibleLightCount = 0;

	private LightManagerSystem() {
		GL45.glGenBuffers(singleIntArray);
		glLightSSBO = singleIntArray[0];
		GL45.glBindBuffer(GL45.GL_SHADER_STORAGE_BUFFER, glLightSSBO);
		GL45.glBufferData(GL45.GL_SHADER_STORAGE_BUFFER, LightCountField_Bytes, GL45.GL_DYNAMIC_DRAW);
		singleIntArray[0] = 0;
		GL45.glBufferSubData(GL45.GL_SHADER_STORAGE_BUFFER, 0, singleIntArray);
	}

	public void bindShaderData(int bindIndex) {
		GL45.glBindBuffer(GL45.GL_SHADER_STORAGE_BUFFER, glLightSSBO);
		GL45.glBindBufferBase(GL45.GL_SHADER_STORAGE_BUFFER, bindIndex, glLightSSBO);
	}

	@Override
	public boolean isTargetEntity_updateTick(Entity entity) {
		Transform transform = entity.transform;
		Light lightComponent = entity.lightComponent;
		return transform != null && lightComponent != null
				&& (lightComponent.lightDataChanged || lightComponent.trackedTransformChangeID != transform.getChangeID());
	}

	@Override
	public synchronized void execute_updateTick(Entity entity) {
		Light light = entity.lightComponent;
		if (light.lightManagerIndex < 0) {
			light.lightManagerIndex = boundLights.size();
			boundLights.add(entity);
			boundLightCountChanged = true;
		}
		if(light.trackedTransformChangeID != entity.transform.getChangeID()){
			light.trackedTransformChangeID = entity.transform.getChangeID();
		}
		changedLights.add(entity);
	}

	@Override
	public void onExecuteUpdateDone() {
		if (!changedLights.isEmpty() || boundLightCountChanged) {
			GL45.glBindBuffer(GL45.GL_SHADER_STORAGE_BUFFER, glLightSSBO);
			boolean bufferWasResized = false;
			if (boundLightCountChanged) {
				if (boundLights.size() > currentPossibleLightCount) {
					bufferWasResized = true;
					performResize();
				}
				singleIntArray[0] = boundLights.size();
				GL45.glBufferSubData(GL45.GL_SHADER_STORAGE_BUFFER, 0, singleIntArray);
			}
			if (!bufferWasResized && !changedLights.isEmpty()) {
				FloatBuffer buffer = ByteBuffer.allocateDirect(Light.BYTES).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer();
				for (Entity changedLight : changedLights) {
					Light light = changedLight.lightComponent;
					light.lightDataChanged = false;
					light.writeToBuffer(buffer, changedLight.transform);
					buffer.rewind();
					GL45.glBufferSubData(
							GL45.GL_SHADER_STORAGE_BUFFER,
							(long) LightCountField_Bytes + ((long) light.lightManagerIndex * Light.BYTES),
							buffer
					);
				}
			}
			changedLights.clear();
		}
	}

	@Override
	public void onErase(Entity entity) {
		if (entity.lightComponent == null || entity.lightComponent.lightManagerIndex < 0) {
			return;
		}
		int removeIndex = entity.lightComponent.lightManagerIndex;
		if (boundLights.size() <= removeIndex) {
			logger.error("Tried to remove light with index: {}, but only {} lights bound", removeIndex, boundLights.size());
			return;
		}

		GL45.glBindBuffer(GL45.GL_SHADER_STORAGE_BUFFER, glLightSSBO);
		int lastBoundLightIndex = boundLights.size() - 1;
		singleIntArray[0] = lastBoundLightIndex;
		GL45.glBufferSubData(GL45.GL_SHADER_STORAGE_BUFFER, 0, singleIntArray);
		if (removeIndex != lastBoundLightIndex) {
			Entity movedLightEntity = boundLights.get(lastBoundLightIndex);
			Light movedLight = movedLightEntity.lightComponent;
			boundLights.set(removeIndex, movedLightEntity);
			movedLight.lightManagerIndex = removeIndex;
			FloatBuffer buffer = ByteBuffer.allocateDirect(Light.BYTES).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer();
			movedLight.writeToBuffer(buffer, movedLightEntity.transform);
			buffer.rewind();
			GL45.glBufferSubData(
					GL45.GL_SHADER_STORAGE_BUFFER,
					LightCountField_Bytes + ((long) removeIndex * Light.BYTES),
					buffer
			);
		}
		boundLights.remove(lastBoundLightIndex);
	}

	private void performResize() {
		currentPossibleLightCount = (int) ((boundLights.size() + 1) * 1.3f);
		int lightDataBytes = currentPossibleLightCount * Light.BYTES;
		GL45.glBufferData(GL45.GL_SHADER_STORAGE_BUFFER, LightCountField_Bytes + lightDataBytes, GL45.GL_DYNAMIC_DRAW);
		FloatBuffer buffer = ByteBuffer.allocateDirect(lightDataBytes).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer();
		for (Entity boundLightEntity : boundLights) {
			Light boundLight = boundLightEntity.lightComponent;
			boundLight.writeToBuffer(buffer, boundLightEntity.transform);
			boundLight.lightDataChanged = false;
		}
		buffer.rewind();
		GL45.glBufferSubData(GL45.GL_SHADER_STORAGE_BUFFER, LightCountField_Bytes, buffer);
	}
}
