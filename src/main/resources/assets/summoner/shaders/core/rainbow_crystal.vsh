#version 330 core

#moj_import <summoner:rainbow_crystal.glsl>

in vec3 Position;
uniform mat4 EffectToView;
uniform mat4 ViewToEffect;
uniform mat4 EffectProjection;
out vec3 effectPosition;
out vec3 crystalCoord;
out vec3 crystalView;
flat out vec3 faceNormal;

void main() {
    int face = int(Position.x);
    int corner = int(Position.y);
    effectPosition = crystalCorner(face, corner);
    crystalCoord = vec3(corner == 0 ? 1.0 : 0.0, corner == 1 ? 1.0 : 0.0, corner == 2 ? 1.0 : 0.0);
    vec3 first = crystalCorner(face, 0);
    vec3 normal = cross(crystalCorner(face, 1) - first, crystalCorner(face, 2) - first);
    faceNormal = normalize(transpose(mat3(ViewToEffect)) * normal);
    vec4 view = EffectToView * vec4(effectPosition, 1.0);
    crystalView = view.xyz;
    gl_Position = EffectProjection * view;
}
