uint rainbowHash(uint seed) {
    seed = (seed ^ (seed >> 16u)) * 0x7feb352du;
    seed = (seed ^ (seed >> 15u)) * 0x846ca68bu;
    return seed ^ (seed >> 16u);
}

float rainbowRandom(uint seed) {
    return float(rainbowHash(seed) >> 8u) * (1.0 / 16777216.0);
}

vec3 rainbowColor(float hue) {
    return 0.55 + 0.45 * cos(hue + vec3(0.0, 2.0943951, 4.1887902));
}

float rainbowPulse(float age, float ticks) {
    float cycle = fract(age / ticks);
    return smoothstep(0.0, 0.18, cycle) * (1.0 - smoothstep(0.7, 1.0, cycle));
}

vec3 crystalColor(float phase) {
    // rainbow_crystal_staff.png 的原始 RGB。
    const vec3 colors[8] = vec3[8](
            vec3(42.0, 39.0, 118.0), vec3(86.0, 43.0, 175.0),
            vec3(183.0, 75.0, 218.0), vec3(228.0, 166.0, 234.0),
            vec3(235.0, 235.0, 245.0), vec3(149.0, 166.0, 166.0),
            vec3(92.0, 135.0, 169.0), vec3(130.0, 97.0, 198.0));
    float position = fract(phase / 6.2831853) * 8.0;
    int first = int(position);
    return mix(colors[first], colors[(first + 1) % 8], smoothstep(0.0, 1.0, fract(position))) / 255.0;
}

vec3 crystalCorner(int face, int corner) {
    bool upper = face < 4;
    if (corner == 0) {
        return vec3(0.0, upper ? 1.0 : -1.0, 0.0);
    }
    int index = face % 4 + (upper ? 2 - corner : corner - 1);
    float angle = float(index) * 1.5707963268;
    return vec3(cos(angle) * 0.95, 0.0, sin(angle) * 0.95);
}
