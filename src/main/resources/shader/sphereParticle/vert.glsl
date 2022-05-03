#version 450

layout(location = 0) in vec4 world_position_and_radius;
layout(location = 1) in vec4 color_inner;
layout(location = 2) in vec4 color_outer;

out particleData{
    vec4 world_position_and_radius;
    vec4 color_inner;
    vec4 color_outer;
}particle;

void main() {
    particle.world_position_and_radius = world_position_and_radius;
    particle.color_inner = color_inner;
    particle.color_outer = color_outer;
}