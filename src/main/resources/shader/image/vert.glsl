#version 450

layout(location = 0) in vec2 screenPosition;
layout(location = 1) in vec2 textureCoordinate;

out fragmentData{
    vec2 textureCoordinate;
}fragment;

void main() {
    gl_Position = vec4(screenPosition, 0.0f, 1.0f);
    fragment.textureCoordinate = textureCoordinate;
}