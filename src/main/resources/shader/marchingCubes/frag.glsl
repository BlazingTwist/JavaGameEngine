#version 450

#define LIGHT_TYPE_DIRECTIONAL 0
#define LIGHT_TYPE_SPOT 1
#define LIGHT_TYPE_POINT 2

layout(location = 0) out vec4 out_color;

in fragmentData{
    vec3 world_position;
    vec3 normal;
}fragment;

uniform vec4 phong_data;
uniform vec3 camera_position;
uniform vec3 ambient_light;

struct LightData{
    int type;
    float range;
    float spot_angle_cosine;
    float intensity;
    vec3 position;
    vec3 direction;
    vec3 color;
};

layout(binding = 0) buffer LightBuffer{
    uint light_count;
    LightData allLights[];
};

void main() {
    vec3 normal_vec = fragment.normal;
    vec3 ambientColor = max(phong_data.r * ambient_light, 0.0f);
    vec3 diffuseColor = vec3(0.0f, 0.0f, 0.0f);
    vec3 specularColor = vec3(0.0f, 0.0f, 0.0f);

    // kinda sorta duplicated from demo/frag.glsl - make sure to keep both updated
    // TODO extract this to a separate file
    vec3 vec_to_eye_normalized = normalize(camera_position - fragment.world_position);
    for (int lightIndex = 0; lightIndex < light_count; lightIndex++){
        LightData lightData = allLights[lightIndex];
        if (lightData.type == LIGHT_TYPE_DIRECTIONAL){
            // direction, color, intensity
            float diffuseDot = max(dot(-lightData.direction, normal_vec), 0.0f);
            float specularDot = max(dot(reflect(lightData.direction, normal_vec), vec_to_eye_normalized), 0.0f);
            diffuseColor += phong_data.g * diffuseDot * lightData.color * lightData.intensity;
            specularColor += phong_data.b * 8 * pow(specularDot, phong_data.a * 255) * lightData.color * lightData.intensity;
        } else {
            vec3 vec_to_light_normalized = normalize(fragment.world_position - lightData.position);
            float lightDistance = length(fragment.world_position - lightData.position);
            if (lightDistance <= lightData.range
            && (lightData.type != LIGHT_TYPE_SPOT || dot(lightData.direction, vec_to_light_normalized) > lightData.spot_angle_cosine))
            {
                // 4 is the distanceFactor at 'distance == 0.05 * range'
                float distanceFactor = min(0.01f / pow(lightDistance / lightData.range, 2), 4);
                float diffuseDot = max(dot(-vec_to_light_normalized, normal_vec), 0.0f);
                float specularDot = max(dot(reflect(vec_to_light_normalized, normal_vec), vec_to_eye_normalized), 0.0f);
                diffuseColor += phong_data.g * diffuseDot * lightData.color * lightData.intensity * distanceFactor;
                specularColor += phong_data.b * 8 * pow(specularDot, phong_data.a * 255) * lightData.color * lightData.intensity * distanceFactor;
            }
        }
    }

    //out_color = vec4(ambientColor + diffuseColor + specularColor, 1.0f);
    out_color = vec4((normal_vec + 1.0f) * 0.5f, 1.0f);
    //out_color = vec4(normal_vec, 1f);
}