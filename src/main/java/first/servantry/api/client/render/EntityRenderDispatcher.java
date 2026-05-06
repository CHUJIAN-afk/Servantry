package first.servantry.api.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import first.servantry.api.PathNode;
import first.servantry.api.entity.AttachmentEntity;
import first.servantry.api.entity.EntityType;
import first.servantry.api.entity.ICollideAttack;
import first.servantry.register.AttachmentRegister;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
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
public class EntityRenderDispatcher {

    /** 渲染器映射表，按实体类型存储对应的渲染器 */
    private static final Map<EntityType<?>, IAttachmentEntityRenderer<?>> renderers = new HashMap<>();

    /**
     * 渲染玩家的所有附件实体。
     *
     * @param player        玩家
     * @param poseStack     矩阵栈
     * @param bufferSource  渲染缓冲源
     * @param partialTick   部分 tick 插值进度
     */
    public static void render(Player player, PoseStack poseStack, MultiBufferSource bufferSource, float partialTick) {
        List<AttachmentEntity> entities = player.getData(AttachmentRegister.EntityData).getEntities();
        Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        boolean showHitboxes = Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes();

        // 创建透明缓冲源包装器
        AlphaBufferSource alphaBufferSource = new AlphaBufferSource(bufferSource);

        for (AttachmentEntity entity : entities) {
            entity.setOwner(player);
            poseStack.pushPose();
            PathNode renderNode = entity.getRenderNode(partialTick);
            poseStack.translate(renderNode.pos().x() - cameraPos.x, renderNode.pos().y() - cameraPos.y, renderNode.pos().z() - cameraPos.z);
            // 渲染实体模型
            IAttachmentEntityRenderer<AttachmentEntity> renderer = getRenderer(entity);
            if (renderer != null) {
                int packedLight = LevelRenderer.getLightColor(player.level(), BlockPos.containing(renderNode.pos().x(), renderNode.pos().y(), renderNode.pos().z()));
                // 计算并设置第一人称视角下的透明度
                alphaBufferSource.setAlpha(calculateFirstPersonAlpha(entity, renderer, renderNode, player, partialTick));
                renderer.render(entity, poseStack, alphaBufferSource, partialTick, packedLight, renderNode);
            }
            // 调试渲染（使用原始缓冲源，不受透明度影响）
            if (showHitboxes) {
                VertexConsumer debugConsumer = bufferSource.getBuffer(RenderType.lines());
                LevelRenderer.renderLineBox(poseStack, debugConsumer, -0.002, -0.002, -0.002, 0.002, 0.002, 0.002, 1.0F, 1.0F, 0.0F, 1.0F);

                if (entity instanceof ICollideAttack<?> iCollideAttack) {
                    poseStack.pushPose();
                    poseStack.mulPose(Axis.YN.rotationDegrees(renderNode.yaw()));
                    poseStack.mulPose(Axis.XP.rotationDegrees(renderNode.pitch()));
                    poseStack.mulPose(Axis.ZP.rotationDegrees(renderNode.roll()));
                    LevelRenderer.renderLineBox(poseStack, debugConsumer, iCollideAttack.getHitbox(), 1.0F, 0.0F, 0.0F, 1.0F);
                    poseStack.popPose();
                }
            }

            poseStack.popPose();
        }
    }

    /**
     * 计算第一人称视角下的透明度。
     * <p>
     * 如果不是第一人称视角或不是当前玩家，返回 1.0。
     * 否则根据附件实体与玩家眼睛的距离计算透明度：
     * <ul>
     *   <li>距离 <= 0.5 方块: 最低透明度 0.105</li>
     *   <li>距离 >= 4.0 方块: 完全不透明 (alpha = 1)</li>
     *   <li>介于两者之间: 线性插值</li>
     * </ul>
     * </p>
     *
     * @param entity     附件实体
     * @param renderer   渲染器
     * @param renderNode 渲染节点
     * @param player     玩家
     * @param partialTick 部分 tick
     * @return 透明度值 [0.105, 1]
     */
    private static float calculateFirstPersonAlpha(AttachmentEntity entity, IAttachmentEntityRenderer<AttachmentEntity> renderer, PathNode renderNode, Player player, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();

        // 检查是否为第一人称视角且是当前玩家
        if (minecraft.player != player || !minecraft.options.getCameraType().isFirstPerson()) {
            return 1.0f;
        }

        // 获取视觉节点位置
        Vec3 entityPos = renderNode.pos();
        if (renderer instanceof AbstractAttachmentEntityRenderer<AttachmentEntity> abstractRenderer) {
            var config = abstractRenderer.createContext(entity);
            if (config != null) {
                entityPos = config.visualNodeFunction.getVisualNode(entity, partialTick, renderNode).pos();
            }
        }

        // 计算距离和透明度
        Vec3 eyePos = player.getEyePosition(partialTick);
        double distance = entityPos.distanceTo(eyePos);

        // 距离阈值：0.5 方块内最低透明度，4.0 方块外完全不透明
        final float minDistance = 0.5f;
        final float maxDistance = 4.0f;
        final float minAlpha = 0.105f;

        if (distance <= minDistance) {
            return minAlpha;
        }
        if (distance >= maxDistance) {
            return 1.0f;
        }

        // 线性插值，但确保不低于最低透明度
        float alpha = (float) ((distance - minDistance) / (maxDistance - minDistance));
        return Math.max(minAlpha, alpha);
    }

    /**
     * 获取附件实体对应的渲染器。
     *
     * @param entity 附件实体实例
     * @return 对应的渲染器，若未注册则返回 null
     */
    @SuppressWarnings("unchecked")
    public static <T extends AttachmentEntity> IAttachmentEntityRenderer<T> getRenderer(T entity) {
        EntityType<T> type = (EntityType<T>) entity.getType();
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
    public static <T extends AttachmentEntity> void register(EntityType<T> type, IAttachmentEntityRenderer<T> renderer) {
        if (!renderers.containsKey(type)) {
            renderers.put(type, renderer);
        }
    }
}