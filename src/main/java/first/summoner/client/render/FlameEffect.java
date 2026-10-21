package first.summoner.client.render;

import com.lowdragmc.lowdraglib2.client.shader.HDRTarget;
import com.lowdragmc.photon.PhotonConfig;
import com.lowdragmc.photon.client.compat.iris.IrisCompat;
import com.lowdragmc.photon.client.gameobject.emitter.renderpipeline.RenderPassPipeline;
import com.lowdragmc.photon.client.postfx.runtime.SceneBlit;
import com.lowdragmc.photon.client.postprocessing.PhotonPostProcessing;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import software.bernie.geckolib.util.Color;

import java.util.ArrayList;
import java.util.List;

public final class FlameEffect {

    private static final List<Flame> FLAMES = new ArrayList<>();
    private static final Matrix4f PROJECTION = new Matrix4f();
    private static ShaderInstance shader;
    private static ShaderInstance colorShader;
    private static ShaderInstance compositeShader;
    private static VertexBuffer cube;
    private static HDRTarget field;
    private static HDRTarget effect;

    private FlameEffect() {
    }

    public static void setShader(ShaderInstance value) {
        shader = value;
    }

    public static void setColorShader(ShaderInstance value) {
        colorShader = value;
    }

    public static void setCompositeShader(ShaderInstance value) {
        compositeShader = value;
    }

    public static void submit(PoseStack poseStack, Profile profile, float age, float alpha, float time, float phase) {
        float progress = profile.progress(age);
        float opacity = alpha * profile.opacity(age);
        if (age >= 0 && age < profile.maxLife && opacity > 0 && !IrisCompat.isShadowPass()) {
            float size = profile.size(age);
            Matrix4f view = new Matrix4f(RenderSystem.getModelViewMatrix()).mul(poseStack.last().pose()).scale(size, size, size * 1.8f);
            Matrix4f projection = RenderSystem.getProjectionMatrix();
            if (new Matrix4f(projection).mul(view).testAab(-1, -1, -1.35f, 1, 1, 0.65f)) {
                if (FLAMES.isEmpty()) {
                    PROJECTION.set(projection);
                }
                FLAMES.add(new Flame(view, profile.color(age), progress, opacity, time, phase));
            }
        }
    }

    public static void render() {
        if (!FLAMES.isEmpty()) {
            var main = Minecraft.getInstance().getMainRenderTarget();
            ShaderInstance previous = RenderSystem.getShader();
            RenderSystem.disableScissor();
            RenderSystem.colorMask(true, true, true, true);
            field = RenderPassPipeline.resize(field, main.width, main.height, false);
            effect = RenderPassPipeline.resize(effect, main.width, main.height, false);
            field.clear(Minecraft.ON_OSX);
            field.bindWrite(true);
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.enableCull();
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ONE);
            if (cube == null) {
                createCube();
            }
            shader.setSampler("SceneDepth", main.getDepthTextureId());
            shader.safeGetUniform("FlameProjection").set(PROJECTION);
            shader.safeGetUniform("InverseProjection").set(new Matrix4f(PROJECTION).invert());
            shader.safeGetUniform("EffectSize").set((float) main.width, (float) main.height);
            shader.safeGetUniform("FogStart").set(RenderSystem.getShaderFogStart());
            shader.safeGetUniform("FogEnd").set(RenderSystem.getShaderFogEnd());
            cube.bind();
            for (Flame flame : FLAMES) {
                Matrix4f inverse = new Matrix4f(flame.view).invert();
                shader.safeGetUniform("FlameToView").set(flame.view);
                shader.safeGetUniform("ViewToFlame").set(inverse);
                shader.safeGetUniform("CameraInFlame").set(inverse.transformPosition(new Vector3f()));
                shader.safeGetUniform("FlameColor").set(flame.color.getRedFloat(), flame.color.getGreenFloat(), flame.color.getBlueFloat());
                shader.safeGetUniform("FlameAge").set(flame.progress);
                shader.safeGetUniform("Opacity").set(flame.opacity);
                shader.safeGetUniform("FlameTime").set(flame.time);
                shader.safeGetUniform("FlamePhase").set(flame.phase);
                shader.apply();
                cube.draw();
                shader.clear();
            }
            VertexBuffer.unbind();
            RenderSystem.disableBlend();
            effect.bindWrite(true);
            colorShader.setSampler("FlameField", field.getColorTextureId());
            colorShader.safeGetUniform("EffectSize").set((float) main.width, (float) main.height);
            colorShader.apply();
            SceneBlit.drawFullscreenQuad();
            colorShader.clear();
            boolean bloom = PhotonConfig.INSTANCE.enableBloom.get()
                    && (!IrisCompat.isUsingShaderPack() || PhotonConfig.INSTANCE.enableBloomWithIrisShader.get());
            if (bloom) {
                PhotonPostProcessing.prepareTarget(main.width, main.height);
            }
            var output = bloom ? PhotonPostProcessing.postTarget(effect) : effect;
            main.bindWrite(true);
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ZERO, GL11.GL_ONE);
            RenderSystem.colorMask(true, true, true, false);
            compositeShader.setSampler("FlameColor", effect.getColorTextureId());
            compositeShader.setSampler("GlowColor", output.getColorTextureId());
            compositeShader.safeGetUniform("EffectSize").set((float) main.width, (float) main.height);
            compositeShader.apply();
            SceneBlit.drawFullscreenQuad();
            compositeShader.clear();
            RenderSystem.colorMask(true, true, true, true);
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(() -> previous);
            FLAMES.clear();
        }
    }

    private static void createCube() {
        float[][] positions = {{-1, -1, -1.35f}, {1, -1, -1.35f}, {1, 1, -1.35f}, {-1, 1, -1.35f},
                {-1, -1, 0.65f}, {1, -1, 0.65f}, {1, 1, 0.65f}, {-1, 1, 0.65f}};
        int[][] faces = {{0, 1, 2, 3}, {4, 7, 6, 5}, {0, 3, 7, 4}, {1, 5, 6, 2}, {0, 4, 5, 1}, {3, 2, 6, 7}};
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        for (int[] face : faces) {
            for (int corner : face) {
                float[] p = positions[corner];
                builder.addVertex(p[0], p[1], p[2]);
            }
        }
        cube = new VertexBuffer(VertexBuffer.Usage.STATIC);
        cube.bind();
        cube.upload(builder.buildOrThrow());
    }

    public static void close() {
        FLAMES.clear();
        if (cube != null) {
            cube.close();
            cube = null;
        }
        if (field != null) {
            field.destroyBuffers();
            effect.destroyBuffers();
            field = null;
            effect = null;
        }
    }

    /** 大小为方块半径，生命与 age 使用 tick，颜色为 RGB；变色系数越大越早到达末端色。 */
    public record Profile(float initialSize, int maxLife, int initialColor, int endColor, float colorSpeed) {

        public float progress(float age) {
            return Mth.clamp(age / maxLife, 0f, 1f);
        }

        public float size(float age) {
            float progress = progress(age);
            return initialSize * (1f + 3.2f * smooth(0f, 0.65f, progress)) * (1f - smooth(0.74f, 1f, progress));
        }

        public float opacity(float age) {
            float progress = progress(age);
            return smooth(0f, 0.12f, progress) * (1f - smooth(0.76f, 1f, progress));
        }

        public Color color(float age) {
            float blend = Mth.clamp(progress(age) * colorSpeed, 0f, 1f);
            Color first = Color.ofOpaque(initialColor);
            Color last = Color.ofOpaque(endColor);
            return Color.ofARGB(1f, Mth.lerp(blend, first.getRedFloat(), last.getRedFloat()),
                    Mth.lerp(blend, first.getGreenFloat(), last.getGreenFloat()), Mth.lerp(blend, first.getBlueFloat(), last.getBlueFloat()));
        }

        private static float smooth(float first, float last, float value) {
            float t = Mth.clamp((value - first) / (last - first), 0f, 1f);
            return t * t * (3f - 2f * t);
        }
    }

    private record Flame(Matrix4f view, Color color, float progress, float opacity, float time, float phase) {
    }
}
