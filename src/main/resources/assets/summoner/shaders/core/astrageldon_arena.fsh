#version 330 core

uniform mat4 ClipToCamera;
uniform vec2 EffectSize;
uniform vec3 ArenaCenter;
uniform float ArenaRadius;
uniform float ArenaTime;
out vec4 fragColor;

float hash(vec3 p) {
    p = fract(p * vec3(0.1031, 0.1030, 0.0973));
    p += dot(p, p.yzx + 33.33);
    return fract((p.x + p.y) * p.z);
}

float noise(vec3 p) {
    vec3 cell = floor(p);
    vec3 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    return mix(mix(mix(hash(cell), hash(cell + vec3(1, 0, 0)), f.x),
                   mix(hash(cell + vec3(0, 1, 0)), hash(cell + vec3(1, 1, 0)), f.x), f.y),
               mix(mix(hash(cell + vec3(0, 0, 1)), hash(cell + vec3(1, 0, 1)), f.x),
                   mix(hash(cell + vec3(0, 1, 1)), hash(cell + vec3(1, 1, 1)), f.x), f.y), f.z);
}

float cloud(vec3 p) {
    return noise(p) * 0.57 + noise(p * 2.03 + 17.2) * 0.28 + noise(p * 4.11 - 9.4) * 0.15;
}

vec2 sphere(vec3 origin, vec3 direction, float radius) {
    float b = dot(origin, direction);
    float h = b * b - dot(origin, origin) + radius * radius;
    float root = sqrt(max(h, 0.0));
    return h > 0.0 ? vec2(-b - root, -b + root) : vec2(-1.0);
}

vec3 stars(vec3 origin, vec3 direction, float radius, float layer) {
    vec2 interval = sphere(origin, direction, radius);
    vec3 p = normalize(origin + direction * interval.y);
    float scale = 65.0 + layer * 31.0;
    float width = max(length(fwidth(p)) * scale * 0.9, 0.025);
    vec3 a = abs(p);
    vec2 face;
    float side;
    if (a.x > a.y && a.x > a.z) {
        face = p.yz / a.x;
        side = p.x > 0.0 ? 1.0 : 2.0;
    } else if (a.y > a.z) {
        face = p.xz / a.y;
        side = p.y > 0.0 ? 3.0 : 4.0;
    } else {
        face = p.xy / a.z;
        side = p.z > 0.0 ? 5.0 : 6.0;
    }
    vec2 grid = face * scale;
    vec3 cell = vec3(floor(grid), side + layer * 7.0);
    float seed = hash(cell);
    vec2 center = vec2(hash(cell + 9.1), hash(cell - 6.7)) * 0.7 + 0.15;
    vec2 delta = fract(grid) - center;
    float light = exp(-dot(delta, delta) / (width * width)) * step(0.965, seed);
    float twinkle = 0.8 + 0.2 * sin(ArenaTime * 1.6 + seed * 120.0);
    return mix(vec3(0.48, 0.38, 0.95), vec3(1.0, 0.82, 0.63), seed) * light * twinkle * (1.5 + seed);
}

void main() {
    vec2 uv = gl_FragCoord.xy / EffectSize;
    vec4 nearPoint = ClipToCamera * vec4(uv * 2.0 - 1.0, -1.0, 1.0);
    vec4 farPoint = ClipToCamera * vec4(uv * 2.0 - 1.0, 1.0, 1.0);
    vec3 rayOrigin = nearPoint.xyz / nearPoint.w;
    vec3 direction = normalize(farPoint.xyz / farPoint.w - rayOrigin);
    vec3 origin = rayOrigin - ArenaCenter;
    vec2 interval = sphere(origin, direction, ArenaRadius);

    float first = max(interval.x, 0.0);
    float stride = max(interval.y - first, 0.0) / 20.0;
    vec3 color = vec3(0.006, 0.003, 0.025);
    float transmission = 1.0;
    for (int i = 0; i < 20; i++) {
        vec3 position = origin + direction * (first + (float(i) + 0.5) * stride);
        vec3 p = position * 0.065;
        p.x += sin(p.y * 0.8 + ArenaTime * 0.018) * 0.65;
        p.z += cos(p.x * 0.7 - ArenaTime * 0.014) * 0.55;
        float density = smoothstep(0.34, 0.78, cloud(p));
        float filament = pow(max(1.0 - abs(noise(p * 1.7 + 42.0) * 2.0 - 1.0), 0.0), 6.0);
        float alpha = 1.0 - exp(-density * stride * 0.018);
        vec3 tint = mix(vec3(0.045, 0.012, 0.16), vec3(0.48, 0.12, 0.72), filament);
        color += transmission * alpha * tint * (0.9 + density * 1.4);
        transmission *= 1.0 - alpha;
    }
    // 有限距离的三层星空与体积云共用世界坐标，移动相机时产生视差。
    vec3 starColor = stars(origin, direction, ArenaRadius * 1.1, 0.0)
            + stars(origin, direction, ArenaRadius * 1.7, 1.0) * 0.65
            + stars(origin, direction, ArenaRadius * 2.6, 2.0) * 0.4;
    color += starColor * (0.35 + transmission * 0.65);
    vec3 exitPoint = origin + direction * interval.y;
    float rim = pow(max(1.0 - abs(dot(normalize(exitPoint), direction)), 0.0), 4.0);
    color += vec3(0.35, 0.055, 0.75) * rim;
    // 星空导数在完整片元组上计算，之后才剔除球外射线。
    if (interval.y <= 0.0) discard;
    fragColor = vec4(color, 1.0);
}
