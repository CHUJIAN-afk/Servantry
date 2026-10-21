#version 330 core

#moj_import <summoner:rainbow_crystal.glsl>

uniform sampler2D SceneDepth;
uniform vec2 EffectSize;
uniform float EffectAlpha;
uniform float EffectTime;
uniform float EffectPhase;
uniform float FogStart;
uniform float FogEnd;
in vec3 effectPosition;
in vec3 crystalCoord;
in vec3 crystalView;
flat in vec3 faceNormal;
out vec4 fragColor;

void main() {
    vec2 uv = gl_FragCoord.xy / EffectSize;
    if (gl_FragCoord.z > texture(SceneDepth, uv).r) discard;
    vec3 p = effectPosition;
    vec3 color = crystalColor(p.y * 3.2 + p.x * 1.2 + EffectTime * 2.2 + EffectPhase);
    float facet = 0.55 + 0.45 * max(dot(faceNormal, normalize(vec3(-0.4, 0.7, 1.0))), 0.0);
    float center = exp(-dot(p.xz, p.xz) * 7.0) * (1.0 - abs(p.y) * 0.6);
    float edge = 1.0 - smoothstep(0.0, 0.025, min(crystalCoord.x, min(crystalCoord.y, crystalCoord.z)));
    float flow = pow(0.5 + 0.5 * sin(p.y * 10.0 + p.x * 5.0 + p.z * 4.0 - EffectTime * 10.0 + EffectPhase), 5.0);
    vec3 emission = mix(color * facet, vec3(235.0, 235.0, 245.0) / 255.0, center * 0.55) * 1.05
            + color * edge * 0.2 + mix(color, vec3(235.0, 235.0, 245.0) / 255.0, 0.35) * flow * 0.3;
    float visibility = 1.0 - smoothstep(FogStart, max(FogEnd, FogStart + 0.001), length(crystalView));
    float opacity = EffectAlpha * visibility;
    fragColor = vec4(emission * opacity, 5.0 * opacity);
}
