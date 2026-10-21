#version 330 core

uniform sampler2D FlameColor;
uniform sampler2D GlowColor;
uniform vec2 EffectSize;
out vec4 fragColor;

void main() {
    vec2 uv = gl_FragCoord.xy / EffectSize;
    fragColor = vec4(texture(GlowColor, uv).rgb, texture(FlameColor, uv).a);
}
