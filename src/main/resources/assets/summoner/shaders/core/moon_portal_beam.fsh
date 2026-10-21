#version 330 core

uniform sampler2D SceneDepth;
uniform vec2 EffectSize;
uniform float PortalTime;
in vec3 beamCoord;
flat in float beamOpacity;
flat in float beamPhase;
out vec4 fragColor;

void main() {
    vec2 uv = gl_FragCoord.xy / EffectSize;
    if (gl_FragCoord.z > texture(SceneDepth, uv).r || beamOpacity <= 0.0) {
        discard;
    }
    float across = beamCoord.y * beamCoord.y;
    float ends = smoothstep(0.0, 0.12, beamCoord.x) * (1.0 - smoothstep(0.94, 1.0, beamCoord.x));
    float wave = 0.5 + 0.5 * sin(beamCoord.z * 32.0 - PortalTime * 18.0 + beamPhase);
    float pulse = 0.45 + 0.55 * wave * wave * wave;
    float head = exp(-pow((beamCoord.x - 0.88) * 9.0, 2.0));
    vec3 color = vec3(0.015, 0.8, 1.35) * exp(-across * 5.0)
            + vec3(0.75, 2.1, 2.5) * exp(-across * 65.0) * (pulse + head * 0.8);
    fragColor = vec4(color * ends * beamOpacity, 0.0);
}
