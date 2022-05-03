#version 450

in fragmentData{
    vec2 textureCoordinate;
}fragment;

layout(binding = 4) uniform sampler2D screenTexture;

layout(location = 0) out vec4 out_color;

struct ShockwaveData{
    float radius;
    float intensity;
    float thickness;
    vec3 position;
};

layout(binding = 5) buffer ShockwaveBuffer{
    uint shockwave_count;
    ShockwaveData allShockwaves[];
};

uniform mat4 world_to_camera_matrix;
uniform vec3 camera_up_vector;
uniform vec3 camera_right_vector;

vec2 worldSpaceToUVSpace(vec3 worldPosition) {
    vec4 projectedPosition = world_to_camera_matrix * vec4(worldPosition, 1.0f);
    return (projectedPosition.xy / projectedPosition.z * 0.5f) + 0.5f;
}

void main() {
    vec2 targetUV = fragment.textureCoordinate;

    for (int shockwaveIndex = 0; shockwaveIndex < shockwave_count; shockwaveIndex++){
        ShockwaveData waveData = allShockwaves[shockwaveIndex];
        vec4 projectedCenterPosition = world_to_camera_matrix * vec4(waveData.position, 1.0f);
        if (projectedCenterPosition.z < 0){
            continue;
        }

        vec2 uvSpaceWaveCenter = (projectedCenterPosition.xy / projectedCenterPosition.z * 0.5f) + 0.5f;
        vec2 uvSpaceTopWaveEdge = worldSpaceToUVSpace(waveData.position + (camera_up_vector * waveData.radius));
        vec2 uvSpaceRightWaveEdge = worldSpaceToUVSpace(waveData.position + (camera_right_vector * waveData.radius));
        float radiusDeltaX = (uvSpaceRightWaveEdge - uvSpaceWaveCenter).x;
        float radiusDeltaY = (uvSpaceTopWaveEdge - uvSpaceWaveCenter).y;

        vec2 uvSpace_waveCenter_to_screenPosition = (fragment.textureCoordinate - uvSpaceWaveCenter) / vec2(radiusDeltaX, radiusDeltaY);
        float shiftFactor = 1.0f - abs(1.0f - length(uvSpace_waveCenter_to_screenPosition));
        if (shiftFactor > 0.0f){
            shiftFactor = pow(shiftFactor, 8 / waveData.thickness);
            targetUV += normalize(uvSpace_waveCenter_to_screenPosition) * shiftFactor * waveData.intensity;
        }
    }

    out_color = texture(screenTexture, targetUV);
    //out_color = vec4(radiusDeltaX, radiusDeltaY, 0.0f, 1.0f);
    //out_color = vec4(worldSpaceToUVSpace(shockwavePosition + camera_right_vector).x, 0.0f, 0.0f, 1.0f);
    //out_color = vec4(radius, intensity, enabled, 1.0f);
    //out_color = vec4(1.0f, 1.0f, 1.0f, 1.0f);
}