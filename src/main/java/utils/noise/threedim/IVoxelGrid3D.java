package utils.noise.threedim;

import utils.noise.twodim.IVoxelGrid2D;

public interface IVoxelGrid3D extends IVoxelGrid2D, IGridDimensions3D {

	float[][][] voxels3D();

}
