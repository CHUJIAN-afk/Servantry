#version 330 core

uniform sampler2D SceneDepth;
uniform mat4 PortalToView;
uniform mat4 ViewToPortal;
uniform mat4 InverseProjection;
uniform vec3 CameraInPortal;
uniform vec2 EffectSize;
uniform float Opacity;
uniform float PortalTime;
uniform float PortalPhase;
uniform float FogStart;
uniform float FogEnd;
in vec3 portalPosition;
out vec4 fragColor;

const float CORE_RADIUS = 0.8;

float erfApprox(float x) {
    float a = abs(x);
    float t = 1.0 / (1.0 + a * (0.278393 + a * (0.230389 + a * (0.000972 + a * 0.078108))));
    float t2 = t * t;
    return sign(x) * (1.0 - t2 * t2);
}

float gaussian(float impactSquared, float first, float last, float sharpness) {
    float scale = sqrt(sharpness);
    return exp(-impactSquared * sharpness) * 0.8862269 / scale
            * (erfApprox(last * scale) - erfApprox(first * scale));
}

void main() {
    vec2 uv = gl_FragCoord.xy / EffectSize;
    vec3 origin = CameraInPortal;
    vec3 direction = normalize(portalPosition - origin);
    float b = dot(origin, direction);
    vec3 closest = origin - direction * b;
    float impactSquared = dot(closest, closest);
    float discriminant = CORE_RADIUS * CORE_RADIUS - impactSquared;
    if (discriminant <= 0.0) {
        discard;
    }
    vec4 sceneView = InverseProjection * vec4(uv * 2.0 - 1.0, texture(SceneDepth, uv).r * 2.0 - 1.0, 1.0);
    vec3 sceneLocal = (ViewToPortal * vec4(sceneView.xyz / sceneView.w, 1.0)).xyz;
    float sceneDistance = dot(sceneLocal - origin, direction);
    float root = sqrt(discriminant);
    float first = max(-b - root, 0.0);
    float last = min(-b + root, sceneDistance);
    if (last <= first) {
        discard;
    }
    float halo = gaussian(impactSquared, first + b, last + b, 7.0);
    float core = gaussian(impactSquared, first + b, last + b, 80.0);
    float pulse = 0.9 + 0.1 * sin(PortalTime * 4.0 + PortalPhase);
    float visibility = 1.0 - smoothstep(FogStart, max(FogEnd, FogStart + 0.001), length(PortalToView[3].xyz));
    float opacity = Opacity * visibility;
    vec3 emission = vec3(0.02, 0.65, 0.9) * halo * 0.5 + vec3(1.6, 3.6, 4.0) * core * 6.0 * pulse;
    fragColor = vec4(emission * opacity, halo * 2.7 * opacity);
}
