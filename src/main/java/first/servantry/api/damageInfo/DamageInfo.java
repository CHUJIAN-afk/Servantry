package first.servantry.api.damageInfo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import first.servantry.utils.EasingCurve;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.concurrent.ThreadLocalRandom;

/**
 * 伤害数字渲染数据。
 * <p>
 * 渲染参数由 {@link DamageInfoStyle} 驱动，贴图布局固定 0-9 顺序，
 * UV = digit / 10。闪烁为纯亮度脉冲，每 10 tick（0.5 秒）一次。
 * </p>
 */
public class DamageInfo {

    private final DamageInfoStyle style;
    private int lastLife;
    private int life;
    private Vec3 lastPos;
    private Vec3 pos;
    private Vec3 velocity;
    private final float drag;
    private final boolean critical;
    private final float roll;

    /** 缓存：damageAmount 取整后的字符串 */
    private final String text;

    public DamageInfo(DamageInfoStyle style, float damageAmount, Vec3 pos, Vec3 velocity, boolean critical) {
        this.style = style;
        this.lastLife = 0;
        this.life = 0;
        this.pos = pos;
        this.lastPos = pos;
        this.velocity = velocity;
        this.drag = 0.75f;
        this.critical = critical;
        this.roll = ThreadLocalRandom.current().nextInt(-30, 30);
        this.text = String.valueOf((int) damageAmount);
    }

    public boolean tick() {
        lastLife = life;
        life++;
        lastPos = pos;
        velocity = velocity.scale(drag);
        pos = pos.add(velocity);
        return isRemove();
    }

    /** 获取伤害数字贴图 */
    public ResourceLocation getTexture() {
        return style.texture();
    }

    // ===================== 渲染 =====================

    /**
     * 完整渲染本条伤害数字：PoseStack 变换 + 顶点写入。
     * <p>
     * 调用方只需提供已按贴图分组好的 VertexConsumer、相机位置和渲染调度器。
     * </p>
     *
     * @param poseStack   当前 PoseStack
     * @param consumer    已绑定贴图的 VertexConsumer
     * @param alphaSource SuperCacheBufferSource，用于设置逐顶点 alpha
     * @param camPos      相机世界坐标
     * @param dispatcher  实体渲染调度器（获取 cameraOrientation）
     * @param partialTick 插值因子
     */
    public void render(PoseStack poseStack, VertexConsumer consumer, MultiBufferSource alphaSource, Vec3 camPos, EntityRenderDispatcher dispatcher, float partialTick) {
        Vec3 renderPos = getRenderPos(partialTick);

        // 预计算渲染参数
        float scale = getRenderScale(partialTick);
        int color = getRenderColor(partialTick);
        float roll = getRenderRoll(partialTick);

        // 居中偏移
        float offsetX = -getTotalWidth(scale) / 2f;

        poseStack.pushPose();
        // 平移到渲染位置（相对于相机）
        poseStack.translate(renderPos.x() - camPos.x(), renderPos.y() - camPos.y(), renderPos.z() - camPos.z());
        // 面向相机
        poseStack.mulPose(dispatcher.cameraOrientation());
        poseStack.mulPose(Axis.XN.rotationDegrees(180));
        poseStack.mulPose(Axis.ZN.rotationDegrees(roll));
        // 居中偏移
        poseStack.translate(offsetX, 0, 0);

        Matrix4f matrix = poseStack.last().pose();
        float size = style.renderSize() * scale;
        float spacingWorld = style.glyphSpacing() * style.renderSize() / style.glyphPixelWidth();
        float step = size + spacingWorld * scale;
        float halfSize = size * 0.5f;

        int glyphPixelWidth = style.glyphPixelWidth();

        for (int i = 0; i < text.length(); i++) {
            int digit = text.charAt(i) - '0';
            float u0 = (float) (digit * glyphPixelWidth) / style.textureWidth();
            float u1 = (float) ((digit + 1) * glyphPixelWidth) / style.textureWidth();

            float x0 = i * step;
            float x1 = x0 + size;
            float y0 = -halfSize;
            //noinspection UnnecessaryLocalVariable
            float y1 = halfSize;

            consumer.addVertex(matrix, x0, y1, 0).setColor(color).setUv(u0, 1f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(0, 0, 1);
            consumer.addVertex(matrix, x1, y1, 0).setColor(color).setUv(u1, 1f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(0, 0, 1);
            consumer.addVertex(matrix, x1, y0, 0).setColor(color).setUv(u1, 0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(0, 0, 1);
            consumer.addVertex(matrix, x0, y0, 0).setColor(color).setUv(u0, 0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(0, 0, 1);
        }

        poseStack.popPose();
    }

    /** 渲染总宽度（含间距），考虑缩放 */
    private float getTotalWidth(float scale) {
        float size = style.renderSize() * scale;
        float spacingWorld = style.glyphSpacing() * style.renderSize() / style.glyphPixelWidth();
        float step = size + spacingWorld * scale;
        return text.length() * step;
    }

    /**
     * 获取渲染颜色（ARGB）。
     * <p>
     * 暴击使用 criticalColor，普通使用 color。
     * 闪烁为纯亮度脉冲：每 10 tick（0.5 秒）一次 sin 波，随 progress 衰减。
     * 透明度由 EASE_IN_OUT_QUAD 缓动曲线驱动淡入淡出。
     * </p>
     */
    private int getRenderColor(float partialTick) {
        float progress = Mth.lerp(partialTick, lastLife, life) / style.maxLife();
        int baseColor = critical ? style.criticalColor() : style.color();

        // 亮度脉冲：每 10 tick 一个完整周期
        float flicker = (Mth.sin(progress * style.maxLife() * Mth.PI / 5f) + 1f) * 0.5f;
        float weight = flicker * (1f - progress);

        int r = (baseColor >> 16) & 0xFF;
        int g = (baseColor >> 8) & 0xFF;
        int b = baseColor & 0xFF;

        // 亮度叠加：向 255 方向提亮
        r = Mth.lerpInt(weight, r, Math.min(255, (int) (r * 1.3f)));
        g = Mth.lerpInt(weight, g, Math.min(255, (int) (g * 1.3f)));
        b = Mth.lerpInt(weight, b, Math.min(255, (int) (b * 1.3f)));

        // 透明度：缓动淡入淡出
        float easedProgress = EasingCurve.EASE_IN_OUT_QUAD.apply(progress);
        int a;
        if (easedProgress < 0.2f) {
            a = Mth.lerpInt(easedProgress / 0.2f, 51, 255); // 0.2*255=51
        } else if (easedProgress > 0.9f) {
            a = Mth.lerpInt((easedProgress - 0.9f) / 0.1f, 255, 0);
        } else {
            a = 255;
        }

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private float getRenderRoll(float partialTick) {
        float progress = Mth.lerp(partialTick, lastLife, life) / style.maxLife();
        return Mth.lerp(progress, roll, 0);
    }

    private float getRenderScale(float partialTick) {
        float progress = Mth.lerp(partialTick, lastLife, life) / style.maxLife();
        progress = EasingCurve.EASE_IN_OUT_QUAD.apply(progress);
        if (progress < 0.1) {
            return Mth.lerp(progress / 0.1f, 0.5f, 1.0f);
        }
        if (progress > 0.9f) {
            return Mth.lerp((progress - 0.9f) / 0.1f, 1.0f, 0.0f);
        }
        return 1;
    }

    public Vec3 getRenderPos(float partialTick) {
        return lastPos.lerp(pos, partialTick);
    }

    public boolean isRemove() {
        return life >= style.maxLife() - 1;
    }
}
