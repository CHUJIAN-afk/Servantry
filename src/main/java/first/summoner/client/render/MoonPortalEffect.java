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
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public final class MoonPortalEffect {

    private static final float RADIUS = 1.4f;
    private static final float CORE_RADIUS = 0.8f;
    private static final List<Portal> PORTALS = new ArrayList<>();
    private static final Matrix4f PROJECTION = new Matrix4f();
    private static ShaderInstance shader;
    private static ShaderInstance beamShader;
    private static ShaderInstance resolveShader;
    private static VertexBuffer cube;
    private static VertexBuffer beams;
    private static HDRTarget effect;

    private MoonPortalEffect() {
    }

    public static void setShader(ShaderInstance value) {
        shader = value;
    }

    public static void setBeamShader(ShaderInstance value) {
        beamShader = value;
    }

    public static void setResolveShader(ShaderInstance value) {
        resolveShader = value;
    }

    public static void submit(PoseStack poseStack, float alpha, float time, float phase) {
        if (alpha > 0 && !IrisCompat.isShadowPass()) {
            Matrix4f view = new Matrix4f(RenderSystem.getModelViewMatrix()).mul(poseStack.last().pose());
            Matrix4f projection = RenderSystem.getProjectionMatrix();
            if (new Matrix4f(projection).mul(view).testAab(-RADIUS, -RADIUS, -RADIUS, RADIUS, RADIUS, RADIUS)) {
                if (PORTALS.isEmpty()) {
                    PROJECTION.set(projection);
                }
                PORTALS.add(new Portal(view, alpha, time, phase));
            }
        }
    }

    public static void render() {
        if (!PORTALS.isEmpty()) {
            var main = Minecraft.getInstance().getMainRenderTarget();
            ShaderInstance previous = RenderSystem.getShader();
            RenderSystem.disableScissor();
            effect = RenderPassPipeline.resize(effect, main.width, main.height, false);
            RenderSystem.colorMask(true, true, true, true);
            effect.clear(Minecraft.ON_OSX);
            effect.bindWrite(true);
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.enableCull();
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ONE);
            if (cube == null) {
                createCube();
                createBeams();
            }
            prepareShader(shader, main.getDepthTextureId(), main.width, main.height);
            prepareShader(beamShader, main.getDepthTextureId(), main.width, main.height);
            Matrix4f inverseProjection = new Matrix4f(PROJECTION).invert();
            shader.safeGetUniform("InverseProjection").set(inverseProjection);
            for (Portal portal : PORTALS) {
                Matrix4f inverse = new Matrix4f(portal.view).invert();
                shader.safeGetUniform("ViewToPortal").set(inverse);
                Vector3f camera = inverse.transformPosition(new Vector3f());
                shader.safeGetUniform("CameraInPortal").set(camera);
                drawPortal(shader, cube, portal);
                RenderSystem.disableCull();
                beamShader.safeGetUniform("CameraInPortal").set(camera);
                drawPortal(beamShader, beams, portal);
                RenderSystem.enableCull();
            }
            VertexBuffer.unbind();
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
            resolveShader.setSampler("VolumeData", effect.getColorTextureId());
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
            PORTALS.clear();
        }
    }

    private static void prepareShader(ShaderInstance value, int depth, int width, int height) {
        value.setSampler("SceneDepth", depth);
        value.safeGetUniform("PortalProjection").set(PROJECTION);
        value.safeGetUniform("EffectSize").set((float) width, (float) height);
        value.safeGetUniform("FogStart").set(RenderSystem.getShaderFogStart());
        value.safeGetUniform("FogEnd").set(RenderSystem.getShaderFogEnd());
    }

    private static void drawPortal(ShaderInstance value, VertexBuffer buffer, Portal portal) {
        value.safeGetUniform("PortalToView").set(portal.view);
        value.safeGetUniform("Opacity").set(portal.alpha);
        value.safeGetUniform("PortalTime").set(portal.time);
        value.safeGetUniform("PortalPhase").set(portal.phase);
        buffer.bind();
        value.apply();
        buffer.draw();
        value.clear();
    }

    private static void createCube() {
        float[][] positions = {{-1, -1, -1}, {1, -1, -1}, {1, 1, -1}, {-1, 1, -1},
                {-1, -1, 1}, {1, -1, 1}, {1, 1, 1}, {-1, 1, 1}};
        // 内向面只绘制射线出口，摄像机进入体积后同样有效。
        int[][] faces = {{0, 1, 2, 3}, {4, 7, 6, 5}, {0, 3, 7, 4}, {1, 5, 6, 2}, {0, 4, 5, 1}, {3, 2, 6, 7}};
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        for (int[] face : faces) {
            for (int corner : face) {
                float[] p = positions[corner];
                builder.addVertex(p[0] * CORE_RADIUS, p[1] * CORE_RADIUS, p[2] * CORE_RADIUS);
            }
        }
        cube = new VertexBuffer(VertexBuffer.Usage.STATIC);
        cube.bind();
        cube.upload(builder.buildOrThrow());
    }

    private static void createBeams() {
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        // 顶点保存曲线进度、横向偏移与光束编号，实际位置由顶点着色器生成。
        for (int beam = 0; beam < 18; beam++) {
            for (int segment = 0; segment < 24; segment++) {
                float first = segment / 24f;
                float last = (segment + 1) / 24f;
                builder.addVertex(first, -1, beam);
                builder.addVertex(last, -1, beam);
                builder.addVertex(last, 1, beam);
                builder.addVertex(first, 1, beam);
            }
        }
        beams = new VertexBuffer(VertexBuffer.Usage.STATIC);
        beams.bind();
        beams.upload(builder.buildOrThrow());
    }

    public static void close() {
        PORTALS.clear();
        if (cube != null) {
            cube.close();
            cube = null;
            beams.close();
            beams = null;
        }
        if (effect != null) {
            effect.destroyBuffers();
            effect = null;
        }
    }

    private record Portal(Matrix4f view, float alpha, float time, float phase) {
    }
}
