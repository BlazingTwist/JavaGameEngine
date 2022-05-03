package rendering;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import org.lwjgl.opengl.GL45;

public class GeometryBuffer {

	private final int glDrawMode;
	private final int vertexArrayObjectID;
	private final int vertexBufferObjectID;
	private final int vertexSize;
	private final boolean indexed;
	private final int indexBufferObjectID;

	private long currentCapacity;
	private long currentIndexCapacity = 0;

	private int vertexCount = 0;
	private int indexCount = 0;

	private boolean isDeleted = false;

	public GeometryBuffer(int glDrawMode, long initialCapacity, int[] floatComponentCounts, boolean indexed, long initialIndexCapacity) {
		this.glDrawMode = glDrawMode;
		this.indexed = indexed;

		vertexArrayObjectID = GL45.glGenVertexArrays();
		GL45.glBindVertexArray(vertexArrayObjectID);

		vertexBufferObjectID = GL45.glGenBuffers();
		GL45.glBindBuffer(GL45.GL_ARRAY_BUFFER, vertexBufferObjectID);

		currentCapacity = initialCapacity * Float.BYTES;
		GL45.glBufferData(GL45.GL_ARRAY_BUFFER, currentCapacity, GL45.GL_STATIC_DRAW);

		{
			vertexSize = Arrays.stream(floatComponentCounts).sum() * Float.BYTES;
			long offset = 0;
			int index = 0;
			for (int floatComponentCount : floatComponentCounts) {
				GL45.glEnableVertexAttribArray(index);
				GL45.glVertexAttribPointer(index, floatComponentCount, GL45.GL_FLOAT, false, vertexSize, offset);
				offset += ((long) floatComponentCount * Float.BYTES);
				index++;
			}
		}

		if (indexed) {
			indexBufferObjectID = GL45.glGenBuffers();
			GL45.glBindBuffer(GL45.GL_ELEMENT_ARRAY_BUFFER, indexBufferObjectID);
			currentIndexCapacity = initialIndexCapacity * Integer.BYTES;
			GL45.glBufferData(GL45.GL_ELEMENT_ARRAY_BUFFER, currentIndexCapacity, GL45.GL_STATIC_DRAW);
		} else {
			indexBufferObjectID = -1;
		}
	}

	public void setData(float[] data) {
		long dataSize = (long) data.length * Float.BYTES;
		GL45.glBindBuffer(GL45.GL_ARRAY_BUFFER, vertexBufferObjectID);
		if (dataSize > currentCapacity) {
			currentCapacity = Math.max(dataSize, currentCapacity * 3 / 2);
			GL45.glBufferData(GL45.GL_ARRAY_BUFFER, currentCapacity, GL45.GL_STATIC_DRAW);
		}
		GL45.glBufferSubData(GL45.GL_ARRAY_BUFFER, 0, data);

		vertexCount = (int) (dataSize / vertexSize);
	}

	private void checkSubDataResize(long dataSize, long offset) {
		if (dataSize + offset > currentCapacity) {
			IntBuffer prevBuffer = ByteBuffer.allocateDirect((int) currentCapacity).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer();
			GL45.glGetBufferSubData(GL45.GL_ARRAY_BUFFER, 0, prevBuffer);
			currentCapacity = Math.max(offset + dataSize, currentCapacity * 3 / 2);
			GL45.glBufferData(GL45.GL_ARRAY_BUFFER, currentCapacity, GL45.GL_STATIC_DRAW);
			prevBuffer.rewind();
			GL45.glBufferSubData(GL45.GL_ARRAY_BUFFER, 0, prevBuffer);
		}
	}

	/**
	 * @param buffer buffer containing the data
	 * @param size   number of vertices in buffer
	 * @param offset number of vertices to use as offset into geometry buffer
	 */
	public void setSubData(FloatBuffer buffer, int size, int offset) {
		GL45.glBindBuffer(GL45.GL_ARRAY_BUFFER, vertexBufferObjectID);
		checkSubDataResize((long) size * vertexSize, (long) offset * vertexSize);
		GL45.glBufferSubData(GL45.GL_ARRAY_BUFFER, (long) offset * vertexSize, buffer);
		vertexCount = Math.max(vertexCount, size + offset);
	}

	public void popLastVertex() {
		if (vertexCount > 0) {
			vertexCount--;
		}
	}

	public void setIndexData(int[] data) {
		long dataSize = (long) data.length * Integer.BYTES;
		GL45.glBindBuffer(GL45.GL_ELEMENT_ARRAY_BUFFER, indexBufferObjectID);
		if (dataSize > currentIndexCapacity) {
			currentIndexCapacity = Math.max(dataSize, currentIndexCapacity * 3 / 2);
			GL45.glBufferData(GL45.GL_ELEMENT_ARRAY_BUFFER, currentIndexCapacity, GL45.GL_STATIC_DRAW);
		}
		GL45.glBufferSubData(GL45.GL_ELEMENT_ARRAY_BUFFER, 0, data);
		indexCount = (int) dataSize;
	}

	public void draw() {
		GL45.glBindVertexArray(vertexArrayObjectID);
		if (indexed) {
			GL45.glBindBuffer(GL45.GL_ELEMENT_ARRAY_BUFFER, indexBufferObjectID);
			GL45.glDrawElementsInstanced(glDrawMode, indexCount, GL45.GL_UNSIGNED_INT, 0, 1);
		} else {
			GL45.glDrawArrays(glDrawMode, 0, vertexCount);
		}
	}

	public void delete() {
		if (isDeleted) {
			return;
		}
		isDeleted = true;
		if (indexed) {
			GL45.glDeleteBuffers(indexBufferObjectID);
		}
		GL45.glDeleteBuffers(vertexBufferObjectID);
		GL45.glDeleteVertexArrays(vertexArrayObjectID);
	}
}
