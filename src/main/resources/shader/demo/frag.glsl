#version 450

#define LIGHT_TYPE_DIRECTIONAL 0
#define LIGHT_TYPE_SPOT 1
#define LIGHT_TYPE_POINT 2

#define TXFLAG_NORMAL_DATA 1
#define TXFLAG_HEIGHT_DATA 2

in fragmentData{
    vec2 uv_coord;
    vec3 world_position;
    mat3 tbn_matrix;
}fragment;

layout(binding = 0) uniform sampler2D tx_color;
layout(binding = 1) uniform sampler2D tx_phong;// stores ambient/diffuse/specular/shinyness weights in r/g/b/a respectively
layout(binding = 2) uniform sampler2D tx_normal;// normal map in tangent space (r/g/b as right/up/out)
layout(binding = 3) uniform sampler2D tx_height;// stores height in range [0,1], 1 being on the surface, 0 being the maximum depth into the surface

layout(location = 0) out vec4 out_color;

uniform int textureFlags;
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

layout(binding = 4) buffer LightBuffer{
    uint light_count;
    LightData allLights[];
};

void main() {
    vec3 vec_to_eye_normalized = normalize(camera_position - fragment.world_position);
    vec2 currentUV = fragment.uv_coord;

    if ((textureFlags & TXFLAG_HEIGHT_DATA) > 0) {
        // quality setting for heightMap // should we turn this into a uniform rather than hard-coding?
        float height_map_scale = 0.05f;
        float minLayers = 8.0f;
        float maxLayers = 64.0f;
        // test more layers for shallower view angles
        float normal_dot_vecToEye = dot(fragment.tbn_matrix[2], vec_to_eye_normalized);
        float numLayers = mix(maxLayers, minLayers, abs(normal_dot_vecToEye));
        float layerHeightStep = 1.0f / numLayers;
        vec3 tangent_view_vec = normalize(transpose(fragment.tbn_matrix) * -vec_to_eye_normalized);

        // calculate shifted UV coordinates using parallax occlusion mapping
        vec2 tangent_step_uv = tangent_view_vec.xy * height_map_scale / numLayers;
        float currentLayerHeight = 1.0f;
        float currentHeightMapValue = texture(tx_height, currentUV).r;
        float previousHeightMapValue = currentHeightMapValue;

        // loop until tested layer is beneath (approximated) heightMap curve
        while (currentLayerHeight > currentHeightMapValue){
            currentUV += tangent_step_uv;
            previousHeightMapValue = currentHeightMapValue;
            currentHeightMapValue = texture(tx_height, currentUV).r;
            currentLayerHeight -= layerHeightStep;
        }

        float heightMapSampleWeight = (currentLayerHeight - currentHeightMapValue) / (previousHeightMapValue - currentHeightMapValue - layerHeightStep);
        currentUV = currentUV - (heightMapSampleWeight * tangent_step_uv);

        // discard UVs that are outside of the uv map
        if (currentUV.x > 1.0f || currentUV.x < 0.0f || currentUV.y > 1.0f || currentUV.y < 0.0f){
            discard;
        }
    }

    // apply shifted UV coordinates to textures
    vec4 texColor = texture(tx_color, currentUV);
    vec4 phongData = texture(tx_phong, currentUV);
    vec3 normal_vec;
    if ((textureFlags & TXFLAG_NORMAL_DATA) > 0) {
        normal_vec = normalize(fragment.tbn_matrix * (texture(tx_normal, currentUV).xyz * 255.0f / 128.0f - 1.0f));
    } else {
        normal_vec = fragment.tbn_matrix[2];
    }
    vec3 ambientColor = max(phongData.r * ambient_light, 0.0f);
    vec3 diffuseColor = vec3(0.0f, 0.0f, 0.0f);
    vec3 specularColor = vec3(0.0f, 0.0f, 0.0f);

    // TODO light bounces? occlusion?
    // TODO rewrite x/distance stuff to use (1+(x/distance)) instead of clamping (?).
    for (int lightIndex = 0; lightIndex < light_count; lightIndex++){
        LightData lightData = allLights[lightIndex];
        if (lightData.type == LIGHT_TYPE_DIRECTIONAL){
            // direction, color, intensity
            float diffuseDot = max(dot(-lightData.direction, normal_vec), 0.0f);
            float specularDot = max(dot(reflect(lightData.direction, normal_vec), vec_to_eye_normalized), 0.0f);
            diffuseColor += phongData.g * diffuseDot * lightData.color * lightData.intensity;
            specularColor += phongData.b * 8 * pow(specularDot, phongData.a * 255) * lightData.color * lightData.intensity;
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
                diffuseColor += phongData.g * diffuseDot * lightData.color * lightData.intensity * distanceFactor;
                specularColor += phongData.b * 8 * pow(specularDot, phongData.a * 255) * lightData.color * lightData.intensity * distanceFactor;
            }
        }
    }

    out_color = texColor * vec4(ambientColor + diffuseColor + specularColor, 1.0f);
    //out_color = vec4((normal_vec + 1.0f) * 0.5f, 1.0f);
}