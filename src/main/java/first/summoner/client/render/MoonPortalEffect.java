package first.summoner.client.render;

import com.lowdragmc.lowdraglib2.client.shader.HDRTarget;
import com.lowdragmc.photon.PhotonConfig;
import com.lowdragmc.photon.client.compat.iris.IrisCompat;
import com.lowdragmc.photon.client.gameobject.emitter.renderpipeline.RenderPassPipeline;
import com.lowdragmc.photon.client.postfx.runtime.SceneBlit;
import com.lowdragmc.photon.client.postprocessing.PhotonPostProcessing;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.util.ArrayList;
import java.util.List;

public final class MoonPortalEffect {

    private static final float RADIUS = 2.4f;
    private static final int[] ATTACHMENTS = {GL30.GL_COLOR_ATTACHMENT0, GL30.GL_COLOR_ATTACHMENT1};
    private static final List<Portal> PORTALS = new ArrayList<>();
    private static final Matrix4f PROJECTION = new Matrix4f();
    private static ShaderInstance shader;
    private static ShaderInstance resolveShader;
    private static VertexBuffer cube;
    private static HDRTarget scene;
    private static HDRTarget effect;
    private static HDRTarget lens;
    private static int resolveFramebuffer = -1;

    private MoonPortalEffect() {
    }

    public static void setShader(ShaderInstance value) {
        shader = value;
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
            scene = RenderPassPipeline.resize(scene, main.width, main.height, false);
            effect = RenderPassPipeline.resize(effect, main.width, main.height, false);
            lens = RenderPassPipeline.resize(lens, main.width, main.height, false);
            // 只复制颜色，深度直接采样世界纹理，不受 stencil 格式影响。
            scene.copyColorFrom(main);
            RenderSystem.colorMask(true, true, true, true);
            effect.bindWrite(true);
            GlStateManager._glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT1, GL11.GL_TEXTURE_2D, lens.getColorTextureId(), 0);
            GL20.glDrawBuffers(ATTACHMENTS);
            effect.clear(Minecraft.ON_OSX);
            effect.bindWrite(true);
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.enableCull();
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ONE);
            if (cube == null) {
                createCube();
            }
            shader.setSampler("SceneDepth", main.getDepthTextureId());
            shader.safeGetUniform("PortalProjection").set(PROJECTION);
            Matrix4f inverseProjection = new Matrix4f(PROJECTION).invert();
            shader.safeGetUniform("InverseProjection").set(inverseProjection);
            shader.safeGetUniform("EffectSize").set((float) main.width, (float) main.height);
            shader.safeGetUniform("FogStart").set(RenderSystem.getShaderFogStart());
            shader.safeGetUniform("FogEnd").set(RenderSystem.getShaderFogEnd());
            cube.bind();
            for (Portal portal : PORTALS) {
                Matrix4f inverse = new Matrix4f(portal.view).invert();
                shader.safeGetUniform("PortalToView").set(portal.view);
                shader.safeGetUniform("ViewToPortal").set(inverse);
                shader.safeGetUniform("CameraInPortal").set(inverse.transformPosition(new Vector3f()));
                shader.safeGetUniform("Opacity").set(portal.alpha);
                shader.safeGetUniform("PortalTime").set(portal.time);
                shader.safeGetUniform("PortalPhase").set(portal.phase);
                shader.apply();
                cube.draw();
                shader.clear();
            }
            VertexBuffer.unbind();
            boolean bloom = PhotonConfig.INSTANCE.enableBloom.get()
                    && (!IrisCompat.isUsingShaderPack() || PhotonConfig.INSTANCE.enableBloomWithIrisShader.get());
            if (bloom) {
                PhotonPostProcessing.prepareTarget(main.width, main.height);
            }
            var output = bloom ? PhotonPostProcessing.postTarget(effect) : effect;
            // 最终缓冲只挂世界颜色，采样世界深度时不形成读写反馈。
            if (resolveFramebuffer == -1) {
                resolveFramebuffer = GlStateManager.glGenFramebuffers();
            }
            GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, resolveFramebuffer);
            GlStateManager._glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, main.getColorTextureId(), 0);
            RenderSystem.viewport(0, 0, main.width, main.height);
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.disableBlend();
            RenderSystem.colorMask(true, true, true, false);
            resolveShader.setSampler("SceneColor", scene.getColorTextureId());
            resolveShader.setSampler("SceneDepth", main.getDepthTextureId());
            resolveShader.setSampler("VolumeData", effect.getColorTextureId());
            resolveShader.setSampler("LensData", lens.getColorTextureId());
            resolveShader.setSampler("GlowColor", output.getColorTextureId());
            resolveShader.safeGetUniform("InverseProjection").set(inverseProjection);
            resolveShader.safeGetUniform("EffectSize").set((float) main.width, (float) main.height);
            resolveShader.apply();
            SceneBlit.drawFullscreenQuad();
            resolveShader.clear();
            main.bindWrite(true);
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

    private static void createCube() {
        float[][] positions = {{-1, -1, -1}, {1, -1, -1}, {1, 1, -1}, {-1, 1, -1},
                {-1, -1, 1}, {1, -1, 1}, {1, 1, 1}, {-1, 1, 1}};
        // 内向面只绘制射线出口，摄像机进入体积后同样有效。
        int[][] faces = {{0, 1, 2, 3}, {4, 7, 6, 5}, {0, 3, 7, 4}, {1, 5, 6, 2}, {0, 4, 5, 1}, {3, 2, 6, 7}};
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        for (int[] face : faces) {
            for (int corner : face) {
                float[] p = positions[corner];
                builder.addVertex(p[0] * RADIUS, p[1] * RADIUS, p[2] * RADIUS);
            }
        }
        cube = new VertexBuffer(VertexBuffer.Usage.STATIC);
        cube.bind();
        cube.upload(builder.buildOrThrow());
    }

    public static void close() {
        PORTALS.clear();
        if (cube != null) {
            cube.close();
            cube = null;
        }
        if (scene != null) {
            scene.destroyBuffers();
            effect.destroyBuffers();
            lens.destroyBuffers();
            scene = null;
            effect = null;
            lens = null;
        }
        if (resolveFramebuffer != -1) {
            GlStateManager._glDeleteFramebuffers(resolveFramebuffer);
            resolveFramebuffer = -1;
        }
    }

    private record Portal(Matrix4f view, float alpha, float time, float phase) {
    }
}
