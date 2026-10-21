#version 330 core

uniform sampler2D SceneDepth;
uniform mat4 FlameToView;
uniform mat4 ViewToFlame;
uniform mat4 InverseProjection;
uniform vec3 CameraInFlame;
uniform vec3 FlameColor;
uniform vec2 EffectSize;
uniform float FlameAge;
uniform float Opacity;
uniform float FlameTime;
uniform float FlamePhase;
uniform float FogStart;
uniform float FogEnd;
in vec3 flamePosition;
out vec4 fragColor;

uint hash(uint seed) {
    seed = (seed ^ (seed >> 16u)) * 0x7feb352du;
    seed = (seed ^ (seed >> 15u)) * 0x846ca68bu;
    return seed ^ (seed >> 16u);
}

float random(ivec3 cell) {
    uint seed = uint(cell.x) * 0x9e3779b9u ^ uint(cell.y) * 0x85ebca6bu ^ uint(cell.z) * 0xc2b2ae35u;
    return float(hash(seed) >> 8u) * (1.0 / 16777216.0);
}

float noise(vec3 p) {
    ivec3 cell = ivec3(floor(p));
    vec3 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    return mix(mix(mix(random(cell), random(cell + ivec3(1, 0, 0)), f.x),
                   mix(random(cell + ivec3(0, 1, 0)), random(cell + ivec3(1, 1, 0)), f.x), f.y),
               mix(mix(random(cell + ivec3(0, 0, 1)), random(cell + ivec3(1, 0, 1)), f.x),
                   mix(random(cell + ivec3(0, 1, 1)), random(cell + ivec3(1, 1, 1)), f.x), f.y), f.z);
}

float density(vec3 p) {
    vec3 flow = p * vec3(4.0, 4.0, 2.8) + vec3(FlamePhase * 2.0, -FlameTime * 0.8, -FlameTime * 4.5);
    float coarse = noise(flow);
    float fine = noise(flow * 2.1 + vec3(7.3, 19.1, 3.7));
    float turbulence = coarse * 0.7 + fine * 0.3;
    float body = 1.0 - dot(p, p);
    float tongues = smoothstep(0.03, 0.55, body - 0.18 + (turbulence - 0.5) * 0.9);
    float core = exp(-dot(p * vec3(1.0, 1.0, 0.8), p * vec3(1.0, 1.0, 0.8)) * 7.0);
    return (tongues * (0.35 + turbulence * 1.5) + core * 0.3)
            * (1.0 - smoothstep(0.75, 1.0, dot(p, p)));
}

void main() {
    vec2 uv = gl_FragCoord.xy / EffectSize;
    vec3 origin = CameraInFlame + vec3(0.0, 0.0, 0.35);
    vec3 direction = normalize(flamePosition - CameraInFlame);
    float b = dot(origin, direction);
    vec3 closest = origin - direction * b;
    float discriminant = 1.0 - dot(closest, closest);
    if (discriminant <= 0.0) {
        discard;
    }
    vec4 sceneView = InverseProjection * vec4(uv * 2.0 - 1.0, texture(SceneDepth, uv).r * 2.0 - 1.0, 1.0);
    vec3 sceneLocal = (ViewToFlame * vec4(sceneView.xyz / sceneView.w, 1.0)).xyz;
    float sceneDistance = dot(sceneLocal - CameraInFlame, direction);
    float root = sqrt(discriminant);
    float first = max(-b - root, 0.0);
    float last = min(-b + root, sceneDistance);
    if (last <= first) {
        discard;
    }
    float stepSize = (last - first) / 16.0;
    float jitter = fract(dot(gl_FragCoord.xy, vec2(0.75487766, 0.56984029)));
    float field = 0.0;
    for (int i = 0; i < 16; i++) {
        vec3 p = origin + direction * (first + (float(i) + jitter) * stepSize);
        field += density(p) * stepSize;
    }
    float visibility = 1.0 - smoothstep(FogStart, max(FogEnd, FogStart + 0.001), length(FlameToView[3].xyz));
    field *= Opacity * visibility * mix(2.3, 1.5, FlameAge);
    fragColor = vec4(FlameColor * field, field);
}
