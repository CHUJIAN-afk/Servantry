package first.summoner.client.render;

import com.lowdragmc.lowdraglib2.client.shader.HDRTarget;
import com.lowdragmc.photon.PhotonConfig;
import com.lowdragmc.photon.client.compat.iris.IrisCompat;
import com.lowdragmc.photon.client.gameobject.emitter.renderpipeline.RenderPassPipeline;
import com.lowdragmc.photon.client.postfx.runtime.SceneBlit;
import com.lowdragmc.photon.client.postprocessing.PhotonPostProcessing;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import first.summoner.common.projectile.MiniRainbowCrystal;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public final class RainbowCrystalEffect {

    private static final List<Effect> EFFECTS = new ArrayList<>();
    private static final Matrix4f PROJECTION = new Matrix4f();
    private static ShaderInstance crystalShader;
    private static ShaderInstance projectileShader;
    private static ShaderInstance beamShader;
    private static ShaderInstance sparkShader;
    private static ShaderInstance glowShader;
    private static ShaderInstance resolveShader;
    private static VertexBuffer cube;
    private static VertexBuffer crystal;
    private static VertexBuffer sparks;
    private static VertexBuffer glow;
    private static HDRTarget target;

    private RainbowCrystalEffect() {
    }

    public static void setCrystalShader(ShaderInstance value) {
        crystalShader = value;
    }

    public static void setProjectileShader(ShaderInstance value) {
        projectileShader = value;
    }

    public static void setBeamShader(ShaderInstance value) {
        beamShader = value;
    }

    public static void setSparkShader(ShaderInstance value) {
        sparkShader = value;
    }

    public static void setGlowShader(ShaderInstance value) {
        glowShader = value;
    }

    public static void setResolveShader(ShaderInstance value) {
        resolveShader = value;
    }

    public static void crystal(PoseStack poseStack, float alpha, float time, float phase) {
        Matrix4f view = new Matrix4f(RenderSystem.getModelViewMatrix()).mul(poseStack.last().pose())
                .rotateY(time * 1.2f + phase).scale(0.4f, 0.85f, 0.4f);
        submit(new Effect(view, null, 0, alpha, time, phase), -1.25f, -1.25f, -1.25f, 1.25f, 1.25f, 1.25f);
    }

    public static void projectile(PoseStack poseStack, Vec3 source, float age, float alpha, float time, float phase) {
        if (age < MiniRainbowCrystal.LIFETIME) {
            Matrix4f view = new Matrix4f(RenderSystem.getModelViewMatrix()).mul(poseStack.last().pose());
            Vector3f start = source.toVector3f();
            submit(new Effect(view, start, age, alpha, time, phase),
                    Math.min(start.x, 0) - 4f, Math.min(start.y, 0) - 4f, Math.min(start.z, 0) - 4f,
                    Math.max(start.x, 0) + 4f, Math.max(start.y, 0) + 4f, Math.max(start.z, 0) + 4f);
        }
    }

    private static void submit(Effect effect, float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        Matrix4f projection = RenderSystem.getProjectionMatrix();
        if (effect.alpha > 0 && !IrisCompat.isShadowPass()
                && new Matrix4f(projection).mul(effect.view).testAab(minX, minY, minZ, maxX, maxY, maxZ)) {
            if (EFFECTS.isEmpty()) {
                PROJECTION.set(projection);
            }
            EFFECTS.add(effect);
        }
    }

    public static void render() {
        if (!EFFECTS.isEmpty()) {
            var main = Minecraft.getInstance().getMainRenderTarget();
            ShaderInstance previous = RenderSystem.getShader();
            RenderSystem.disableScissor();
            RenderSystem.colorMask(true, true, true, true);
            target = RenderPassPipeline.resize(target, main.width, main.height, false);
            target.clear(Minecraft.ON_OSX);
            target.bindWrite(true);
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ONE);
            if (cube == null) {
                createBuffers();
            }
            Matrix4f inverseProjection = new Matrix4f(PROJECTION).invert();
            prepare(crystalShader, inverseProjection, main.getDepthTextureId(), main.width, main.height);
            prepare(projectileShader, inverseProjection, main.getDepthTextureId(), main.width, main.height);
            prepare(beamShader, inverseProjection, main.getDepthTextureId(), main.width, main.height);
            prepare(sparkShader, inverseProjection, main.getDepthTextureId(), main.width, main.height);
            sparkShader.safeGetUniform("SparkMinLife").set((float) MiniRainbowCrystal.SPARK_MIN_LIFE);
            sparkShader.safeGetUniform("SparkMaxLife").set((float) MiniRainbowCrystal.SPARK_MAX_LIFE);
            prepare(glowShader, inverseProjection, main.getDepthTextureId(), main.width, main.height);
            for (Effect effect : EFFECTS) {
                RenderSystem.enableCull();
                if (effect.source == null) {
                    draw(crystalShader, crystal, effect);
                    RenderSystem.disableCull();
                    draw(glowShader, glow, effect);
                } else {
                    draw(projectileShader, cube, effect);
                    if (effect.age < MiniRainbowCrystal.PULSE_TICKS * 2) {
                        beamShader.safeGetUniform("SourcePosition").set(effect.source);
                        draw(beamShader, cube, effect);
                    }
                    RenderSystem.disableCull();
                    draw(sparkShader, sparks, effect);
                }
            }
            VertexBuffer.unbind();
            boolean bloom = PhotonConfig.INSTANCE.enableBloom.get()
                    && (!IrisCompat.isUsingShaderPack() || PhotonConfig.INSTANCE.enableBloomWithIrisShader.get());
            if (bloom) {
                PhotonPostProcessing.prepareTarget(main.width, main.height);
            }
            var output = bloom ? PhotonPostProcessing.postTarget(target) : target;
            main.bindWrite(true);
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ZERO, GL11.GL_ONE);
            RenderSystem.colorMask(true, true, true, false);
            resolveShader.setSampler("VolumeData", target.getColorTextureId());
            resolveShader.setSampler("GlowColor", output.getColorTextureId());
            resolveShader.safeGetUniform("EffectSize").set((float) main.width, (float) main.height);
            resolveShader.apply();
            SceneBlit.drawFullscreenQuad();
            resolveShader.clear();
            RenderSystem.colorMask(true, true, true, true);
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(() -> previous);
            EFFECTS.clear();
        }
    }

    private static void prepare(ShaderInstance shader, Matrix4f inverseProjection, int depth, int width, int height) {
        shader.setSampler("SceneDepth", depth);
        shader.safeGetUniform("EffectProjection").set(PROJECTION);
        shader.safeGetUniform("InverseProjection").set(inverseProjection);
        shader.safeGetUniform("EffectSize").set((float) width, (float) height);
        shader.safeGetUniform("FogStart").set(RenderSystem.getShaderFogStart());
        shader.safeGetUniform("FogEnd").set(RenderSystem.getShaderFogEnd());
        shader.safeGetUniform("PulseTicks").set((float) MiniRainbowCrystal.PULSE_TICKS);
    }

    private static void draw(ShaderInstance shader, VertexBuffer buffer, Effect effect) {
        shader.safeGetUniform("EffectToView").set(effect.view);
        if (buffer == cube || buffer == crystal || buffer == glow) {
            Matrix4f inverse = new Matrix4f(effect.view).invert();
            shader.safeGetUniform("ViewToEffect").set(inverse);
            shader.safeGetUniform("CameraInEffect").set(inverse.transformPosition(new Vector3f()));
        }
        shader.safeGetUniform("EffectAlpha").set(effect.alpha * (effect.source == null ? 2f : 1f));
        shader.safeGetUniform("EffectAge").set(effect.age);
        shader.safeGetUniform("EffectTime").set(effect.time);
        shader.safeGetUniform("EffectPhase").set(effect.phase);
        buffer.bind();
        shader.apply();
        buffer.draw();
        shader.clear();
    }

    private static void createBuffers() {
        float[][] positions = {{-1, -1, -1}, {1, -1, -1}, {1, 1, -1}, {-1, 1, -1},
                {-1, -1, 1}, {1, -1, 1}, {1, 1, 1}, {-1, 1, 1}};
        int[][] faces = {{0, 1, 2, 3}, {4, 7, 6, 5}, {0, 3, 7, 4}, {1, 5, 6, 2}, {0, 4, 5, 1}, {3, 2, 6, 7}};
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        for (int[] face : faces) {
            for (int corner : face) {
                float[] p = positions[corner];
                builder.addVertex(p[0], p[1], p[2]);
            }
        }
        cube = upload(builder);
        builder = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION);
        for (int face = 0; face < 8; face++) {
            for (int corner = 0; corner < 3; corner++) {
                builder.addVertex(face, corner, 0);
            }
        }
        crystal = upload(builder);
        builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        for (int stream = 0; stream < 8; stream++) {
            for (int segment = 0; segment < 24; segment++) {
                float first = segment / 24f;
                float last = (segment + 1) / 24f;
                builder.addVertex(first, -1, stream);
                builder.addVertex(last, -1, stream);
                builder.addVertex(last, 1, stream);
                builder.addVertex(first, 1, stream);
            }
        }
        glow = upload(builder);
        sparks = createPhotons(128);
    }

    private static VertexBuffer createPhotons(int count) {
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        for (int spark = 0; spark < count; spark++) {
            builder.addVertex(-1, -1, spark);
            builder.addVertex(1, -1, spark);
            builder.addVertex(1, 1, spark);
            builder.addVertex(-1, 1, spark);
        }
        return upload(builder);
    }

    private static VertexBuffer upload(BufferBuilder builder) {
        VertexBuffer buffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        buffer.bind();
        buffer.upload(builder.buildOrThrow());
        return buffer;
    }

    public static void close() {
        EFFECTS.clear();
        if (cube != null) {
            cube.close();
            crystal.close();
            glow.close();
            sparks.close();
            cube = null;
            crystal = null;
            glow = null;
            sparks = null;
        }
        if (target != null) {
            target.destroyBuffers();
            target = null;
        }
    }

    private record Effect(Matrix4f view, Vector3f source, float age, float alpha, float time, float phase) {
    }
}
