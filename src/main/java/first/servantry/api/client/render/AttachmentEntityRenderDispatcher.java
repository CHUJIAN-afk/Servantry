package first.servantry.api.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import first.servantry.api.entity.*;
import first.servantry.register.AttachmentRegister;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 附件实体渲染调度器，统一管理所有附件实体（仆从、射弹）的渲染。
 * <p>
 * 在客户端渲染事件中调用，遍历玩家的所有附件实体并调用对应的渲染器进行渲染。
 * </p>
 * <h2>第一人称透明度调整</h2>
 * <p>
 * 当玩家处于第一人称视角时，附件实体可能会遮挡玩家视野。为解决此问题，
 * 根据附件实体与玩家眼睛的距离动态调整透明度。
 * </p>
 */
public class AttachmentEntityRenderDispatcher {

    /** 渲染器映射表，按实体类型存储对应的渲染器 */
    private static final Map<AttachmentEntityType<?>, IAttachmentEntityRenderer<?>> renderers = new HashMap<>();

    /**
     * 渲染玩家的所有附件实体。
     *
     * @param player        玩家
     * @param poseStack     矩阵栈
     * @param bufferSource  渲染缓冲源
     * @param partialTick   部分 tick 插值进度
     */
    public static void render(Player player, PoseStack poseStack, MultiBufferSource bufferSource, float partialTick) {
        List<AttachmentEntity> entities = player.getData(AttachmentRegister.EntityData).getRenderCache();
        Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        boolean showHitboxes = Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes();
        VertexConsumer debugConsumer = showHitboxes ? bufferSource.getBuffer(RenderType.lines()) : null;
        int packedLight = LightTexture.FULL_BRIGHT;
        for (AttachmentEntity entity : entities) {
            entity.setOwner(player);
            poseStack.pushPose();
            PathNode renderNode = entity.getRenderNode(partialTick);
            poseStack.translate(renderNode.pos().x() - cameraPos.x, renderNode.pos().y() - cameraPos.y, renderNode.pos().z() - cameraPos.z);
            // 渲染实体模型
            IAttachmentEntityRenderer<AttachmentEntity> renderer = getRenderer(entity);
            if (renderer != null) {
                renderer.render(entity, poseStack, bufferSource, partialTick, packedLight, renderNode);
            }
            // 调试渲染（使用原始缓冲源，不受透明度影响）
            if (showHitboxes) {
                // 渲染剑尖朝向（蓝色）
                poseStack.pushPose();
                poseStack.mulPose(Axis.YN.rotationDegrees(renderNode.yaw()));
                poseStack.mulPose(Axis.XP.rotationDegrees(renderNode.pitch()));
                poseStack.mulPose(Axis.ZP.rotationDegrees(renderNode.roll()));
                LevelRenderer.renderLineBox(poseStack, debugConsumer, -0.0001, -0.0001, 0, 0.0001, 0.0001, 2, 0, 0, 1, 1.0F);
                // 渲染法向量（绿色）
                LevelRenderer.renderLineBox(poseStack, debugConsumer, -0.0001, 0, -0.0001, 0.0001, 0.5, 0.0001, 0, 0, 1, 1.0F);
                poseStack.popPose();
                LevelRenderer.renderLineBox(poseStack, debugConsumer, -0.002, -0.002, -0.002, 0.002, 0.002, 0.002, 1.0F, 1.0F, 0.0F, 1.0F);
                if (entity instanceof IBlockCollision<?> iBlockCollision) {
                    LevelRenderer.renderLineBox(poseStack, debugConsumer, iBlockCollision.getBlockCollisionBox(), 0.0F, 1.0F, 0.0F, 1.0F);
                }
                if (entity instanceof ICollideAttack<?> iCollideAttack) {
                    if (iCollideAttack.renderHitbox()) {
                        poseStack.pushPose();
                        poseStack.mulPose(Axis.YN.rotationDegrees(renderNode.yaw()));
                        poseStack.mulPose(Axis.XP.rotationDegrees(renderNode.pitch()));
                        poseStack.mulPose(Axis.ZP.rotationDegrees(renderNode.roll()));
                        LevelRenderer.renderLineBox(poseStack, debugConsumer, iCollideAttack.getHitbox(), 1.0F, 0.0F, 0.0F, 1.0F);
                        poseStack.popPose();
                    }
                }
            }
            poseStack.popPose();
        }
    }

    /**
     * 获取附件实体对应的渲染器。
     *
     * @param entity 附件实体实例
     * @return 对应的渲染器，若未注册则返回 null
     */
    @SuppressWarnings("unchecked")
    public static <T extends AttachmentEntity> IAttachmentEntityRenderer<T> getRenderer(T entity) {
        AttachmentEntityType<T> type = (AttachmentEntityType<T>) entity.getType();
        return (IAttachmentEntityRenderer<T>) renderers.get(type);
    }

    /**
     * 注册附件实体类型的渲染器。
     * <p>
     * 每种实体类型只能注册一个渲染器，重复注册将被忽略。
     * </p>
     *
     * @param type     实体类型
     * @param renderer 渲染器实例
     */
    public static <T extends AttachmentEntity> void register(AttachmentEntityType<T> type, IAttachmentEntityRenderer<T> renderer) {
        if (!renderers.containsKey(type)) {
            renderers.put(type, renderer);
        }
    }
}