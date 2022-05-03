#version 450

#define x1 (x + 1u)
#define y1 (y + 1u)
#define z1 (z + 1u)

layout (points) in;
layout (triangle_strip, max_vertices = 15) out;

in uvec3 geom_xyz[];

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

void gatherNormals(const uint x, const uint y, const uint z, const uint targetEdge, const uint normalIndex);

float interpolate(const float val0, const float val1);

void main() {
    const uint x = geom_xyz[0].x;
    const uint y = geom_xyz[0].y;
    const uint z = geom_xyz[0].z;
    uint cubeIndex = 0;
    if (voxels[(x * yLength * zLength) + (y * zLength) + (z)] < 0) cubeIndex |= 1u;
    if (voxels[(x1 * yLength * zLength) + (y * zLength) + (z)] < 0) cubeIndex |= 2u;
    if (voxels[(x1 * yLength * zLength) + (y * zLength) + (z1)] < 0) cubeIndex |= 4u;
    if (voxels[(x * yLength * zLength) + (y * zLength) + (z1)] < 0) cubeIndex |= 8u;
    if (voxels[(x * yLength * zLength) + (y1 * zLength) + (z)] < 0) cubeIndex |= 16u;
    if (voxels[(x1 * yLength * zLength) + (y1 * zLength) + (z)] < 0) cubeIndex |= 32u;
    if (voxels[(x1 * yLength * zLength) + (y1 * zLength) + (z1)] < 0) cubeIndex |= 64u;
    if (voxels[(x * yLength * zLength) + (y1 * zLength) + (z1)] < 0) cubeIndex |= 128u;

    const uint edgeValue = texelFetch(tx_cube_lookup, ivec2(0, cubeIndex), 0).r;
    if (edgeValue == 0){
        return;
    }

    if (bool(edgeValue & 1u)) {
        mainVertexCache[0] = vec3(x + interpolate(voxels[(x * yLength * zLength) + (y * zLength) + (z)], voxels[(x1 * yLength * zLength) + (y * zLength) + (z)]), y + 0f, z + 0f);
        gatherNormals(x, y, z - 1u, 2u, 0u);
        gatherNormals(x, y - 1u, z, 4u, 0u);
        gatherNormals(x, y - 1u, z - 1u, 6u, 0u);
    }
    if (bool(edgeValue & 2u)) {
        mainVertexCache[1] = vec3(x + 1f, y + 0f, z + interpolate(voxels[(x1 * yLength * zLength) + (y * zLength) + (z)], voxels[(x1 * yLength * zLength) + (y * zLength) + (z + 1)]));
        gatherNormals(x + 1u, y, z, 3u, 1u);
        gatherNormals(x, y - 1u, z, 5u, 1u);
        gatherNormals(x + 1u, y - 1u, z, 7u, 1u);
    }
    if (bool(edgeValue & 4u)) {
        mainVertexCache[2] = vec3(x + interpolate(voxels[(x * yLength * zLength) + (y * zLength) + (z + 1)], voxels[(x1 * yLength * zLength) + (y * zLength) + (z + 1)]), y + 0f, z + 1f);
        gatherNormals(x, y, z + 1u, 0u, 2u);
        gatherNormals(x, y - 1u, z, 6u, 2u);
        gatherNormals(x, y - 1u, z + 1u, 4u, 2u);
    }
    if (bool(edgeValue & 8u)) {
        mainVertexCache[3] = vec3(x + 0f, y + 0f, z + interpolate(voxels[(x * yLength * zLength) + (y * zLength) + (z)], voxels[(x * yLength * zLength) + (y * zLength) + (z + 1)]));
        gatherNormals(x - 1u, y, z, 1u, 3u);
        gatherNormals(x, y - 1u, z, 7u, 3u);
        gatherNormals(x - 1u, y - 1u, z, 5u, 3u);
    }
    if (bool(edgeValue & 16u)) {
        mainVertexCache[4] = vec3(x + interpolate(voxels[(x * yLength * zLength) + (y1 * zLength) + (z)], voxels[(x1 * yLength * zLength) + (y1 * zLength) + (z)]), y + 1f, z + 0f);
        gatherNormals(x, y, z - 1u, 6u, 4u);
        gatherNormals(x, y + 1u, z, 0u, 4u);
        gatherNormals(x, y + 1u, z - 1u, 2u, 4u);
    }
    if (bool(edgeValue & 32u)) {
        mainVertexCache[5] = vec3(x + 1f, y + 1f, z + interpolate(voxels[(x1 * yLength * zLength) + (y1 * zLength) + (z)], voxels[(x1 * yLength * zLength) + (y1 * zLength) + (z + 1)]));
        gatherNormals(x + 1u, y, z, 7u, 5u);
        gatherNormals(x, y + 1u, z, 1u, 5u);
        gatherNormals(x + 1u, y + 1u, z, 3u, 5u);
    }
    if (bool(edgeValue & 64u)) {
        mainVertexCache[6] = vec3(x + interpolate(voxels[(x * yLength * zLength) + (y1 * zLength) + (z + 1)], voxels[(x1 * yLength * zLength) + (y1 * zLength) + (z + 1)]), y + 1f, z + 1f);
        gatherNormals(x, y, z + 1u, 4u, 6u);
        gatherNormals(x, y + 1u, z, 2u, 6u);
        gatherNormals(x, y + 1u, z + 1u, 0u, 6u);
    }
    if (bool(edgeValue & 128u)) {
        mainVertexCache[7] = vec3(x + 0f, y + 1f, z + interpolate(voxels[(x * yLength * zLength) + (y1 * zLength) + (z)], voxels[(x * yLength * zLength) + (y1 * zLength) + (z + 1)]));
        gatherNormals(x - 1u, y, z, 5u, 7u);
        gatherNormals(x, y + 1u, z, 3u, 7u);
        gatherNormals(x - 1u, y + 1u, z, 1u, 7u);
    }
    if (bool(edgeValue & 256u)) {
        mainVertexCache[8] = vec3(x + 0f, y + interpolate(voxels[(x * yLength * zLength) + (y * zLength) + (z)], voxels[(x * yLength * zLength) + (y1 * zLength) + (z)]), z + 0f);
        gatherNormals(x - 1u, y, z, 9u, 8u);
        gatherNormals(x, y, z - 1u, 11u, 8u);
        gatherNormals(x - 1u, y, z - 1u, 10u, 8u);
    }
    if (bool(edgeValue & 512u)) {
        mainVertexCache[9] = vec3(x + 1f, y + interpolate(voxels[(x1 * yLength * zLength) + (y * zLength) + (z)], voxels[(x1 * yLength * zLength) + (y1 * zLength) + (z)]), z + 0f);
        gatherNormals(x + 1u, y, z, 8u, 9u);
        gatherNormals(x, y, z - 1u, 10u, 9u);
        gatherNormals(x + 1u, y, z - 1u, 11u, 9u);
    }
    if (bool(edgeValue & 1024u)) {
        mainVertexCache[10] = vec3(x + 1f, y + interpolate(voxels[(x1 * yLength * zLength) + (y * zLength) + (z + 1)], voxels[(x1 * yLength * zLength) + (y1 * zLength) + (z + 1)]), z + 1f);
        gatherNormals(x + 1u, y, z, 11u, 10u);
        gatherNormals(x, y, z + 1u, 9u, 10u);
        gatherNormals(x + 1u, y, z + 1u, 8u, 10u);
    }
    if (bool(edgeValue & 2048u)) {
        mainVertexCache[11] = vec3(x + 0f, y + interpolate(voxels[(x * yLength * zLength) + (y * zLength) + (z + 1)], voxels[(x * yLength * zLength) + (y1 * zLength) + (z + 1)]), z + 1f);
        gatherNormals(x - 1u, y, z, 10u, 11u);
        gatherNormals(x, y, z + 1u, 8u, 11u);
        gatherNormals(x - 1u, y, z + 1u, 9u, 11u);
    }

    const uint faceCount = texelFetch(tx_cube_lookup, ivec2(1, cubeIndex), 0).r;
    for (int i = 0; i < faceCount; i++) {
        const uint tri0 = texelFetch(tx_cube_lookup, ivec2((i * 3) + 2, cubeIndex), 0).r;
        const uint tri1 = texelFetch(tx_cube_lookup, ivec2((i * 3) + 3, cubeIndex), 0).r;
        const uint tri2 = texelFetch(tx_cube_lookup, ivec2((i * 3) + 4, cubeIndex), 0).r;

        const vec3 pos0 = mainVertexCache[tri0];
        const vec3 pos1 = mainVertexCache[tri1];
        const vec3 pos2 = mainVertexCache[tri2];
        const vec3 faceNormal = cross(pos1 - pos0, pos2 - pos0);
        const vec3 faceNormalScaled = faceNormal / dot(faceNormal, faceNormal);

        const mat3 objToWorldMat3 = mat3(object_to_world_matrix);

        fragment.world_position = (object_to_world_matrix * vec4(pos0, 1f)).xyz;
        fragment.normal = normalize(objToWorldMat3 * (faceNormalScaled + normalCache[tri0]));
        gl_Position = world_to_camera_matrix * vec4(fragment.world_position, 1.0f);
        EmitVertex();

        fragment.world_position = (object_to_world_matrix * vec4(pos1, 1f)).xyz;
        fragment.normal = normalize(objToWorldMat3 * (faceNormalScaled + normalCache[tri1]));
        gl_Position = world_to_camera_matrix * vec4(fragment.world_position, 1.0f);
        EmitVertex();

        fragment.world_position = (object_to_world_matrix * vec4(pos2, 1f)).xyz;
        fragment.normal = normalize(objToWorldMat3 * (faceNormalScaled + normalCache[tri2]));
        gl_Position = world_to_camera_matrix * vec4(fragment.world_position, 1.0f);
        EmitVertex();

        EndPrimitive();
    }
}

void gatherNormals(const uint x, const uint y, const uint z, const uint targetEdge, const uint normalIndex) {
    if (x < 0 || y < 0 || z < 0 || x >= (xLength - 1) || y >= (yLength - 1) || z >= (zLength - 1)) {
        return;
    }

    uint cubeIndex = 0;
    if (voxels[(x * yLength * zLength) + (y * zLength) + (z)] < 0) cubeIndex |= 1u;
    if (voxels[(x1 * yLength * zLength) + (y * zLength) + (z)] < 0) cubeIndex |= 2u;
    if (voxels[(x1 * yLength * zLength) + (y * zLength) + (z + 1)] < 0) cubeIndex |= 4u;
    if (voxels[(x * yLength * zLength) + (y * zLength) + (z + 1)] < 0) cubeIndex |= 8u;
    if (voxels[(x * yLength * zLength) + (y1 * zLength) + (z)] < 0) cubeIndex |= 16u;
    if (voxels[(x1 * yLength * zLength) + (y1 * zLength) + (z)] < 0) cubeIndex |= 32u;
    if (voxels[(x1 * yLength * zLength) + (y1 * zLength) + (z + 1)] < 0) cubeIndex |= 64u;
    if (voxels[(x * yLength * zLength) + (y1 * zLength) + (z + 1)] < 0) cubeIndex |= 128u;

    const uint edgeValue = texelFetch(tx_cube_lookup, ivec2(0, cubeIndex), 0).r;
    if (bool(edgeValue & 1u)) normalVertexCache[0] = vec3(x + interpolate(voxels[(x * yLength * zLength) + (y * zLength) + (z)], voxels[(x1 * yLength * zLength) + (y * zLength) + (z)]), y + 0f, z + 0f);
    if (bool(edgeValue & 2u)) normalVertexCache[1] = vec3(x + 1f, y + 0f, z + interpolate(voxels[(x1 * yLength * zLength) + (y * zLength) + (z)], voxels[(x1 * yLength * zLength) + (y * zLength) + (z + 1)]));
    if (bool(edgeValue & 4u)) normalVertexCache[2] = vec3(x + interpolate(voxels[(x * yLength * zLength) + (y * zLength) + (z + 1)], voxels[(x1 * yLength * zLength) + (y * zLength) + (z + 1)]), y + 0f, z + 1f);
    if (bool(edgeValue & 8u)) normalVertexCache[3] = vec3(x + 0f, y + 0f, z + interpolate(voxels[(x * yLength * zLength) + (y * zLength) + (z)], voxels[(x * yLength * zLength) + (y * zLength) + (z + 1)]));
    if (bool(edgeValue & 16u)) normalVertexCache[4] = vec3(x + interpolate(voxels[(x * yLength * zLength) + (y1 * zLength) + (z)], voxels[(x1 * yLength * zLength) + (y1 * zLength) + (z)]), y + 1f, z + 0f);
    if (bool(edgeValue & 32u)) normalVertexCache[5] = vec3(x + 1f, y + 1f, z + interpolate(voxels[(x1 * yLength * zLength) + (y1 * zLength) + (z)], voxels[(x1 * yLength * zLength) + (y1 * zLength) + (z + 1)]));
    if (bool(edgeValue & 64u)) normalVertexCache[6] = vec3(x + interpolate(voxels[(x * yLength * zLength) + (y1 * zLength) + (z + 1)], voxels[(x1 * yLength * zLength) + (y1 * zLength) + (z + 1)]), y + 1f, z + 1f);
    if (bool(edgeValue & 128u)) normalVertexCache[7] = vec3(x + 0f, y + 1f, z + interpolate(voxels[(x * yLength * zLength) + (y1 * zLength) + (z)], voxels[(x * yLength * zLength) + (y1 * zLength) + (z + 1)]));
    if (bool(edgeValue & 256u)) normalVertexCache[8] = vec3(x + 0f, y + interpolate(voxels[(x * yLength * zLength) + (y * zLength) + (z)], voxels[(x * yLength * zLength) + (y1 * zLength) + (z)]), z + 0f);
    if (bool(edgeValue & 512u)) normalVertexCache[9] = vec3(x + 1f, y + interpolate(voxels[(x1 * yLength * zLength) + (y * zLength) + (z)], voxels[(x1 * yLength * zLength) + (y1 * zLength) + (z)]), z + 0f);
    if (bool(edgeValue & 1024u)) normalVertexCache[10] = vec3(x + 1f, y + interpolate(voxels[(x1 * yLength * zLength) + (y * zLength) + (z + 1)], voxels[(x1 * yLength * zLength) + (y1 * zLength) + (z + 1)]), z + 1f);
    if (bool(edgeValue & 2048u)) normalVertexCache[11] = vec3(x + 0f, y + interpolate(voxels[(x * yLength * zLength) + (y * zLength) + (z + 1)], voxels[(x * yLength * zLength) + (y1 * zLength) + (z + 1)]), z + 1f);

    const uint faceCount = texelFetch(tx_cube_lookup, ivec2(1, cubeIndex), 0).r;
    for (int i = 0; i < faceCount; i++) {
        const uint tri0 = texelFetch(tx_cube_lookup, ivec2((i * 3) + 2, cubeIndex), 0).r;
        const uint tri1 = texelFetch(tx_cube_lookup, ivec2((i * 3) + 3, cubeIndex), 0).r;
        const uint tri2 = texelFetch(tx_cube_lookup, ivec2((i * 3) + 4, cubeIndex), 0).r;
        if (tri0 == targetEdge || tri1 == targetEdge || tri2 == targetEdge) {
            const vec3 pos0 = normalVertexCache[tri0];
            const vec3 faceNormal = cross(normalVertexCache[tri1] - pos0, normalVertexCache[tri2] - pos0);
            normalCache[normalIndex] = (faceNormal / dot(faceNormal, faceNormal)) + normalCache[normalIndex];
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
    const float abs0 = abs(val0);
    const float abs1 = abs(val1);
    return abs0 / (abs0 + abs1);
}