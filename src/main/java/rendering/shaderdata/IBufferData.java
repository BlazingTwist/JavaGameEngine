package rendering.shaderdata;

import java.nio.FloatBuffer;
import java.util.function.BiConsumer;

public interface IBufferData {
	int bytes();

	void writeToBuffer(FloatBuffer buffer);

	BiConsumer<Integer, FloatBuffer> getBindBufferFunction();
}
