#version 450

layout(location = 0) in uvec3 in_xyz;

out uvec3 geom_xyz;

void main() {
    geom_xyz = in_xyz;
}