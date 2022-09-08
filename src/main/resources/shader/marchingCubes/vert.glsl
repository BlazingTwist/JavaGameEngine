#version 450

layout(location = 0) in ivec3 in_xyz;

out ivec3 geom_xyz;

void main() {
    geom_xyz = in_xyz;
}