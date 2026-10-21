#version 330 core

uniform sampler2D FlameField;
uniform vec2 EffectSize;
out vec4 fragColor;

void main() {
    vec2 uv = gl_FragCoord.xy / EffectSize;
    vec4 field = texture(FlameField, uv);
    float density = field.a;
    vec3 color = field.rgb / max(density, 0.0001);
    float opacity = 1.0 - exp(-density * 1.5);
    float heat = smoothstep(0.7, 3.2, density);
    color = mix(color, vec3(1.0), heat * 0.4);
    fragColor = vec4(color * opacity * (1.3 + heat * 1.6), opacity);
}
