package first.servantry.client.renderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import first.servantry.api.client.IProjectileRenderer;
import first.servantry.api.client.IProjectileTrailRenderer;
import first.servantry.api.projectile.Projectile;
import first.servantry.api.servant.PathNode;
import first.servantry.common.projectile.StardustProjectile;
import first.servantry.register.ItemRegister;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;

/**
 * 星细胞射弹渲染器。
 * <p>
 * 实现 {@link IProjectileRenderer} 和 {@link IProjectileTrailRenderer} 接口，
 * 渲染缩小版星尘细胞模型（0.25倍）。
 * 飞行状态渲染圆锥拖尾，黏贴状态仅渲染模型。
 * </p>
 */
public class StardustProjectileRenderer implements IProjectileRenderer<StardustProjectile>, IProjectileTrailRenderer {

    // ===================== IProjectileTrailRenderer 实现 =====================

    @Override
    public int getTrailTimer(Projectile projectile) {
        return projectile.getTrailTimer();
    }

    @Override
    public int getTrailHistoryLength() {
        return 6;
    }

    @Override
    public int getTrailSegmentsPerNode() {
        return 3;
    }

    @Override
    public float getTrailMaxRadius() {
        return 0.15f;
    }

    @Override
    public int getTrailResolution() {
        return 8;
    }

    @Override
    public float getTrailFadeOut(float progress) {
        return (float) Math.pow(Math.max(0.0f, 1.0f - progress), 2.0);
    }

    @Override
    public int getTrailColorRGB(float progress) {
        return 0x8AE0FF;
    }

    // ===================== IProjectileRenderer 实现 =====================

    @Override
    public void render(StardustProjectile projectile, PoseStack poseStack, MultiBufferSource bufferSource,
                       float partialTick, int packedLight, PathNode renderNode) {

        // 飞行状态：渲染拖尾
        if (projectile.getState() == Projectile.ProjectileState.FLYING && projectile.getTrailTimer() > 0) {
            processTrailRender(poseStack, bufferSource, partialTick, projectile, renderNode);
        }

        // 渲染模型
        renderModel(poseStack, bufferSource, renderNode);
    }

    /**
     * 渲染缩小版星尘细胞模型。
     */
    private void renderModel(PoseStack poseStack, MultiBufferSource bufferSource, PathNode renderNode) {
        poseStack.pushPose();

        // 应用旋转（偏航、俯仰、滚转）
        poseStack.mulPose(Axis.YN.rotationDegrees(renderNode.yaw()));
        poseStack.mulPose(Axis.XP.rotationDegrees(renderNode.pitch()));
        poseStack.mulPose(Axis.ZP.rotationDegrees(renderNode.roll()));

        // 缩放
        poseStack.scale(0.25f, 0.25f, 0.25f);

        // 渲染物品模型，强制全亮度
        Minecraft.getInstance().getItemRenderer().renderStatic(
                ItemRegister.StardustCell.get().getDefaultInstance(),
                ItemDisplayContext.FIXED,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                bufferSource,
                Minecraft.getInstance().level,
                0
        );

        poseStack.popPose();
    }
}