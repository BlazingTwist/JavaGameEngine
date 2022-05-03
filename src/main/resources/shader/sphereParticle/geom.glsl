#version 450

layout (points) in;
layout (triangle_strip, max_vertices = 9) out;

in particleData{
    vec4 world_position_and_radius;
    vec4 color_inner;
    vec4 color_outer;
}particles[];

out fragmentData{
    vec3 world_position;
    float invertNormals;
    vec4 sphere_position_and_squaredRadius;
    vec4 color_inner;
    vec4 color_outer;
}fragment;

uniform mat4 world_to_camera_matrix;
uniform vec3 camera_position;

void main() {
    vec4 sphere_position_and_squaredRadius = particles[0].world_position_and_radius;
    if (sphere_position_and_squaredRadius.w == 0.0f){
        return;
    }
    const float invertNormals = sphere_position_and_squaredRadius.w;
    const float radius = abs(sphere_position_and_squaredRadius.w);
    sphere_position_and_squaredRadius.w *= sphere_position_and_squaredRadius.w;
    
    const vec3 spherePosition = sphere_position_and_squaredRadius.xyz;
    const vec4 cameraSpace_spherePosition = world_to_camera_matrix * vec4(spherePosition, 1.0f);
    const float cameraSpace_depth = cameraSpace_spherePosition.z;
    if (cameraSpace_depth < 0.0f){
        return;
    }
    
    const vec3 sphereToCamera = camera_position - spherePosition;
    const vec3 faceMultiplier = vec3(
    sphereToCamera.x > 0.0f ? 1.0f : -1.0f,
    sphereToCamera.y > 0.0f ? 1.0f : -1.0f,
    sphereToCamera.z > 0.0f ? 1.0f : -1.0f
    );
    const vec4 color_inner = particles[0].color_inner;
    const vec4 color_outer = particles[0].color_outer;

    fragment.world_position = spherePosition + (vec3(radius, -radius, -radius) * faceMultiplier);
    fragment.invertNormals = invertNormals;
    fragment.sphere_position_and_squaredRadius = sphere_position_and_squaredRadius;
    fragment.color_inner = color_inner;
    fragment.color_outer = color_outer;
    gl_Position = world_to_camera_matrix * vec4(fragment.world_position, 1.0f);
    EmitVertex();

    fragment.world_position = spherePosition + (vec3(radius, radius, -radius) * faceMultiplier);
    fragment.invertNormals = invertNormals;
    fragment.sphere_position_and_squaredRadius = sphere_position_and_squaredRadius;
    fragment.color_inner = color_inner;
    fragment.color_outer = color_outer;
    gl_Position = world_to_camera_matrix * vec4(fragment.world_position, 1.0f);
    EmitVertex();

    fragment.world_position = spherePosition + (vec3(radius, -radius, radius) * faceMultiplier);
    fragment.invertNormals = invertNormals;
    fragment.sphere_position_and_squaredRadius = sphere_position_and_squaredRadius;
    fragment.color_inner = color_inner;
    fragment.color_outer = color_outer;
    gl_Position = world_to_camera_matrix * vec4(fragment.world_position, 1.0f);
    EmitVertex();

    fragment.world_position = spherePosition + (vec3(radius, radius, radius) * faceMultiplier);
    fragment.invertNormals = invertNormals;
    fragment.sphere_position_and_squaredRadius = sphere_position_and_squaredRadius;
    fragment.color_inner = color_inner;
    fragment.color_outer = color_outer;
    gl_Position = world_to_camera_matrix * vec4(fragment.world_position, 1.0f);
    EmitVertex();

    fragment.world_position = spherePosition + (vec3(-radius, -radius, radius) * faceMultiplier);
    fragment.invertNormals = invertNormals;
    fragment.sphere_position_and_squaredRadius = sphere_position_and_squaredRadius;
    fragment.color_inner = color_inner;
    fragment.color_outer = color_outer;
    gl_Position = world_to_camera_matrix * vec4(fragment.world_position, 1.0f);
    EmitVertex();

    fragment.world_position = spherePosition + (vec3(-radius, radius, radius) * faceMultiplier);
    fragment.invertNormals = invertNormals;
    fragment.sphere_position_and_squaredRadius = sphere_position_and_squaredRadius;
    fragment.color_inner = color_inner;
    fragment.color_outer = color_outer;
    gl_Position = world_to_camera_matrix * vec4(fragment.world_position, 1.0f);
    EmitVertex();

    fragment.world_position = spherePosition + (vec3(-radius, radius, -radius) * faceMultiplier);
    fragment.invertNormals = invertNormals;
    fragment.sphere_position_and_squaredRadius = sphere_position_and_squaredRadius;
    fragment.color_inner = color_inner;
    fragment.color_outer = color_outer;
    gl_Position = world_to_camera_matrix * vec4(fragment.world_position, 1.0f);
    EmitVertex();

    fragment.world_position = spherePosition + (vec3(radius, radius, radius) * faceMultiplier);
    fragment.invertNormals = invertNormals;
    fragment.sphere_position_and_squaredRadius = sphere_position_and_squaredRadius;
    fragment.color_inner = color_inner;
    fragment.color_outer = color_outer;
    gl_Position = world_to_camera_matrix * vec4(fragment.world_position, 1.0f);
    EmitVertex();

    fragment.world_position = spherePosition + (vec3(radius, radius, -radius) * faceMultiplier);
    fragment.invertNormals = invertNormals;
    fragment.sphere_position_and_squaredRadius = sphere_position_and_squaredRadius;
    fragment.color_inner = color_inner;
    fragment.color_outer = color_outer;
    gl_Position = world_to_camera_matrix * vec4(fragment.world_position, 1.0f);
    EmitVertex();

    EndPrimitive();
}