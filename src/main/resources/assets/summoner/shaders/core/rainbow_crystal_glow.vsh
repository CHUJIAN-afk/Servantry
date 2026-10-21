#version 330 core

#moj_import <summoner:rainbow_crystal.glsl>

in vec3 Position;
uniform mat4 EffectToView;
uniform mat4 EffectProjection;
uniform vec3 CameraInEffect;
uniform float EffectTime;
uniform float EffectPhase;
uniform float EffectAlpha;
uniform float FogStart;
uniform float FogEnd;
out vec3 glowPosition;
out vec3 glowCoord;
flat out float glowAlpha;
flat out float glowPhase;

vec3 curve(float t, float angle, float twist, float upwards) {
    float height = (t * 1.9 - 0.95) * upwards;
    float spin = angle + t * twist;
    vec2 radial = vec2(cos(spin), sin(spin));
    float radius = (1.0 - abs(height)) * 0.95 / (abs(radial.x) + abs(radial.y));
    radius += 0.09 + 0.11 * sin(t * 3.1415927);
    return vec3(radial.x * radius, height, radial.y * radius);
}

void main() {
    uint id = rainbowHash(uint(Position.z) ^ floatBitsToUint(EffectPhase));
    float clock = EffectTime / (1.5 + rainbowRandom(id + 1u) * 0.6) + rainbowRandom(id + 2u) * 7.0;
    float age = fract(clock);
    uint seed = rainbowHash(id ^ (uint(floor(clock)) * 0x9e3779b9u));
    float angle = rainbowRandom(seed + 1u) * 6.2831853;
    float twist = 3.0 + rainbowRandom(seed + 2u) * 2.0;
    float upwards = (id & 1u) == 0u ? 1.0 : -1.0;
    float head = age * 1.6;
    float tail = max(head - 0.6, 0.0);
    float t = mix(min(tail, 1.0), min(head, 1.0), Position.x);
    vec3 p = curve(t, angle, twist, upwards);
    vec3 tangent = curve(t + 0.001, angle, twist, upwards) - curve(t - 0.001, angle, twist, upwards);
    vec3 facing = cross(tangent, CameraInEffect - p);
    if (dot(facing, facing) < 0.000001) {
        facing = cross(tangent, vec3(cos(angle), 0.0, sin(angle)));
    }
    float width = (0.025 + rainbowRandom(seed + 3u) * 0.015) * (0.45 + 0.55 * sin(Position.x * 3.1415927));
    glowPosition = p + normalize(facing) * Position.y * width;
    vec4 view = EffectToView * vec4(glowPosition, 1.0);
    gl_Position = EffectProjection * view;
    glowCoord = vec3(Position.xy, t);
    glowPhase = rainbowRandom(seed + 4u) * 6.2831853;
    float visibility = 1.0 - smoothstep(FogStart, max(FogEnd, FogStart + 0.001), length(EffectToView[3].xyz));
    glowAlpha = EffectAlpha * visibility * smoothstep(0.0, 0.1, age) * (1.0 - smoothstep(0.8, 1.0, age));
}
