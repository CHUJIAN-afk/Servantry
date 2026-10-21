#version 330 core

uniform sampler2D VolumeData;
uniform sampler2D GlowColor;
uniform vec2 EffectSize;
out vec4 fragColor;

void main() {
    vec2 uv = gl_FragCoord.xy / EffectSize;
    float absorption = texture(VolumeData, uv).a;
    vec3 glow = texture(GlowColor, uv).rgb;
    fragColor = vec4(glow, 1.0 - exp(-absorption));
}
