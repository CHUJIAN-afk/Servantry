#version 330 core

in vec3 Position;
uniform mat4 PortalToView;
uniform mat4 PortalProjection;
uniform vec3 CameraInPortal;
uniform float Opacity;
uniform float PortalTime;
uniform float PortalPhase;
uniform float FogStart;
uniform float FogEnd;
out vec3 beamCoord;
flat out float beamOpacity;
flat out float beamPhase;

uint hash(uint seed) {
    seed = (seed ^ (seed >> 16u)) * 0x7feb352du;
    seed = (seed ^ (seed >> 15u)) * 0x846ca68bu;
    return seed ^ (seed >> 16u);
}

float random(uint seed) {
    return float(hash(seed) >> 8u) * (1.0 / 16777216.0);
}

void main() {
    uint id = hash(uint(Position.z) ^ floatBitsToUint(PortalPhase));
    float clock = PortalTime / (2.2 + random(id + 5u) * 1.4) + random(id + 17u) * 9.0;
    float age = fract(clock);
    uint seed = hash(id ^ (uint(floor(clock)) * 0x9e3779b9u));
    float height = random(seed + 1u) * 2.0 - 1.0;
    float azimuth = random(seed + 2u) * 6.2831853;
    float horizontal = sqrt(1.0 - height * height);
    vec3 radial = vec3(horizontal * cos(azimuth), height, horizontal * sin(azimuth));
    vec3 reference = abs(radial.y) < 0.9 ? vec3(0.0, 1.0, 0.0) : vec3(1.0, 0.0, 0.0);
    vec3 side = normalize(cross(radial, reference));
    vec3 up = cross(radial, side);
    float roll = random(seed + 3u) * 6.2831853;
    vec3 tangent = side * cos(roll) + up * sin(roll);
    vec3 normal = cross(radial, tangent);
    float head = age * 1.7;
    float tail = max(head - 0.65, 0.0);
    float t = mix(min(tail, 1.0), min(head, 1.0), Position.x);
    float radius = 0.35 + pow(random(seed + 4u), 0.3333333) * 0.95;
    float twist = 1.3 + random(seed + 5u) * 1.8;
    float angle = t * twist;
    vec3 rotated = radial * cos(angle) + tangent * sin(angle);
    vec3 turned = -radial * sin(angle) + tangent * cos(angle);
    vec3 position = radius * (1.0 - t) * (rotated + normal * sin(t * 3.1415927) * 0.12);
    vec3 derivative = -rotated + (1.0 - t) * twist * turned
            + normal * 0.12 * ((1.0 - t) * 3.1415927 * cos(t * 3.1415927) - sin(t * 3.1415927));
    vec3 facing = cross(derivative, CameraInPortal - position);
    if (dot(facing, facing) < 0.000001) {
        facing = cross(derivative, normal);
    }
    float width = (0.045 + random(seed + 6u) * 0.025) * (0.45 + 0.55 * sin(Position.x * 3.1415927));
    position += normalize(facing) * Position.y * width;
    gl_Position = PortalProjection * PortalToView * vec4(position, 1.0);
    beamCoord = vec3(Position.xy, t);
    float visibility = 1.0 - smoothstep(FogStart, max(FogEnd, FogStart + 0.001), length(PortalToView[3].xyz));
    beamOpacity = Opacity * visibility * smoothstep(0.0, 0.08, age) * (1.0 - smoothstep(0.78, 0.97, age));
    beamPhase = random(seed + 7u) * 6.2831853;
}
