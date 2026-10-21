#version 330 core

in vec3 Position;
uniform mat4 EffectToView;
uniform mat4 EffectProjection;
uniform vec3 SourcePosition;
uniform float EffectAge;
uniform float PulseTicks;
out vec3 beamPosition;

void main() {
    float beamLength = length(SourcePosition);
    vec3 axis = -SourcePosition / beamLength;
    vec3 reference = abs(axis.y) < 0.9 ? vec3(0.0, 1.0, 0.0) : vec3(1.0, 0.0, 0.0);
    vec3 side = normalize(cross(axis, reference));
    vec3 up = cross(axis, side);
    float start = min(beamLength * 0.22, 1.2);
    float finish = beamLength - min(beamLength * 0.18, 0.9);
    float along = mix(start, finish, Position.z * 0.5 + 0.5) / beamLength;
    float cycle = fract(EffectAge / PulseTicks);
    float width = 0.08 + 0.77 * smoothstep(0.0, 0.85, cycle);
    beamPosition = SourcePosition * (1.0 - along) + (side * Position.x + up * Position.y) * width;
    gl_Position = EffectProjection * EffectToView * vec4(beamPosition, 1.0);
}
