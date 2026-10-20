#version 330 core

in vec3 Position;
uniform mat4 PortalToView;
uniform mat4 PortalProjection;
out vec3 portalPosition;

void main() {
    portalPosition = Position;
    gl_Position = PortalProjection * PortalToView * vec4(Position, 1.0);
}
