#version 330 core

#moj_import <summoner:rainbow_crystal.glsl>

in vec3 Position;
uniform mat4 EffectToView;
uniform mat4 EffectProjection;
uniform float EffectAge;
uniform float EffectAlpha;
uniform float EffectPhase;
uniform float PulseTicks;
uniform float SparkMinLife;
uniform float SparkMaxLife;
uniform float FogStart;
uniform float FogEnd;
out vec2 sparkCoord;
flat out vec3 sparkColor;
flat out float sparkAlpha;

void main() {
    uint id = uint(Position.z);
    float burst = floor(Position.z / 64.0) + 1.0;
    float age = EffectAge - burst * PulseTicks;
    uint seed = rainbowHash(id ^ floatBitsToUint(EffectPhase));
    float lifetime = SparkMinLife + floor(rainbowRandom(seed + 8u) * (SparkMaxLife - SparkMinLife + 1.0));
    float life = clamp(age / lifetime, 0.0, 1.0);
    float height = rainbowRandom(seed + 1u) * 2.0 - 1.0;
    float angle = rainbowRandom(seed + 2u) * 6.2831853;
    float horizontal = sqrt(1.0 - height * height);
    vec3 direction = vec3(horizontal * cos(angle), height, horizontal * sin(angle));
    float damping = 0.45 + rainbowRandom(seed + 9u) * 0.3;
    float speed = 0.9 + rainbowRandom(seed + 3u) * 0.7;
    float spread = (1.0 - exp(-max(age, 0.0) * damping)) * speed / damping;
    vec3 position = direction * spread;
    vec4 view = EffectToView * vec4(position, 1.0);
    float size = (0.045 + rainbowRandom(seed + 4u) * 0.07) * (1.0 - life * 0.3);
    float spin = rainbowRandom(seed + 5u) * 6.2831853 + life * 3.0;
    vec2 offset = mat2(cos(spin), -sin(spin), sin(spin), cos(spin)) * Position.xy;
    sparkCoord = Position.xy;
    view.xy += offset * size;
    gl_Position = EffectProjection * view;
    sparkColor = rainbowColor(EffectPhase + rainbowRandom(seed + 6u) * 6.2831853);
    float visibility = 1.0 - smoothstep(FogStart, max(FogEnd, FogStart + 0.001), length(view.xyz));
    float twinkle = 0.8 + 0.2 * sin(age * 2.5 + rainbowRandom(seed + 7u) * 6.2831853);
    sparkAlpha = age >= 0.0 && age < lifetime ? EffectAlpha * visibility * smoothstep(0.0, 0.65, age)
            * (1.0 - smoothstep(0.65, 1.0, life)) * twinkle : 0.0;
}
