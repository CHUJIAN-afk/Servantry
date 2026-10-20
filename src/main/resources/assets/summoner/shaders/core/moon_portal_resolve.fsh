#version 330 core

uniform sampler2D SceneColor;
uniform sampler2D SceneDepth;
uniform sampler2D VolumeData;
uniform sampler2D LensData;
uniform sampler2D GlowColor;
uniform mat4 InverseProjection;
uniform vec2 EffectSize;
out vec4 fragColor;

float eyeDepth(vec2 uv) {
    float depth = texture(SceneDepth, uv).r;
    vec4 p = InverseProjection * vec4(uv * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
    return -p.z / p.w;
}

void main() {
    vec2 uv = gl_FragCoord.xy / EffectSize;
    vec4 field = texture(LensData, uv);
    vec2 push = field.xy / max(1.0, field.a);
    push *= min(1.0, 0.2 / max(length(push), 0.0001));
    vec2 margin = 0.5 / EffectSize;
    vec2 refractedUv = clamp(uv + push, margin, 1.0 - margin);
    if (field.a > 0.0 && eyeDepth(refractedUv) < field.z / field.a - 0.02) {
        refractedUv = uv;
    }
    float absorption = texture(VolumeData, uv).a;
    vec3 scene = texture(SceneColor, refractedUv).rgb;
    vec3 glow = texture(GlowColor, uv).rgb;
    fragColor = vec4(scene * exp(-absorption) + glow, 1.0);
}
