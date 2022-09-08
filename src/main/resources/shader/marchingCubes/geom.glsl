#version 450

#define x1 (x0 + 1)
#define y1 (y0 + 1)
#define z1 (z0 + 1)
#define xn (x0 - 1)
#define yn (y0 - 1)
#define zn (z0 - 1)

layout (points) in;
layout (triangle_strip, max_vertices = 15) out;

in ivec3 geom_xyz[];

out fragmentData{
    vec3 world_position;
    vec3 normal;
}fragment;

uniform mat4 object_to_world_matrix;
uniform mat4 world_to_camera_matrix;

layout(binding = 1) buffer VoxelBuffer{
    uint xLength;
    uint yLength;
    uint zLength;

    float voxels[];
};

// contains triangulation for the 256 different cube layouts
//  height: 256
//  width: 17
//   first value is edgeBitMap, for example: 0b101101000111 means edges [11,9,8,6,2,1,0] contain vertices
//   second value is amount of faces, may be (0,1,2,3,4 or 5)
//   remaining 15 values are the edge indices for each vertex (3 per face), this data is right-padded with 0xffff
// normals can be computed by taking (vertex[1] - vertex[0]).cross(vertex[2] - vertex[0])
//  with vertex[] containing the edge-vertices of the face
layout(binding = 2) uniform isampler2D tx_cube_lookup;

vec3 mainVertexCache[12] = { vec3(0.0f), vec3(0.0f), vec3(0.0f), vec3(0.0f), vec3(0.0f), vec3(0.0f), vec3(0.0f), vec3(0.0f), vec3(0.0f), vec3(0.0f), vec3(0.0f), vec3(0.0f) };
vec3 normalCache[12] = { vec3(0.0f), vec3(0.0f), vec3(0.0f), vec3(0.0f), vec3(0.0f), vec3(0.0f), vec3(0.0f), vec3(0.0f), vec3(0.0f), vec3(0.0f), vec3(0.0f), vec3(0.0f) };
vec3 normalVertexCache[12] = { vec3(0.0f), vec3(0.0f), vec3(0.0f), vec3(0.0f), vec3(0.0f), vec3(0.0f), vec3(0.0f), vec3(0.0f), vec3(0.0f), vec3(0.0f), vec3(0.0f), vec3(0.0f) };

void gatherNormals(const int x, const int y, const int z, const int targetEdge, const int normalIndex);

float interpolate(const float val0, const float val1);

void main() {
    const int x0 = geom_xyz[0].x;
    const int y0 = geom_xyz[0].y;
    const int z0 = geom_xyz[0].z;
    int cubeIndex = 0;
    if (voxels[(x0 * yLength * zLength) + (y0 * zLength) + z0] < 0) cubeIndex |= 1;
    if (voxels[(x1 * yLength * zLength) + (y0 * zLength) + z0] < 0) cubeIndex |= 2;
    if (voxels[(x1 * yLength * zLength) + (y0 * zLength) + z1] < 0) cubeIndex |= 4;
    if (voxels[(x0 * yLength * zLength) + (y0 * zLength) + z1] < 0) cubeIndex |= 8;
    if (voxels[(x0 * yLength * zLength) + (y1 * zLength) + z0] < 0) cubeIndex |= 16;
    if (voxels[(x1 * yLength * zLength) + (y1 * zLength) + z0] < 0) cubeIndex |= 32;
    if (voxels[(x1 * yLength * zLength) + (y1 * zLength) + z1] < 0) cubeIndex |= 64;
    if (voxels[(x0 * yLength * zLength) + (y1 * zLength) + z1] < 0) cubeIndex |= 128;

    const int edgeValue = texelFetch(tx_cube_lookup, ivec2(0, cubeIndex), 0).r;
    if (edgeValue == 0){
        return;
    }

    if (bool(edgeValue & 1)) {
        mainVertexCache[0] = vec3(x0 + interpolate(voxels[(x0 * yLength * zLength) + (y0 * zLength) + z0], voxels[(x1 * yLength * zLength) + (y0 * zLength) + z0]), y0, z0);
        gatherNormals(x0, y0, zn, 2, 0);
        gatherNormals(x0, yn, z0, 4, 0);
        gatherNormals(x0, yn, zn, 6, 0);
    }
    if (bool(edgeValue & 2)) {
        mainVertexCache[1] = vec3(x1, y0, z0 + interpolate(voxels[(x1 * yLength * zLength) + (y0 * zLength) + z0], voxels[(x1 * yLength * zLength) + (y0 * zLength) + z1]));
        gatherNormals(x1, y0, z0, 3, 1);
        gatherNormals(x0, yn, z0, 5, 1);
        gatherNormals(x1, yn, z0, 7, 1);
    }
    if (bool(edgeValue & 4)) {
        mainVertexCache[2] = vec3(x0 + interpolate(voxels[(x0 * yLength * zLength) + (y0 * zLength) + z1], voxels[(x1 * yLength * zLength) + (y0 * zLength) + z1]), y0, z1);
        gatherNormals(x0, y0, z1, 0, 2);
        gatherNormals(x0, yn, z0, 6, 2);
        gatherNormals(x0, yn, z1, 4, 2);
    }
    if (bool(edgeValue & 8)) {
        mainVertexCache[3] = vec3(x0, y0, z0 + interpolate(voxels[(x0 * yLength * zLength) + (y0 * zLength) + z0], voxels[(x0 * yLength * zLength) + (y0 * zLength) + z1]));
        gatherNormals(xn, y0, z0, 1, 3);
        gatherNormals(x0, yn, z0, 7, 3);
        gatherNormals(xn, yn, z0, 5, 3);
    }
    if (bool(edgeValue & 16)) {
        mainVertexCache[4] = vec3(x0 + interpolate(voxels[(x0 * yLength * zLength) + (y1 * zLength) + z0], voxels[(x1 * yLength * zLength) + (y1 * zLength) + z0]), y1, z0);
        gatherNormals(x0, y0, zn, 6, 4);
        gatherNormals(x0, y1, z0, 0, 4);
        gatherNormals(x0, y1, zn, 2, 4);
    }
    if (bool(edgeValue & 32)) {
        mainVertexCache[5] = vec3(x1, y1, z0 + interpolate(voxels[(x1 * yLength * zLength) + (y1 * zLength) + z0], voxels[(x1 * yLength * zLength) + (y1 * zLength) + z1]));
        gatherNormals(x1, y0, z0, 7, 5);
        gatherNormals(x0, y1, z0, 1, 5);
        gatherNormals(x1, y1, z0, 3, 5);
    }
    if (bool(edgeValue & 64)) {
        mainVertexCache[6] = vec3(x0 + interpolate(voxels[(x0 * yLength * zLength) + (y1 * zLength) + z1], voxels[(x1 * yLength * zLength) + (y1 * zLength) + z1]), y1, z1);
        gatherNormals(x0, y0, z1, 4, 6);
        gatherNormals(x0, y1, z0, 2, 6);
        gatherNormals(x0, y1, z1, 0, 6);
    }
    if (bool(edgeValue & 128)) {
        mainVertexCache[7] = vec3(x0, y1, z0 + interpolate(voxels[(x0 * yLength * zLength) + (y1 * zLength) + z0], voxels[(x0 * yLength * zLength) + (y1 * zLength) + z1]));
        gatherNormals(xn, y0, z0, 5, 7);
        gatherNormals(x0, y1, z0, 3, 7);
        gatherNormals(xn, y1, z0, 1, 7);
    }
    if (bool(edgeValue & 256)) {
        mainVertexCache[8] = vec3(x0, y0 + interpolate(voxels[(x0 * yLength * zLength) + (y0 * zLength) + z0], voxels[(x0 * yLength * zLength) + (y1 * zLength) + z0]), z0);
        gatherNormals(xn, y0, z0, 9, 8);
        gatherNormals(x0, y0, zn, 11, 8);
        gatherNormals(xn, y0, zn, 10, 8);
    }
    if (bool(edgeValue & 512)) {
        mainVertexCache[9] = vec3(x1, y0 + interpolate(voxels[(x1 * yLength * zLength) + (y0 * zLength) + z0], voxels[(x1 * yLength * zLength) + (y1 * zLength) + z0]), z0);
        gatherNormals(x1, y0, z0, 8, 9);
        gatherNormals(x0, y0, zn, 10, 9);
        gatherNormals(x1, y0, zn, 11, 9);
    }
    if (bool(edgeValue & 1024)) {
        mainVertexCache[10] = vec3(x1, y0 + interpolate(voxels[(x1 * yLength * zLength) + (y0 * zLength) + z1], voxels[(x1 * yLength * zLength) + (y1 * zLength) + z1]), z1);
        gatherNormals(x1, y0, z0, 11, 10);
        gatherNormals(x0, y0, z1, 9, 10);
        gatherNormals(x1, y0, z1, 8, 10);
    }
    if (bool(edgeValue & 2048)) {
        mainVertexCache[11] = vec3(x0, y0 + interpolate(voxels[(x0 * yLength * zLength) + (y0 * zLength) + z1], voxels[(x0 * yLength * zLength) + (y1 * zLength) + z1]), z1);
        gatherNormals(xn, y0, z0, 10, 11);
        gatherNormals(x0, y0, z1, 8, 11);
        gatherNormals(xn, y0, z1, 9, 11);
    }

    const int faceCount = texelFetch(tx_cube_lookup, ivec2(1, cubeIndex), 0).r;
    if (faceCount <= 0){
        return;
    }
    for (int i = 0; i < faceCount; i++) {
        const int edge0 = texelFetch(tx_cube_lookup, ivec2((i * 3) + 2, cubeIndex), 0).r;
        const int edge1 = texelFetch(tx_cube_lookup, ivec2((i * 3) + 3, cubeIndex), 0).r;
        const int edge2 = texelFetch(tx_cube_lookup, ivec2((i * 3) + 4, cubeIndex), 0).r;

        const vec3 pos0 = mainVertexCache[edge0];
        const vec3 pos1 = mainVertexCache[edge1];
        const vec3 pos2 = mainVertexCache[edge2];
        const vec3 faceNormal = cross(pos1 - pos0, pos2 - pos0);
        const vec3 faceNormalScaled = faceNormal / dot(faceNormal, faceNormal);

        normalCache[edge0] = faceNormalScaled + normalCache[edge0];
        normalCache[edge1] = faceNormalScaled + normalCache[edge1];
        normalCache[edge2] = faceNormalScaled + normalCache[edge2];
    }
    const vec3 positionScale = vec3(2f / (float(xLength) - 1f), 2f / (float(yLength) - 1f), 2f / (float(zLength) - 1f));
    for (int i = 0; i < faceCount; i++) {
        const int edge0 = texelFetch(tx_cube_lookup, ivec2((i * 3) + 2, cubeIndex), 0).r;
        const int edge1 = texelFetch(tx_cube_lookup, ivec2((i * 3) + 3, cubeIndex), 0).r;
        const int edge2 = texelFetch(tx_cube_lookup, ivec2((i * 3) + 4, cubeIndex), 0).r;

        const vec3 pos0 = (mainVertexCache[edge0] * positionScale) - 1f;
        const vec3 pos1 = (mainVertexCache[edge1] * positionScale) - 1f;
        const vec3 pos2 = (mainVertexCache[edge2] * positionScale) - 1f;

        const mat3 objToWorldMat3 = mat3(object_to_world_matrix);

        fragment.world_position = (object_to_world_matrix * vec4(pos0, 1f)).xyz;
        fragment.normal = normalize(objToWorldMat3 * normalCache[edge0]);
        gl_Position = world_to_camera_matrix * vec4(fragment.world_position, 1.0f);
        EmitVertex();

        fragment.world_position = (object_to_world_matrix * vec4(pos1, 1f)).xyz;
        fragment.normal = normalize(objToWorldMat3 * normalCache[edge1]);
        gl_Position = world_to_camera_matrix * vec4(fragment.world_position, 1.0f);
        EmitVertex();

        fragment.world_position = (object_to_world_matrix * vec4(pos2, 1f)).xyz;
        fragment.normal = normalize(objToWorldMat3 * normalCache[edge2]);
        gl_Position = world_to_camera_matrix * vec4(fragment.world_position, 1.0f);
        EmitVertex();

        EndPrimitive();
    }
}

void gatherNormals(const int x0, const int y0, const int z0, const int targetEdge, const int normalIndex) {
    if (x0 < 0 || y0 < 0 || z0 < 0 || x0 >= (xLength - 1) || y0 >= (yLength - 1) || z0 >= (zLength - 1)) {
        return;
    }

    int cubeIndex = 0;
    if (voxels[(x0 * yLength * zLength) + (y0 * zLength) + z0] < 0) cubeIndex |= 1;
    if (voxels[(x1 * yLength * zLength) + (y0 * zLength) + z0] < 0) cubeIndex |= 2;
    if (voxels[(x1 * yLength * zLength) + (y0 * zLength) + z1] < 0) cubeIndex |= 4;
    if (voxels[(x0 * yLength * zLength) + (y0 * zLength) + z1] < 0) cubeIndex |= 8;
    if (voxels[(x0 * yLength * zLength) + (y1 * zLength) + z0] < 0) cubeIndex |= 16;
    if (voxels[(x1 * yLength * zLength) + (y1 * zLength) + z0] < 0) cubeIndex |= 32;
    if (voxels[(x1 * yLength * zLength) + (y1 * zLength) + z1] < 0) cubeIndex |= 64;
    if (voxels[(x0 * yLength * zLength) + (y1 * zLength) + z1] < 0) cubeIndex |= 128;

    const int edgeValue = texelFetch(tx_cube_lookup, ivec2(0, cubeIndex), 0).r;
    if (bool(edgeValue & 1)) normalVertexCache[0] = vec3(x0 + interpolate(voxels[(x0 * yLength * zLength) + (y0 * zLength) + z0], voxels[(x1 * yLength * zLength) + (y0 * zLength) + z0]), y0, z0);
    if (bool(edgeValue & 2)) normalVertexCache[1] = vec3(x1, y0, z0 + interpolate(voxels[(x1 * yLength * zLength) + (y0 * zLength) + z0], voxels[(x1 * yLength * zLength) + (y0 * zLength) + z1]));
    if (bool(edgeValue & 4)) normalVertexCache[2] = vec3(x0 + interpolate(voxels[(x0 * yLength * zLength) + (y0 * zLength) + z1], voxels[(x1 * yLength * zLength) + (y0 * zLength) + z1]), y0, z1);
    if (bool(edgeValue & 8)) normalVertexCache[3] = vec3(x0, y0, z0 + interpolate(voxels[(x0 * yLength * zLength) + (y0 * zLength) + z0], voxels[(x0 * yLength * zLength) + (y0 * zLength) + z1]));
    if (bool(edgeValue & 16)) normalVertexCache[4] = vec3(x0 + interpolate(voxels[(x0 * yLength * zLength) + (y1 * zLength) + z0], voxels[(x1 * yLength * zLength) + (y1 * zLength) + z0]), y1, z0);
    if (bool(edgeValue & 32)) normalVertexCache[5] = vec3(x1, y1, z0 + interpolate(voxels[(x1 * yLength * zLength) + (y1 * zLength) + z0], voxels[(x1 * yLength * zLength) + (y1 * zLength) + z1]));
    if (bool(edgeValue & 64)) normalVertexCache[6] = vec3(x0 + interpolate(voxels[(x0 * yLength * zLength) + (y1 * zLength) + z1], voxels[(x1 * yLength * zLength) + (y1 * zLength) + z1]), y1, z1);
    if (bool(edgeValue & 128)) normalVertexCache[7] = vec3(x0, y1, z0 + interpolate(voxels[(x0 * yLength * zLength) + (y1 * zLength) + z0], voxels[(x0 * yLength * zLength) + (y1 * zLength) + z1]));
    if (bool(edgeValue & 256)) normalVertexCache[8] = vec3(x0, y0 + interpolate(voxels[(x0 * yLength * zLength) + (y0 * zLength) + z0], voxels[(x0 * yLength * zLength) + (y1 * zLength) + z0]), z0);
    if (bool(edgeValue & 512)) normalVertexCache[9] = vec3(x1, y0 + interpolate(voxels[(x1 * yLength * zLength) + (y0 * zLength) + z0], voxels[(x1 * yLength * zLength) + (y1 * zLength) + z0]), z0);
    if (bool(edgeValue & 1024)) normalVertexCache[10] = vec3(x1, y0 + interpolate(voxels[(x1 * yLength * zLength) + (y0 * zLength) + z1], voxels[(x1 * yLength * zLength) + (y1 * zLength) + z1]), z1);
    if (bool(edgeValue & 2048)) normalVertexCache[11] = vec3(x0, y0 + interpolate(voxels[(x0 * yLength * zLength) + (y0 * zLength) + z1], voxels[(x0 * yLength * zLength) + (y1 * zLength) + z1]), z1);

    const int faceCount = texelFetch(tx_cube_lookup, ivec2(1, cubeIndex), 0).r;
    for (int i = 0; i < faceCount; i++) {
        const int edge0 = texelFetch(tx_cube_lookup, ivec2((i * 3) + 2, cubeIndex), 0).r;
        const int edge1 = texelFetch(tx_cube_lookup, ivec2((i * 3) + 3, cubeIndex), 0).r;
        const int edge2 = texelFetch(tx_cube_lookup, ivec2((i * 3) + 4, cubeIndex), 0).r;
        if (edge0 == targetEdge || edge1 == targetEdge || edge2 == targetEdge) {
            const vec3 pos0 = normalVertexCache[edge0];
            const vec3 faceNormal = cross(normalVertexCache[edge1] - pos0, normalVertexCache[edge2] - pos0);
            normalCache[normalIndex] = (faceNormal / dot(faceNormal, faceNormal)) + normalCache[normalIndex];
            //normalCache[normalIndex] = faceNormal + normalCache[normalIndex];
        }
    }
}

// output examples:
//  0,0 -> 0.5
//  -1,+1 -> 0.5
//  0,1 -> 0
//  1,0 -> 1
//  2,0.25 -> 0.8888
float interpolate(const float val0, const float val1) {
    if (abs(val0 - val1) < 0.00001f){
        return 0.5f;
    }
    const float abs0 = abs(val0) + 0.00001f;
    const float abs1 = abs(val1) + 0.00001f;
    return abs0 / (abs0 + abs1);
}