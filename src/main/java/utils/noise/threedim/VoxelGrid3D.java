package utils.noise.threedim;

public class VoxelGrid3D implements IVoxelGrid3D {

	protected final int xDimension;
	protected final int yDimension;
	protected final int zDimension;
	protected final float[][][] voxels;

	public VoxelGrid3D(int xDimension, int yDimension, int zDimension) {
		this.xDimension = xDimension;
		this.yDimension = yDimension;
		this.zDimension = zDimension;
		this.voxels = new float[xDimension][yDimension][zDimension];
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
	public int zDimension() {
		return zDimension;
	}

	@Override
	public float[][] voxels2D() {
		// this technically returns the YZ plane, but "fixing" this would require a lot of refactoring
		return voxels[0];
	}

	@Override
	public float[][][] voxels3D() {
		return voxels;
	}
}
