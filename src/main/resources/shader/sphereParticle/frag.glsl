#version 450

#define LIGHT_TYPE_DIRECTIONAL 0
#define LIGHT_TYPE_SPOT 1
#define LIGHT_TYPE_POINT 2

in fragmentData{
    vec3 world_position;
    float invertNormals;
    vec4 sphere_position_and_squaredRadius;
    vec4 color_inner;
    vec4 color_outer;
}fragment;

layout(location = 0) out vec4 out_color;

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
    vec3 vec_to_eye_normalized = normalize(camera_position - fragment.world_position);
    vec3 sphere_to_camera = camera_position - fragment.sphere_position_and_squaredRadius.xyz;
    float temp_a = 2.0f * dot(vec_to_eye_normalized, sphere_to_camera);
    float temp_b = dot(sphere_to_camera, sphere_to_camera) - fragment.sphere_position_and_squaredRadius.w;
    float half_intersection_thickness_squared = (temp_a * temp_a) - (4.0f * temp_b);
    if (half_intersection_thickness_squared < 0.0f) {
        discard;
    }

    float k = (temp_a - sqrt(half_intersection_thickness_squared)) / 2.0f;
    vec3 sphere_hull_position = (vec_to_eye_normalized * -k) + camera_position;
    vec3 normal_vec = fragment.invertNormals < 0.0f
    ? normalize(fragment.sphere_position_and_squaredRadius.xyz - sphere_hull_position)
    : normalize(sphere_hull_position - fragment.sphere_position_and_squaredRadius.xyz);

    float baseColorWeight = half_intersection_thickness_squared / (fragment.sphere_position_and_squaredRadius.w * 4.0f);
    vec4 baseColor = mix(fragment.color_outer, fragment.color_inner, baseColorWeight);
    float whiteCoreFactor = fragment.invertNormals < 0.0f ? pow(baseColorWeight, 5) : 0.0f;
    float ambientFactor = 0.25f;
    float diffuseFactor = 0.75f;
    float specularFactor = 0.5f;
    float shinynessPower = 0.25f;

    vec3 ambientColor = max(ambientFactor * ambient_light, 0.0f);
    vec3 diffuseColor = vec3(0.0f, 0.0f, 0.0f);
    vec3 specularColor = vec3(0.0f, 0.0f, 0.0f);

    for (int lightIndex = 0; lightIndex < light_count; lightIndex++){
        LightData lightData = allLights[lightIndex];
        if (lightData.type == LIGHT_TYPE_DIRECTIONAL){
            // direction, color, intensity
            float diffuseDot = max(dot(-lightData.direction, normal_vec), 0.0f);
            float specularDot = max(dot(reflect(lightData.direction, normal_vec), vec_to_eye_normalized), 0.0f);
            diffuseColor += diffuseFactor * diffuseDot * lightData.color * lightData.intensity;
            specularColor += specularFactor * 8 * pow(specularDot, shinynessPower * 255) * lightData.color * lightData.intensity;
        } else {
            vec3 vec_from_light_normalized = normalize(sphere_hull_position - lightData.position);
            float lightDistance = length(sphere_hull_position - lightData.position);
            if (lightDistance <= lightData.range
            && (lightData.type != LIGHT_TYPE_SPOT || dot(lightData.direction, vec_from_light_normalized) > lightData.spot_angle_cosine))
            {
                // 4 is the distanceFactor at 'distance == 0.05 * range'
                float distanceFactor = min(0.01f / pow(lightDistance / lightData.range, 2), 4);
                float diffuseDot = max(dot(-vec_from_light_normalized, normal_vec), 0.0f);
                float specularDot = max(dot(reflect(vec_from_light_normalized, normal_vec), vec_to_eye_normalized), 0.0f);
                diffuseColor += diffuseFactor * diffuseDot * lightData.color * lightData.intensity * distanceFactor;
                specularColor += specularFactor * 8 * pow(specularDot, shinynessPower * 255) * lightData.color * lightData.intensity * distanceFactor;
            }
        }
    }

    if (whiteCoreFactor < 0.5f){
        whiteCoreFactor = 0.0f;
    }
    out_color = baseColor * vec4(ambientColor + diffuseColor + specularColor, 1.0f) + whiteCoreFactor * vec4(1.0f, 1.0f, 1.0f, 1.0f);
    //out_color = vec4(diffuseColor, 1.0f);
}