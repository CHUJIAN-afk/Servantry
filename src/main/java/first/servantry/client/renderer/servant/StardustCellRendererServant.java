package first.servantry.client.renderer.servant;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import first.servantry.api.client.IServantConeTrailRenderer;
import first.servantry.api.client.IServantRenderer;
import first.servantry.api.servant.PathNode;
import first.servantry.api.servant.Servant;
import first.servantry.common.servent.StardustCell;
import first.servantry.register.ItemRegister;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;

/**
 * 星尘细胞渲染器，同时实现物品模型渲染和圆锥拖尾特效。
 * <p>
 * 该渲染器通过实现 {@link IServantConeTrailRenderer} 将拖尾逻辑与仆从实体解耦，
 * 拖尾的颜色、半径、淡出曲线等参数在此统一配置。
 * </p>
 * <p>
 * 渲染顺序：先绘制拖尾（若计时器有效），再绘制仆从的本体模型（使用物品渲染）。
 * </p>
 */
public class StardustCellRendererServant implements IServantConeTrailRenderer, IServantRenderer<StardustCell> {

    // ===================== IServantConeTrailRenderer 实现 =====================
    @Override
    public int getTrailTimer(Servant servant) {
        return ((StardustCell) servant).trailTimer;
    }

    @Override
    public float getTrailMaxRadius() {
        return 0.2f; // 与原实现保持一致
    }

    @Override
    public int getTrailResolution() {
        return 12; // 原实现中使用 12 边形，更圆滑
    }

    @Override
    public float getTrailFadeOut(float progress) {
        // 使用平方衰减，末端更快速消失，与原逻辑一致
        return (float) Math.pow(Math.max(0.0f, 1.0f - progress), 2.0);
    }

    @Override
    public int getTrailColorRGB(float progress) {
        // 星尘细胞的拖尾为淡蓝色 (#8AE0FF)
        return 0x8AE0FF;
    }

    /**
     * 重写视觉节点插值，使拖尾的头部跟随仆从的旋转动画。
     * <p>
     * 星尘细胞在客户端会持续自旋（renderYaw/Pitch/Roll 每 tick 增加 2°），
     * 这里返回插值后的节点，保证拖尾的朝向与模型渲染一致。
     * </p>
     */
    @Override
    public PathNode getVisualRenderNode(Servant servant, float partialTick, PathNode rawRenderNode) {
        StardustCell cell = (StardustCell) servant;
        float y = cell.getRenderYaw(partialTick);
        float p = cell.getRenderPitch(partialTick);
        float r = cell.getRenderRoll(partialTick);
        return new PathNode(rawRenderNode.pos(), y, p, r);
    }

    // ===================== IServantRenderer 实现 =====================

    /**
     * 渲染星尘细胞的本体及拖尾。
     *
     * @param servant      星尘细胞实例
     * @param poseStack    矩阵栈
     * @param bufferSource 渲染缓冲源
     * @param partialTick  部分 tick 插值
     * @param packedLight  光照值（此处未使用，内部强制全亮度）
     * @param renderNode   原始渲染节点（未经客户端动画插值）
     */
    @Override
    public void render(StardustCell servant, PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, int packedLight, PathNode renderNode) {
        // 渲染仆从模型
        PathNode visualNode = getVisualRenderNode(servant, partialTick, renderNode);
        poseStack.pushPose();
        // 应用视觉节点相对于原始节点的偏移（插值导致的位置微调）
        Vec3 offset = visualNode.pos().subtract(renderNode.pos());
        poseStack.translate(offset.x, offset.y, offset.z);
        renderModel(servant, poseStack, bufferSource, visualNode);
        poseStack.popPose();
    }

    /**
     * 渲染星尘细胞的本体模型（使用注册的物品模型）。
     */
    private void renderModel(StardustCell servant, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode) {
        poseStack.pushPose();

        // 应用旋转（偏航、俯仰、滚转）
        poseStack.mulPose(Axis.YN.rotationDegrees(visualNode.yaw()));
        poseStack.mulPose(Axis.XP.rotationDegrees(visualNode.pitch()));
        poseStack.mulPose(Axis.ZP.rotationDegrees(visualNode.roll()));

        // 缩放到合适大小
        poseStack.scale(0.5f, 0.5f, 0.5f);

        // 渲染物品模型，强制全亮度、无叠加层
        Minecraft.getInstance().getItemRenderer().renderStatic(
                ItemRegister.StardustCell.get().getDefaultInstance(),
                ItemDisplayContext.FIXED,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                bufferSource,
                servant.getOwner().level(),
                0
        );

        poseStack.popPose();
    }
}