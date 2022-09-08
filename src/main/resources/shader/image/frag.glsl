#version 450

in fragmentData{
    vec2 textureCoordinate;
}fragment;

layout(binding = 4) uniform sampler2D screenTexture;

layout(location = 0) out vec4 out_color;

void main() {
    vec2 targetUV = fragment.textureCoordinate;
    out_color = texture(screenTexture, targetUV);
}