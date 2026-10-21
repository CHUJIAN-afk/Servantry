#version 330 core

#moj_import <summoner:rainbow_crystal.glsl>

uniform sampler2D SceneDepth;
uniform mat4 EffectToView;
uniform mat4 ViewToEffect;
uniform mat4 InverseProjection;
uniform vec3 CameraInEffect;
uniform vec2 EffectSize;
uniform float EffectAlpha;
uniform float EffectAge;
uniform float EffectPhase;
uniform float PulseTicks;
uniform float FogStart;
uniform float FogEnd;
in vec3 effectPosition;
out vec4 fragColor;

void main() {
    vec2 uv = gl_FragCoord.xy / EffectSize;
    vec3 origin = CameraInEffect;
    vec3 direction = normalize(effectPosition - origin);
    float b = dot(origin, direction);
    vec3 closest = origin - direction * b;
    float discriminant = 1.44 - dot(closest, closest);
    if (discriminant <= 0.0) discard;
    vec4 sceneView = InverseProjection * vec4(uv * 2.0 - 1.0, texture(SceneDepth, uv).r * 2.0 - 1.0, 1.0);
    vec3 scene = (ViewToEffect * vec4(sceneView.xyz / sceneView.w, 1.0)).xyz;
    float root = sqrt(discriminant);
    float first = max(-b - root, 0.0);
    float last = min(-b + root, dot(scene - origin, direction));
    if (last <= first) discard;
    float impact = length(origin + direction * clamp(-b, first, last));
    float charge = EffectAge < PulseTicks * 2.0 ? fract(EffectAge / PulseTicks) : 0.0;
    float radius = 0.035 + charge * charge * 0.21;
    float envelope = rainbowPulse(EffectAge, PulseTicks) * float(EffectAge < PulseTicks * 2.0);
    vec3 color = rainbowColor(EffectPhase + charge * 1.2);
    vec3 emission = mix(color, vec3(1.0), 0.3) * exp(-impact * impact / (radius * radius)) * envelope * 1.2;
    for (int i = 1; i <= 2; i++) {
        float age = EffectAge - float(i) * PulseTicks;
        if (age >= 0.0 && age < 4.0) {
            float fade = 1.0 - smoothstep(0.0, 4.0, age);
            float expansion = 0.07 + age * 0.17;
            float core = exp(-impact * impact / (expansion * expansion));
            float shell = exp(-pow((impact - expansion) / (0.04 + age * 0.025), 2.0));
            emission += (mix(color, vec3(1.0), 0.3) * core * 0.6 + color * shell * 0.1) * fade;
        }
    }
    float depthFade = smoothstep(0.0, 0.15, last - first);
    float visibility = 1.0 - smoothstep(FogStart, max(FogEnd, FogStart + 0.001), length(EffectToView[3].xyz));
    fragColor = vec4(emission * EffectAlpha * visibility * depthFade, 0.0);
}
