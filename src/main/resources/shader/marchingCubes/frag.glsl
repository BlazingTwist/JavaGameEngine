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

    float distanceSphereSquared = dot(fragment.world_position, fragment.world_position);
    float terrainAngleCos = dot(normalize(fragment.world_position), normal_vec);
    if (distanceSphereSquared < (3.5f * 3.5f * 9f)){
        out_color = vec4(0f, 0.5f, 1f, 1f) * vec4(ambientColor + diffuseColor + specularColor, 1.0f);
    } else if (distanceSphereSquared < (3.55f * 3.55f * 9f) && terrainAngleCos >= 0.7f){
        out_color = vec4(0.800f, 0.789f, 0.337f, 1f) * vec4(ambientColor + diffuseColor + specularColor, 1.0f);
    } else if (distanceSphereSquared < (3.8f * 3.8f * 9f) && terrainAngleCos >= 0.98f){
        vec4 darkGrassColor = vec4(0.066f, 0.521, 0.004, 1f);
        vec4 sandColor = vec4(0.800f, 0.789f, 0.337f, 1f);
        float sandFactor = (terrainAngleCos - 0.98f) * (1f / (1f - 0.98f));
        out_color = mix(darkGrassColor, sandColor, sandFactor) * vec4(ambientColor + diffuseColor + specularColor, 1.0f);
    } else if (distanceSphereSquared < (3.9f * 3.9f * 9f) && terrainAngleCos >= 0.5f){
        vec4 lightGrassColor = vec4(0.079f, 0.614, 0.005, 1f);
        vec4 darkGrassColor = vec4(0.066f, 0.521, 0.004, 1f);
        float lerpFactor = (terrainAngleCos - 0.5f) * (1f / 0.5f);
        vec4 grassMix = mix(darkGrassColor, lightGrassColor, lerpFactor);
        out_color = grassMix * vec4(ambientColor + diffuseColor + specularColor, 1.0f);
    } else if (terrainAngleCos >= 0f){
        vec4 lightRockColor = vec4(0.239f, 0.160f, 0.035f, 1f);
        vec4 darkRockColor = vec4(0.160f, 0.113f, 0.035f, 1f);
        vec4 terrainMix = mix(darkRockColor, lightRockColor, terrainAngleCos);
        float snowFactor = max(0, min(1, (distanceSphereSquared - (4.3f * 4.3f * 9f)) / ((5f * 5f * 9f) - (4.3f * 4.3f * 9f))));
        out_color = mix(terrainMix, vec4(2f, 1.8f, 1.6f, 1f), snowFactor) * vec4(ambientColor + diffuseColor + specularColor, 1.0f);
    } else {
        vec4 darkRockColor = vec4(0.160f, 0.113f, 0.035f, 1f);
        vec4 blackRockColor = vec4(0.080f, 0.050f, 0.018f, 1f);
        out_color = mix(blackRockColor, darkRockColor, 1f + terrainAngleCos) * vec4(ambientColor + diffuseColor + specularColor, 1.0f);
    }
    //out_color = vec4((normal_vec + 1.0f) * 0.5f, 1.0f);
    //out_color = vec4(normal_vec, 1f);
}