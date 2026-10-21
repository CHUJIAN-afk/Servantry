#version 330 core

uniform sampler2D SceneDepth;
uniform vec2 EffectSize;
in vec2 sparkCoord;
flat in vec3 sparkColor;
flat in float sparkAlpha;
out vec4 fragColor;

void main() {
    vec2 uv = gl_FragCoord.xy / EffectSize;
    if (sparkAlpha <= 0.0 || gl_FragCoord.z > texture(SceneDepth, uv).r) discard;
    vec2 p = abs(sparkCoord);
    float core = exp(-dot(p, p) * 22.0);
    float star = exp(-min(p.x, p.y) * 55.0 - max(p.x, p.y) * 3.5);
    vec3 emission = sparkColor * star * 1.25 + mix(sparkColor, vec3(1.0), 0.45) * core * 1.8;
    fragColor = vec4(emission * sparkAlpha, 0.0);
}
