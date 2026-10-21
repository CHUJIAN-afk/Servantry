#version 330 core

in vec3 Position;
uniform mat4 EffectToView;
uniform mat4 EffectProjection;
out vec3 effectPosition;

void main() {
    effectPosition = Position * 1.2;
    gl_Position = EffectProjection * EffectToView * vec4(effectPosition, 1.0);
}
