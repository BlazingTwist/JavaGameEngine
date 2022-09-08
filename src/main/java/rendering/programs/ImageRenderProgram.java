package rendering.programs;

import org.lwjgl.opengl.GL45;
import rendering.GeometryBuffer;
import rendering.ShaderProgram;
import rendering.texture.ITexture;

public class ImageRenderProgram {

	private final ShaderProgram program = ShaderProgram.fromBaseDirectory("shader/image", true, false, true);

	private final GeometryBuffer geometryBuffer = new GeometryBuffer(GL45.GL_TRIANGLE_STRIP, 4 * 4, new int[]{2, 2}, false, 0);

	public ImageRenderProgram() {
		geometryBuffer.setData(new float[]{
				-1f, -1f, 0f, 0f,
				1f, -1f, 1f, 0f,
				-1f, 1f, 0f, 1f,
				1f, 1f, 1f, 1f
		});
	}

	public void drawImage(ITexture texture) {
		program.use();
		texture.bindTexture(4);
		geometryBuffer.draw();
	}

	public void delete() {
		program.delete();
		geometryBuffer.delete();
	}
}
