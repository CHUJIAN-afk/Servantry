#version 330 core

in vec3 Position;
uniform mat4 FlameToView;
uniform mat4 FlameProjection;
out vec3 flamePosition;

void main() {
    flamePosition = Position;
    gl_Position = FlameProjection * FlameToView * vec4(Position, 1.0);
}
