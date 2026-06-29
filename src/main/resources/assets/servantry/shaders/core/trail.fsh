#version 150

in vec4 vertexColor;
in vec2 texCoord0;
in float progress;
in float timeShift;

uniform sampler2D Sampler0;
uniform float GameTime;

out vec4 fragColor;

void main() {
    vec4 color = vertexColor;
    color.a *= pow(1.0 - progress, 1.5) * 0.8;
    fragColor = color;
}
