#version 150

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV1;
in ivec2 UV2;
in vec3 Normal;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform float GameTime;
uniform vec4 ColorModulator;

out vec4 vertexColor;
out vec2 texCoord0;
out float progress;
out float timeShift;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    vertexColor = Color * ColorModulator;
    texCoord0 = UV0;

    // UV1.x 存储进度值 (0-255 映射到 0.0-1.0)
    // UV1.y 存储时间偏移 (用于动态颜色效果)
    progress = float(UV1.x) / 255.0;
    timeShift = float(UV1.y) / 255.0;
}
