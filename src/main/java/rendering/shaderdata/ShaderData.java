package rendering.shaderdata;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.function.BiConsumer;
import org.jetbrains.annotations.NotNull;

public class ShaderData<Data extends IBufferData> {
	private final FloatBuffer buffer;
	private final BiConsumer<Integer, FloatBuffer> glBindFunction;
	private boolean dirty;

	public boolean isDirty() {
		return dirty;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	public ShaderData(@NotNull Data dataInstance) {
		buffer = ByteBuffer.allocateDirect(dataInstance.bytes()).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer();
		glBindFunction = dataInstance.getBindBufferFunction();
		setData(dataInstance);
	}

	public void setData(Data data) {
		data.writeToBuffer(buffer);
		buffer.rewind();
		setDirty(true);
	}

	public void loadData(int glsl_location) {
		glBindFunction.accept(glsl_location, buffer);
	}
}
