#version 150

in vec4 vertexColor;
in vec2 texCoord0;
in float progress;
in float timeShift;

uniform sampler2D Sampler0;
uniform float GameTime;

out vec4 fragColor;

void main() {
    // 基础颜色 - 自发光，不受光照影响
    vec4 color = vertexColor;

    // 淡出计算：使用二次曲线
    float fade = pow(1.0 - progress, 1.5);

    // 加法混合：颜色叠加效果，适合发光拖尾
    color.rgb *= fade;
    color.a *= fade * 0.6;

    // 如果透明度太低，丢弃片段
    if (color.a < 0.01) {
        discard;
    }

    fragColor = color;
}
