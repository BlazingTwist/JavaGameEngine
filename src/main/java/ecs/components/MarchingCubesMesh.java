package ecs.components;

import rendering.marchingcubes.MarchingCubesGrid3D;
import utils.vector.Vec4f;

public record MarchingCubesMesh(MarchingCubesGrid3D voxelGrid, Vec4f phongData) {
}
