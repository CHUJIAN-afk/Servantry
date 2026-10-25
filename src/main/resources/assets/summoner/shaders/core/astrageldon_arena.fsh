#version 330 core

uniform mat4 ClipToCamera;
uniform vec2 EffectSize;
uniform vec3 ArenaCenter;
uniform float ArenaRadius;
uniform float ArenaTime;
uniform vec4 EdgeWarning;
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

vec3 stars(vec3 origin, vec3 direction, float pixelAngle, float pixelOrigin, float sceneDepth) {
    // 按原星点占比调整胞元体积，密度提高 50%，星点大小不变。
    const float cellSize = 0.3689651;
    float distance = max(sphere(origin, direction, 1.12).y, 0.0);
    vec3 cell = floor((origin + direction * (distance + 0.0001)) / cellSize);
    vec3 stepDirection = mix(vec3(-1.0), vec3(1.0), step(vec3(0.0), direction));
    vec3 inverse = stepDirection / max(abs(direction), vec3(0.00001));
    vec3 next = ((cell + step(vec3(0.0), direction)) * cellSize - origin) * inverse;
    vec3 stride = cellSize * abs(inverse);
    vec3 color = vec3(0.0);
    // 星体位于随机三维坐标，逐胞元追踪避免球壳分层和移动采样闪烁。
    for (int i = 0; i < 64; i++) {
        float end = min(min(next.x, next.y), next.z);
        float seed = hash(cell);
        vec3 center = (cell + vec3(hash(cell + 9.1), hash(cell - 6.7), hash(cell + 21.3)) * 0.5 + 0.25) * cellSize;
        float along = dot(center - origin, direction);
        if (seed > 0.22 && along > distance && along < min(end, sceneDepth)) {
            vec3 delta = origin + direction * along - center;
            float radius = mix(0.00168, 0.00504, hash(cell + 4.8));
            float width = max(radius, pixelOrigin + pixelAngle * along * 0.7);
            float light = exp(-dot(delta, delta) / (width * width)) * radius * radius / (width * width);
            float flicker = 0.5 + 0.5 * sin(ArenaTime * (1.1 + seed * 3.7) + seed * 120.0);
            float twinkle = 0.35 + 1.65 * flicker * flicker;
            vec3 tint = mix(vec3(0.52, 0.38, 1.0), vec3(1.0, 0.86, 1.0), hash(cell - 12.5));
            color += tint * light * twinkle * (1.6 + seed) * (1.0 - smoothstep(4.5, 6.0, length(center)));
        }
        if (end >= sceneDepth) {
            break;
        }
        vec3 crossed = step(next, vec3(end + 0.00001));
        cell += crossed * stepDirection;
        next += crossed * stride;
        distance = end;
    }
    return color;
}

vec3 galaxy(vec3 origin, vec3 direction, float sceneDepth) {
    float first = max(sphere(origin, direction, 1.05).y, 0.0);
    origin -= vec3(0.4, 1.7, -2.8);
    mat2 tilt = mat2(0.8, -0.6, 0.6, 0.8);
    mat2 turn = mat2(0.6, -0.8, 0.8, 0.6);
    origin.xy = tilt * origin.xy;
    direction.xy = tilt * direction.xy;
    origin.yz = turn * origin.yz;
    direction.yz = turn * direction.yz;
    vec2 bounds = sphere(origin, direction, 3.6);
    first = max(first, bounds.x);
    float last = min(sceneDepth, bounds.y);
    if (abs(direction.y) > 0.0001) {
        vec2 slab = (vec2(-0.85, 0.85) - origin.y) / direction.y;
        first = max(first, min(slab.x, slab.y));
        last = min(last, max(slab.x, slab.y));
    }
    float stride = max(last - first, 0.0) / 28.0;
    vec3 color = vec3(0.0);
    float transmission = 1.0;
    for (int i = 0; i < 28; i++) {
        vec3 p = origin + direction * (first + (float(i) + 0.5) * stride);
        float radius = length(p.xz);
        float height = 0.1 + radius * 0.055;
        float disk = exp(-p.y * p.y / (height * height) - radius * 0.6) * (1.0 - smoothstep(2.7, 3.5, radius));
        float angle = radius > 0.0001 ? atan(p.z, p.x) : 0.0;
        float armDistance = sin((angle * 3.0 - log(max(radius, 0.06)) * 5.0 + ArenaTime * 0.015) * 0.5) / 0.2;
        float arm = exp(-armDistance * armDistance);
        float core = exp(-dot(p * vec3(2.5, 4.5, 2.5), p * vec3(2.5, 4.5, 2.5)));
        float density = disk * (0.08 + arm * 0.92) + core;
        if (density > 0.001) {
            float grain = cloud(p * 5.0 + 31.0);
            float alpha = 1.0 - exp(-density * (0.45 + grain) * stride * 4.0);
            vec3 tint = mix(vec3(0.13, 0.015, 0.36), vec3(0.75, 0.22, 1.1), grain * arm);
            color += transmission * alpha * (tint + vec3(0.8, 0.48, 1.0) * core);
            transmission *= 1.0 - alpha;
        }
    }
    return color;
}

void main() {
    vec2 uv = gl_FragCoord.xy / EffectSize;
    vec4 nearPoint = ClipToCamera * vec4(uv * 2.0 - 1.0, -1.0, 1.0);
    vec4 farPoint = ClipToCamera * vec4(uv * 2.0 - 1.0, 1.0, 1.0);
    vec3 rayOrigin = nearPoint.xyz / nearPoint.w;
    vec3 direction = normalize(farPoint.xyz / farPoint.w - rayOrigin);
    vec3 origin = rayOrigin - ArenaCenter;
    vec2 interval = sphere(origin, direction, ArenaRadius);
    vec3 sceneOrigin = origin / ArenaRadius;
    float pixelAngle = max(length(dFdx(direction)), length(dFdy(direction)));
    float pixelOrigin = max(length(dFdx(sceneOrigin)), length(dFdy(sceneOrigin)));
    // 在完整片元组上计算导数，再剔除球外射线。
    if (interval.y <= 0.0) {
        discard;
    }

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
    float sceneDepth = sphere(sceneOrigin, direction, 6.0).y;
    vec3 starColor = stars(sceneOrigin, direction, pixelAngle, pixelOrigin, sceneDepth);
    color += (starColor + galaxy(sceneOrigin, direction, sceneDepth)) * (0.35 + transmission * 0.65);
    vec3 exitPoint = origin + direction * interval.y;
    float rim = pow(max(1.0 - abs(dot(normalize(exitPoint), direction)), 0.0), 4.0);
    color += vec3(0.35, 0.055, 0.75) * rim;
    float patch = 1.0 - smoothstep(0.0, 7.0, length(normalize(exitPoint) - EdgeWarning.xyz) * ArenaRadius);
    color += vec3(0.7, 0.45, 1.0) * EdgeWarning.w * patch * patch * 1.2;
    fragColor = vec4(color, 1.0);
}
