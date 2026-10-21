#version 330 core

uniform sampler2D VolumeData;
uniform sampler2D GlowColor;
uniform vec2 EffectSize;
out vec4 fragColor;

void main() {
    vec2 uv = gl_FragCoord.xy / EffectSize;
    vec3 emission = texture(GlowColor, uv).rgb;
    float peak = max(emission.r, max(emission.g, emission.b));
    emission /= 1.0 + max(peak - 1.6, 0.0) / 1.6;
    fragColor = vec4(emission, 1.0 - exp(-texture(VolumeData, uv).a));
}
