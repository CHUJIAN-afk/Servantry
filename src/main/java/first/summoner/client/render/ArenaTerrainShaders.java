package first.summoner.client.render;

import com.lowdragmc.photon.client.compat.iris.IrisCompat;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;

import java.nio.FloatBuffer;
import java.util.regex.Pattern;

public final class ArenaTerrainShaders {

    private static final Pattern MAIN = Pattern.compile("\\bvoid\\s+main\\s*\\(\\s*(?:void)?\\s*\\)");
    private static final Pattern OUTPUT = Pattern.compile("(?m)^[ \\t]*(?:layout\\s*\\([^\\n]*\\)\\s*)?out\\s+vec4\\s+(\\w+)\\s*(\\[[^;]+\\])?\\s*;");
    private static final FloatBuffer MATRIX = BufferUtils.createFloatBuffer(16);

    private ArenaTerrainShaders() {
    }

    public static String vanilla(String name, boolean vertex, String source) {
        String path = name.substring(name.indexOf(':') + 1);
        return switch (path) {
            case "rendertype_solid", "rendertype_cutout", "rendertype_cutout_mipped", "rendertype_translucent", "rendertype_tripwire" -> patch(source, vertex);
            default -> source;
        };
    }

    public static String sodium(String name, String source) {
        boolean terrain = name.contains("blocks/block_layer_") || name.contains("iris:sodium-shader-") && !name.contains("shadow");
        if (terrain && !source.contains("EmitVertex") && !source.contains("gl_TessCoord")) {
            return patch(source, Pattern.compile("\\bgl_Position\\s*=").matcher(source).find());
        }
        return source;
    }

    private static String patch(String source, boolean vertex) {
        if (source.contains("summonerTerrainMain")) {
            return source;
        }
        if (vertex) {
            return MAIN.matcher(source).replaceFirst("void summonerTerrainMain()") + """

                    uniform mat4 SummonerClipToCamera;
                    out vec3 summonerBlockPosition;
                    void main() {
                        summonerTerrainMain();
                        vec4 position = SummonerClipToCamera * gl_Position;
                        summonerBlockPosition = position.xyz / position.w;
                    }
                    """;
        }
        var output = OUTPUT.matcher(source);
        if (output.find()) {
            String color = output.group(1) + (output.group(2) == null ? "" : "[0]");
            return MAIN.matcher(source).replaceFirst("void summonerTerrainMain()") + """

                    uniform vec4 SummonerArena;
                    uniform vec3 SummonerGridOffset;
                    in vec3 summonerBlockPosition;
                    void main() {
                        vec3 position = summonerBlockPosition + SummonerGridOffset;
                        vec3 normal = abs(cross(dFdx(position), dFdy(position)));
                        vec3 width = max(fwidth(position) * 1.25, vec3(0.003));
                        if (SummonerArena.w > 0.0 && distance(summonerBlockPosition, SummonerArena.xyz) < SummonerArena.w) {
                            vec3 cell = fract(position + 0.00001);
                            vec3 edge = min(cell, 1.0 - cell);
                            vec3 lines = 1.0 - smoothstep(width * 0.3, width, edge);
                            float line = normal.x > normal.y && normal.x > normal.z ? max(lines.y, lines.z)
                                    : normal.y > normal.z ? max(lines.x, lines.z) : max(lines.x, lines.y);
                            if (line < 0.25) discard;
                            %s = vec4(vec3(0.65, 0.32, 1.0) * (0.8 + 0.6 * line), 1.0);
                        } else {
                            summonerTerrainMain();
                        }
                    }
                    """.formatted(color);
        }
        return source;
    }

    public static final class Uniforms {

        private final int arena;
        private final int grid;
        private final int clip;

        public Uniforms(int program) {
            arena = GL20.glGetUniformLocation(program, "SummonerArena");
            grid = GL20.glGetUniformLocation(program, "SummonerGridOffset");
            clip = GL20.glGetUniformLocation(program, "SummonerClipToCamera");
        }

        public void upload() {
            if (arena >= 0) {
                var sphere = AstrageldonArenaEffect.TERRAIN_SPHERE;
                GL20.glUniform4f(arena, sphere.x, sphere.y, sphere.z, IrisCompat.isShadowPass() ? 0 : sphere.w);
                var offset = AstrageldonArenaEffect.GRID_OFFSET;
                GL20.glUniform3f(grid, offset.x, offset.y, offset.z);
                AstrageldonArenaEffect.CLIP_TO_CAMERA.get(MATRIX);
                GL20.glUniformMatrix4fv(clip, false, MATRIX);
            }
        }
    }
}
