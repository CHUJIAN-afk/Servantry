#version 330 core

#moj_import <summoner:rainbow_crystal.glsl>

uniform sampler2D SceneDepth;
uniform vec3 CameraInEffect;
uniform vec2 EffectSize;
uniform float EffectTime;
uniform float EffectPhase;
in vec3 glowPosition;
in vec3 glowCoord;
flat in float glowAlpha;
flat in float glowPhase;
out vec4 fragColor;

void main() {
    vec2 uv = gl_FragCoord.xy / EffectSize;
    if (glowAlpha <= 0.0 || gl_FragCoord.z > texture(SceneDepth, uv).r) {
        discard;
    }
    vec3 ray = glowPosition - CameraInEffect;
    float distance = length(ray);
    vec3 direction = ray / distance;
    float first = 0.0;
    float last = distance;
    for (int i = 0; i < 8; i++) {
        vec3 normal = vec3((i & 1) == 0 ? -1.0 : 1.0, (i & 2) == 0 ? -1.0 : 1.0, (i & 4) == 0 ? -1.0 : 1.0);
        normal.xz /= 0.95;
        float denominator = dot(normal, direction);
        float plane = 1.0 - dot(normal, CameraInEffect);
        if (abs(denominator) < 0.000001) {
            if (plane < 0.0) {
                last = -1.0;
            }
        } else if (denominator < 0.0) {
            first = max(first, plane / denominator);
        } else {
            last = min(last, plane / denominator);
        }
    }
    if (last > first && first < distance - 0.025) {
        discard;
    }
    float across = glowCoord.y * glowCoord.y;
    float ends = smoothstep(0.0, 0.12, glowCoord.x) * (1.0 - smoothstep(0.9, 1.0, glowCoord.x));
    float wave = 0.5 + 0.5 * sin(glowCoord.z * 26.0 - EffectTime * 18.0 + glowPhase);
    vec3 color = crystalColor(EffectPhase + glowPhase + glowCoord.z * 2.0 - EffectTime * 2.2);
    vec3 pearl = vec3(235.0, 235.0, 245.0) / 255.0;
    vec3 emission = color * exp(-across * 5.0) * 0.9
            + mix(color, pearl, 0.75) * exp(-across * 55.0) * (1.0 + wave * wave * 0.8);
    fragColor = vec4(emission * ends * glowAlpha, 0.0);
}
