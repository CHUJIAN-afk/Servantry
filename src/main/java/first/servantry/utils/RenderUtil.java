package first.servantry.utils;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class RenderUtil {

    private static final Map<Object, Integer> COLOR_CACHE = new WeakHashMap<>();

    public static int getDominantColor(ItemStack itemStack) {
        if (!itemStack.isEmpty()) {
            return getDominantColor(Minecraft.getInstance().getItemRenderer().getModel(itemStack, null, null, 0));
        }
        return 0xFFFFFF;
    }

    public static int getDominantColor(BakedModel bakedModel) {
        return COLOR_CACHE.computeIfAbsent(bakedModel, k -> extractDominantColor(bakedModel));
    }

    @SuppressWarnings("deprecation")
    public static int extractDominantColor(BakedModel model) {
        try {
            Minecraft minecraft = Minecraft.getInstance();
            ClientLevel level = minecraft.level;
            if (level == null) return 0xFFFFFF;
            List<BakedQuad> quads = model.getQuads(null, null, level.random);
            if (quads.isEmpty()) {
                for (Direction dir : Direction.values()) {
                    quads = model.getQuads(null, dir, minecraft.level.random);
                    if (!quads.isEmpty()) break;
                }
            }
            if (quads.isEmpty()) return 0xFFFFFF;

            TextureAtlasSprite sprite = quads.getFirst().getSprite();
            SpriteContents contents = sprite.contents();

            int width = contents.width();
            int height = contents.height();
            if (width <= 0 || height <= 0) return 0xFFFFFF;

            long totalR = 0, totalG = 0, totalB = 0;
            long count = 0;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int pixel = contents.getOriginalImage().getPixelRGBA(x, y);
                    int a = (pixel >> 24) & 0xFF;
                    if (a < 128) continue;
                    int r = pixel & 0xFF;
                    int g = (pixel >> 8) & 0xFF;
                    int b = (pixel >> 16) & 0xFF;
                    totalR += r;
                    totalG += g;
                    totalB += b;
                    count++;
                }
            }
            if (count == 0) return 0xFFFFFF;
            return ((int) (totalR / count) << 16) | ((int) (totalG / count) << 8) | (int) (totalB / count);
        } catch (Exception e) {
            return 0xFFFFFF;
        }
    }

    public static final ResourceLocation BEAM_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/beacon_beam.png");

    /**
     * 在空间中渲染一根光柱。
     *
     * @param poseStack    姿态栈
     * @param bufferSource 缓冲源
     * @param start        光柱起点（世界坐标）
     * @param end          光柱终点（世界坐标）
     * @param width        光柱宽度（半径）
     * @param color        ARGB 颜色
     * @param yaw          光柱绕自身纵轴的旋转角度（度）
     * @param partialTick  部分刻
     */
    public static void renderBeam(PoseStack poseStack, MultiBufferSource bufferSource, Vec3 start, Vec3 end, float width, int color, float yaw, float partialTick) {
        Vec3 diff = end.subtract(start);
        double length = diff.length();
        if (length < 0.001) {
            return;
        }

        // 计算从 Y 轴正方向到光柱方向的旋转
        Vec3 dir = diff.normalize();
        float pitchAngle = (float) Math.toDegrees(Math.acos(Mth.clamp(dir.y, -1, 1)));
        float yawAngle = (float) Math.toDegrees(Math.atan2(-dir.x, dir.z));

        long gameTime = 0;
        if (Minecraft.getInstance().level != null) {
            gameTime = Minecraft.getInstance().level.getGameTime();
        }
        float f = (float) Math.floorMod(gameTime, 40) + partialTick;
        float f1 = (float) length < 0 ? f : -f;
        float f2 = Mth.frac(f1 * 0.2F - (float) Mth.floor(f1 * 0.1F));

        poseStack.pushPose();
        poseStack.translate(start.x, start.y, start.z);
        poseStack.mulPose(Axis.YP.rotationDegrees(yawAngle));
        poseStack.mulPose(Axis.XP.rotationDegrees(-pitchAngle));
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw + f * 2.25F - 45.0F));

        float glowRadius = width * 1.25F;
        float minV = -1.0F + f2;
        float maxV = (float) length * 0.5F / width + minV;

        VertexConsumer beamConsumer = bufferSource.getBuffer(RenderType.beaconBeam(BEAM_LOCATION, false));
        renderBeamPart(poseStack, beamConsumer, color, (int) length, width, minV, maxV);

        VertexConsumer glowConsumer = bufferSource.getBuffer(RenderType.beaconBeam(BEAM_LOCATION, true));
        renderBeamPart(poseStack, glowConsumer, ARGB32.color(32, color), (int) length, glowRadius, minV, minV + (float) length);

        poseStack.popPose();
    }

    private static void renderBeamPart(PoseStack poseStack, VertexConsumer consumer, int color, int height, float radius, float minV, float maxV) {
        PoseStack.Pose pose = poseStack.last();
        // 四个面，每面一个 quad，沿 Y 轴展开
        renderQuad(pose, consumer, color, height, -radius, -radius, radius, -radius, minV, maxV);
        renderQuad(pose, consumer, color, height, radius, -radius, radius, radius, minV, maxV);
        renderQuad(pose, consumer, color, height, radius, radius, -radius, radius, minV, maxV);
        renderQuad(pose, consumer, color, height, -radius, radius, -radius, -radius, minV, maxV);
    }

    private static void renderQuad(PoseStack.Pose pose, VertexConsumer consumer, int color, int maxY, float minX, float minZ, float maxX, float maxZ, float minV, float maxV) {
        addVertex(pose, consumer, color, maxY, minX, minZ, (float) 1.0, minV);
        addVertex(pose, consumer, color, 0, minX, minZ, (float) 1.0, maxV);
        addVertex(pose, consumer, color, 0, maxX, maxZ, (float) 0.0, maxV);
        addVertex(pose, consumer, color, maxY, maxX, maxZ, (float) 0.0, minV);
    }

    private static void addVertex(PoseStack.Pose pose, VertexConsumer consumer, int color, float y, float x, float z, float u, float v) {
        consumer.addVertex(pose, x, y, z).setColor(color).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, 0.0F, 1.0F, 0.0F);
    }
}
