package ecs.components;

import rendering.marchingcubes.VoxelGrid3D;
import utils.vector.Vec4f;

public record MarchingCubesMesh(VoxelGrid3D voxelGrid, Vec4f phongData) {
}
