package first.summoner.client.render;

import com.lowdragmc.lowdraglib2.client.shader.HDRTarget;
import com.lowdragmc.photon.PhotonConfig;
import com.lowdragmc.photon.client.compat.iris.IrisCompat;
import com.lowdragmc.photon.client.gameobject.emitter.renderpipeline.RenderPassPipeline;
import com.lowdragmc.photon.client.postfx.runtime.SceneBlit;
import com.lowdragmc.photon.client.postprocessing.PhotonPostProcessing;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import first.summoner.common.entity.Astrageldon;
import first.summoner.common.entity.astrageldon.AstrageldonArena;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class AstrageldonArenaEffect {

    static final Matrix4f CLIP_TO_CAMERA = new Matrix4f();
    static final Vector4f TERRAIN_SPHERE = new Vector4f();
    static final Vector3f GRID_OFFSET = new Vector3f();
    private static final Matrix4f CAMERA_TO_CLIP = new Matrix4f();
    private static final List<Arena> ARENAS = new ArrayList<>();
    private static ShaderInstance shader;
    private static ShaderInstance resolveShader;
    private static HDRTarget effect;
    private static Astrageldon terrainArena;
    private static Vec3 cameraPosition = Vec3.ZERO;
    private static Vec3 terrainCenter = Vec3.ZERO;
    private static float arenaPartialTick;

    private AstrageldonArenaEffect() {
    }

    public static void setShader(ShaderInstance value) {
        shader = value;
    }

    public static void setResolveShader(ShaderInstance value) {
        resolveShader = value;
    }

    public static void beginFrame(Camera camera, Matrix4f view, Matrix4f projection, float partialTick) {
        if (!IrisCompat.isShadowPass()) {
            ARENAS.clear();
            terrainArena = null;
            TERRAIN_SPHERE.zero();
            cameraPosition = camera.getPosition();
            arenaPartialTick = partialTick;
            CAMERA_TO_CLIP.set(projection).mul(view);
            CLIP_TO_CAMERA.set(CAMERA_TO_CLIP).invert();
            GRID_OFFSET.set((float) (cameraPosition.x - Math.floor(cameraPosition.x)),
                    (float) (cameraPosition.y - Math.floor(cameraPosition.y)), (float) (cameraPosition.z - Math.floor(cameraPosition.z)));
            var minecraft = Minecraft.getInstance();
            Vec3 selection = minecraft.player == null ? cameraPosition : minecraft.player.getPosition(partialTick).add(0, minecraft.player.getBbHeight() / 2, 0);
            terrainArena = AstrageldonArena.nearest(minecraft.level.entitiesForRendering(), selection, Double.MAX_VALUE, partialTick);
            if (terrainArena != null) {
                terrainCenter = terrainArena.getRenderArenaCenter(partialTick);
                Vec3 center = terrainCenter.subtract(cameraPosition);
                TERRAIN_SPHERE.set((float) center.x, (float) center.y, (float) center.z, terrainArena.getRenderArenaRadius(partialTick));
            }
        }
    }

    public static void submit(Astrageldon boss, float partialTick) {
        if (boss.hasArena() && !IrisCompat.isShadowPass()) {
            Vec3 relative = boss.getRenderArenaCenter(arenaPartialTick).subtract(cameraPosition);
            float time = (boss.level().getGameTime() % 240000 + partialTick) / 20f;
            ARENAS.add(new Arena(relative.toVector3f(), boss.getRenderArenaRadius(arenaPartialTick), time));
        }
    }

    public static boolean outlineBlockEntity(BlockEntity block, PoseStack poses, MultiBufferSource buffers) {
        boolean inside = terrainArena != null && !IrisCompat.isShadowPass()
                && Vec3.atCenterOf(block.getBlockPos()).distanceToSqr(terrainCenter) < TERRAIN_SPHERE.w * TERRAIN_SPHERE.w;
        if (inside) {
            VertexConsumer lines = buffers.getBuffer(RenderType.lines());
            PoseStack.Pose pose = poses.last();
            block.getBlockState().getShape(block.getLevel(), block.getBlockPos()).forAllEdges((x0, y0, z0, x1, y1, z1) -> {
                Vector3f direction = new Vector3f((float) (x1 - x0), (float) (y1 - y0), (float) (z1 - z0)).normalize();
                lines.addVertex(pose.pose(), (float) x0, (float) y0, (float) z0).setColor(0.65f, 0.32f, 1f, 1f).setNormal(pose, direction.x, direction.y, direction.z);
                lines.addVertex(pose.pose(), (float) x1, (float) y1, (float) z1).setColor(0.65f, 0.32f, 1f, 1f).setNormal(pose, direction.x, direction.y, direction.z);
            });
        }
        return inside;
    }

    public static void render() {
        if (!ARENAS.isEmpty()) {
            var main = Minecraft.getInstance().getMainRenderTarget();
            ShaderInstance previous = RenderSystem.getShader();
            int width = Math.max(1, main.width / 2);
            int height = Math.max(1, main.height / 2);
            effect = RenderPassPipeline.resize(effect, width, height, false);
            RenderSystem.disableScissor();
            RenderSystem.colorMask(true, true, true, true);
            shader.safeGetUniform("EffectSize").set((float) width, (float) height);
            shader.safeGetUniform("ClipToCamera").set(CLIP_TO_CAMERA);
            resolveShader.safeGetUniform("EffectSize").set((float) main.width, (float) main.height);
            resolveShader.safeGetUniform("ClipToCamera").set(CLIP_TO_CAMERA);
            resolveShader.safeGetUniform("CameraToClip").set(CAMERA_TO_CLIP);
            boolean bloom = PhotonConfig.INSTANCE.enableBloom.get()
                    && (!IrisCompat.isUsingShaderPack() || PhotonConfig.INSTANCE.enableBloomWithIrisShader.get());
            if (bloom) {
                PhotonPostProcessing.prepareTarget(main.width, main.height);
            }
            ARENAS.sort(Comparator.comparingDouble((Arena arena) -> arena.center.lengthSquared()).reversed());
            for (Arena arena : ARENAS) {
                RenderSystem.colorMask(true, true, true, true);
                effect.clear(Minecraft.ON_OSX);
                effect.bindWrite(true);
                shader.safeGetUniform("ArenaCenter").set(arena.center);
                shader.safeGetUniform("ArenaRadius").set(arena.radius);
                shader.safeGetUniform("ArenaTime").set(arena.time);
                shader.apply();
                RenderSystem.disableDepthTest();
                RenderSystem.depthMask(false);
                RenderSystem.disableCull();
                RenderSystem.disableBlend();
                SceneBlit.drawFullscreenQuad();
                shader.clear();

                var output = bloom ? PhotonPostProcessing.postTarget(effect) : effect;
                main.bindWrite(true);
                resolveShader.setSampler("ArenaColor", effect.getColorTextureId());
                resolveShader.setSampler("GlowColor", output.getColorTextureId());
                resolveShader.safeGetUniform("ArenaCenter").set(arena.center);
                resolveShader.safeGetUniform("ArenaRadius").set(arena.radius);
                resolveShader.apply();
                // 球面在全分辨率写入真实深度，避免半分辨率遮挡掩码产生块状空洞。
                RenderSystem.enableDepthTest();
                RenderSystem.depthFunc(GL11.GL_LEQUAL);
                RenderSystem.depthMask(true);
                RenderSystem.disableCull();
                RenderSystem.enableBlend();
                RenderSystem.blendFuncSeparate(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ZERO, GL11.GL_ONE);
                RenderSystem.colorMask(true, true, true, false);
                SceneBlit.drawFullscreenQuad();
                resolveShader.clear();
            }
            RenderSystem.colorMask(true, true, true, true);
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(() -> previous);
            ARENAS.clear();
        }
        TERRAIN_SPHERE.w = 0;
    }

    public static void close() {
        ARENAS.clear();
        terrainArena = null;
        terrainCenter = Vec3.ZERO;
        TERRAIN_SPHERE.zero();
        if (effect != null) {
            effect.destroyBuffers();
            effect = null;
        }
    }

    private record Arena(Vector3f center, float radius, float time) {
    }
}
