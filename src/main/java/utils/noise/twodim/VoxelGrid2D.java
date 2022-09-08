package utils.noise.twodim;

public class VoxelGrid2D implements IVoxelGrid2D {

	protected final int xDimension;
	protected final int yDimension;
	protected final float[][] voxels;

	public VoxelGrid2D(int xDimension, int yDimension) {
		this.xDimension = xDimension;
		this.yDimension = yDimension;
		this.voxels = new float[xDimension][yDimension];
	}

	@Override
	public int xDimension() {
		return xDimension;
	}

	@Override
	public int yDimension() {
		return yDimension;
	}

	@Override
	public float[][] voxels2D() {
		return voxels;
	}
}
