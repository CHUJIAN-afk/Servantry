#version 330 core

#moj_import <photon:kila_lib.glsl>

uniform sampler2D SceneDepth;
uniform mat4 PortalToView;
uniform mat4 ViewToPortal;
uniform mat4 PortalProjection;
uniform mat4 InverseProjection;
uniform vec3 CameraInPortal;
uniform vec2 EffectSize;
uniform float Opacity;
uniform float PortalTime;
uniform float PortalPhase;
uniform float FogStart;
uniform float FogEnd;
in vec3 portalPosition;
layout(location = 0) out vec4 fragColor;
layout(location = 1) out vec4 lensData;

const float FIELD_RADIUS = 2.4;
const float VOLUME_RADIUS = 1.4;

vec3 viewPosition(vec2 uv, float depth) {
    vec4 p = InverseProjection * vec4(uv * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
    return p.xyz / p.w;
}

vec4 vortex(vec3 p) {
    float radius = length(p.xz);
    float height = abs(p.y);
    float angle = radius > 0.0001 ? atan(p.z, p.x) : 0.0;
    float spin = PortalTime * 1.6 + PortalPhase + radius * 4.5;
    float s = sin(spin);
    float c = cos(spin);
    vec2 flow = mat2(c, -s, s, c) * p.xz;
    float noise = kila_value_noise(abs(flow) * 7.0 + vec2(height * 4.0, PortalTime * 0.35));
    float arms = pow(0.5 + 0.5 * sin(angle * 4.0 + radius * 11.0 - PortalTime * 3.5 + height * 5.0 + PortalPhase), 5.0);
    float torus = exp(-pow((radius - 0.63) / 0.29, 2.0) - pow(height / (0.16 + radius * 0.2), 2.0));
    vec3 corePosition = p * vec3(1.0, 1.4, 1.0);
    float core = exp(-dot(corePosition, corePosition) * 32.0);
    float cloud = exp(-dot(p, p) * 2.8) * smoothstep(0.35, 0.8, noise);
    float density = core * 7.0 + torus * arms * (1.0 + noise * 3.0) + cloud * 0.6;
    density *= 1.0 - smoothstep(1.1, 1.38, length(p));
    vec3 cyan = mix(vec3(0.005, 0.15, 0.22), vec3(0.04, 1.4, 1.65), arms * noise);
    vec3 color = mix(cyan, vec3(2.8, 4.0, 4.2), clamp(core * 4.0, 0.0, 1.0));
    return vec4(color, density);
}

void main() {
    vec2 uv = gl_FragCoord.xy / EffectSize;
    vec3 origin = CameraInPortal;
    vec3 direction = normalize(portalPosition - origin);
    float b = dot(origin, direction);
    float rayDistance = b * b - dot(origin, origin);
    float discriminant = rayDistance + FIELD_RADIUS * FIELD_RADIUS;
    if (discriminant <= 0.0) {
        discard;
    }
    vec3 sceneView = viewPosition(uv, texture(SceneDepth, uv).r);
    vec3 sceneLocal = (ViewToPortal * vec4(sceneView, 1.0)).xyz;
    float sceneDistance = dot(sceneLocal - origin, direction);
    float root = sqrt(discriminant);
    float first = max(-b - root, 0.0);
    float last = min(-b + root, sceneDistance);
    if (last <= first) {
        discard;
    }

    vec3 emission = vec3(0.0);
    float coverage = 0.0;
    float volumeDiscriminant = rayDistance + VOLUME_RADIUS * VOLUME_RADIUS;
    if (volumeDiscriminant > 0.0) {
        float volumeRoot = sqrt(volumeDiscriminant);
        float volumeFirst = max(-b - volumeRoot, 0.0);
        float volumeLast = min(-b + volumeRoot, sceneDistance);
        if (volumeLast > volumeFirst) {
            float stepSize = (volumeLast - volumeFirst) / 32.0;
            float jitter = fract(dot(gl_FragCoord.xy, vec2(0.75487766, 0.56984029)));
            for (int i = 0; i < 32; i++) {
                vec3 p = origin + direction * (volumeFirst + (float(i) + jitter) * stepSize);
                vec4 sampleColor = vortex(p);
                float alpha = 1.0 - exp(-sampleColor.a * stepSize * 3.5);
                emission += (1.0 - coverage) * sampleColor.rgb * alpha;
                coverage += (1.0 - coverage) * alpha;
            }
        }
    }

    vec3 closest = origin + direction * clamp(-b, first, last);
    float impact = length(closest);
    float rim = exp(-pow((impact - 0.72) / 0.3, 2.0));
    float lens = exp(-pow((impact - 0.85) / 0.85, 2.0)) * (1.0 - smoothstep(1.65, FIELD_RADIUS, impact));
    vec3 centerView = PortalToView[3].xyz;
    vec3 closestView = (PortalToView * vec4(closest, 1.0)).xyz;
    vec2 radial = (mat3(PortalToView) * closest).xy;
    radial /= max(length(radial), 0.0001);
    vec2 tangent = (mat3(PortalToView) * cross(vec3(0.0, 1.0, 0.0), closest)).xy;
    tangent /= max(length(tangent), 0.0001);
    float scale = length(PortalToView[0].xyz);
    float size = abs(PortalProjection[1][1]) * scale / max(-closestView.z, scale * 0.35) * 0.5;
    vec2 aspect = vec2(EffectSize.x / EffectSize.y, 1.0);
    float visibility = 1.0 - smoothstep(FogStart, max(FogEnd, FogStart + 0.001), length(centerView));
    float opacity = Opacity * visibility;
    float weight = lens * opacity;
    vec2 push = (-radial + tangent * 0.65) / aspect * size * 0.42 * weight;
    float entryEye = max(-(PortalToView * vec4(origin + direction * first, 1.0)).z, 0.0);
    fragColor = vec4(emission * opacity, (rim * 1.7 + coverage * 0.4) * opacity);
    lensData = vec4(push, entryEye * weight, weight);
}
