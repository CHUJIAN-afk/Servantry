#version 330 core

uniform sampler2D ArenaColor;
uniform sampler2D GlowColor;
uniform mat4 ClipToCamera;
uniform mat4 CameraToClip;
uniform vec2 EffectSize;
uniform vec3 ArenaCenter;
uniform float ArenaRadius;
out vec4 fragColor;

void main() {
    vec2 uv = gl_FragCoord.xy / EffectSize;
    vec4 nearPoint = ClipToCamera * vec4(uv * 2.0 - 1.0, -1.0, 1.0);
    vec4 farPoint = ClipToCamera * vec4(uv * 2.0 - 1.0, 1.0, 1.0);
    vec3 rayOrigin = nearPoint.xyz / nearPoint.w;
    vec3 direction = normalize(farPoint.xyz / farPoint.w - rayOrigin);
    vec3 origin = rayOrigin - ArenaCenter;
    float b = dot(origin, direction);
    float h = b * b - dot(origin, origin) + ArenaRadius * ArenaRadius;
    if (h <= 0.0) discard;

    float root = sqrt(h);
    float distance = -b - root;
    if (distance <= 0.0) {
        distance = -b + root;
    }
    if (distance <= 0.0) discard;

    vec4 clip = CameraToClip * vec4(rayOrigin + direction * distance, 1.0);
    gl_FragDepth = clip.z / clip.w * 0.5 + 0.5;
    fragColor = vec4(textureLod(GlowColor, uv, 0.0).rgb, textureLod(ArenaColor, uv, 0.0).a);
}
