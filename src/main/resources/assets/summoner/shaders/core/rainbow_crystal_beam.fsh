#version 330 core

#moj_import <summoner:rainbow_crystal.glsl>

uniform sampler2D SceneDepth;
uniform mat4 EffectToView;
uniform mat4 ViewToEffect;
uniform mat4 InverseProjection;
uniform vec3 CameraInEffect;
uniform vec3 SourcePosition;
uniform vec2 EffectSize;
uniform float EffectAge;
uniform float EffectAlpha;
uniform float EffectTime;
uniform float EffectPhase;
uniform float PulseTicks;
uniform float FogStart;
uniform float FogEnd;
in vec3 beamPosition;
out vec4 fragColor;

void main() {
    float beamLength = length(SourcePosition);
    vec3 axis = -SourcePosition / beamLength;
    vec3 reference = abs(axis.y) < 0.9 ? vec3(0.0, 1.0, 0.0) : vec3(1.0, 0.0, 0.0);
    vec3 side = normalize(cross(axis, reference));
    vec3 up = cross(axis, side);
    vec3 origin = CameraInEffect;
    vec3 direction = normalize(beamPosition - origin);
    float start = min(beamLength * 0.22, 1.2);
    float finish = beamLength - min(beamLength * 0.18, 0.9);
    float cycle = fract(EffectAge / PulseTicks);
    float width = 0.08 + 0.77 * smoothstep(0.0, 0.85, cycle);
    vec3 offset = origin - SourcePosition;
    vec3 rayOrigin = vec3(dot(offset, side), dot(offset, up), dot(offset, axis));
    vec3 rayDirection = vec3(dot(direction, side), dot(direction, up), dot(direction, axis));
    vec3 firstHit = (vec3(-width, -width, start) - rayOrigin) / rayDirection;
    vec3 lastHit = (vec3(width, width, finish) - rayOrigin) / rayDirection;
    vec3 lower = min(firstHit, lastHit);
    vec3 upper = max(firstHit, lastHit);
    float first = max(max(lower.x, lower.y), max(lower.z, 0.0));
    float last = min(min(upper.x, upper.y), upper.z);
    vec2 uv = gl_FragCoord.xy / EffectSize;
    vec4 sceneView = InverseProjection * vec4(uv * 2.0 - 1.0, texture(SceneDepth, uv).r * 2.0 - 1.0, 1.0);
    vec3 scene = (ViewToEffect * vec4(sceneView.xyz / sceneView.w, 1.0)).xyz;
    last = min(last, dot(scene - origin, direction));
    if (last <= first) discard;
    float spin = EffectTime * 4.0 + EffectPhase;
    mat2 rotation = mat2(cos(spin), -sin(spin), sin(spin), cos(spin));
    float stepSize = (last - first) / 20.0;
    float jitter = fract(dot(gl_FragCoord.xy, vec2(0.75487766, 0.56984029)));
    vec3 emission = vec3(0.0);
    float transmission = 1.0;
    for (int i = 0; i < 20; i++) {
        vec3 p = rayOrigin + rayDirection * (first + (float(i) + jitter) * stepSize);
        float along = clamp((p.z - start) / (finish - start), 0.0, 1.0);
        float profile = min(along / 0.58, (1.0 - along) / 0.42);
        float radius = max(width * profile, 0.002);
        vec2 crossSection = rotation * p.xy;
        float radial = (abs(crossSection.x) + abs(crossSection.y)) / radius;
        float body = 1.0 - smoothstep(0.0, 1.0, radial);
        float stream = 0.7 + 0.3 * sin(along * 35.0 - EffectTime * 22.0);
        float density = body * sqrt(body) * stream;
        vec3 color = rainbowColor(EffectPhase + along * 2.0 - EffectTime * 1.8);
        float opacity = 1.0 - exp(-density * stepSize * 1.5);
        emission += color * opacity * transmission * 1.2;
        transmission *= 1.0 - opacity;
    }
    float visibility = 1.0 - smoothstep(FogStart, max(FogEnd, FogStart + 0.001), length(EffectToView[3].xyz));
    float alpha = EffectAlpha * visibility * rainbowPulse(EffectAge, PulseTicks);
    fragColor = vec4(emission * alpha, 0.0);
}
