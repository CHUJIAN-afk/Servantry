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

    /** 第一人称视角下，完全透明的距离阈值（方块） */
    private static final float FIRST_PERSON_MIN_DISTANCE = 0.5f;

    /** 第一人称视角下，完全不透明的距离阈值（方块） */
    private static final float FIRST_PERSON_MAX_DISTANCE = 4f;

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

        // 检测是否为第一人称视角且正在渲染当前玩家
        boolean isFirstPerson = isFirstPersonCamera(player);
        Vec3 eyePos = isFirstPerson ? player.getEyePosition(partialTick) : null;

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
                // 计算第一人称视角下的透明度
                float alpha = 1.0f;
                if (isFirstPerson) {
                    PathNode visualNode = renderNode;
                    if (renderer instanceof AbstractAttachmentEntityRenderer<AttachmentEntity> entityAbstractAttachmentEntityRenderer) {
                        visualNode = entityAbstractAttachmentEntityRenderer.createContext(entity).visualNodeFunction.getVisualNode(entity, partialTick, renderNode);
                    }
                    alpha = Math.max(0.105f, calculateFirstPersonAlpha(visualNode.pos(), eyePos));
                }
                // 设置透明度
                alphaBufferSource.setAlpha(alpha);
                renderer.render(entity, poseStack, alphaBufferSource, partialTick, packedLight, renderNode);
            }

            // 调试渲染（使用原始缓冲源，不受透明度影响）
            if (showHitboxes) {
                // 渲染实体位置点（黄色小方块）
                VertexConsumer debugConsumer = bufferSource.getBuffer(RenderType.lines());
                LevelRenderer.renderLineBox(poseStack, debugConsumer, -0.002, -0.002, -0.002, 0.002, 0.002, 0.002, 1.0F, 1.0F, 0.0F, 1.0F);

                // 渲染碰撞箱
                if (entity instanceof ICollideAttack<?> iCollideAttack) {
                    poseStack.pushPose();
                    poseStack.mulPose(Axis.YN.rotationDegrees(renderNode.yaw()));
                    poseStack.mulPose(Axis.XP.rotationDegrees(renderNode.pitch()));
                    poseStack.translate(0, 0, 0.5);
                    poseStack.mulPose(Axis.ZP.rotationDegrees(renderNode.roll()));
                    LevelRenderer.renderLineBox(poseStack, debugConsumer, iCollideAttack.getHitbox(), 1.0F, 0.0F, 0.0F, 1.0F);
                    poseStack.popPose();
                }
            }

            poseStack.popPose();
        }
    }

    /**
     * 检测当前是否为第一人称视角且正在渲染当前玩家。
     *
     * @param player 正在渲染的玩家
     * @return 如果是第一人称视角且是当前玩家返回 true
     */
    private static boolean isFirstPersonCamera(Player player) {
        Minecraft minecraft = Minecraft.getInstance();
        // 检查是否是当前客户端玩家
        if (minecraft.player != player) {
            return false;
        }
        // 检查视角类型（0 = 第一人称，1 = 第三人称背面，2 = 第三人称正面）
        return minecraft.options.getCameraType().isFirstPerson();
    }

    /**
     * 计算第一人称视角下的透明度。
     * <p>
     * 根据附件实体与玩家眼睛的距离，动态计算透明度：
     * <ul>
     *   <li>距离 <= minDistance: 完全透明 (alpha = 0)</li>
     *   <li>距离 >= maxDistance: 完全不透明 (alpha = 1)</li>
     *   <li>介于两者之间: 线性插值</li>
     * </ul>
     * </p>
     *
     * @param entityPos 附件实体的渲染位置
     * @param eyePos    玩家眼睛位置
     * @return 透明度值 [0, 1]
     */
    private static float calculateFirstPersonAlpha(Vec3 entityPos, Vec3 eyePos) {
        double distance = entityPos.distanceTo(eyePos);
        if (distance <= FIRST_PERSON_MIN_DISTANCE) {
            return 0.0f;
        }
        if (distance >= FIRST_PERSON_MAX_DISTANCE) {
            return 1.0f;
        }
        return (float) ((distance - FIRST_PERSON_MIN_DISTANCE) / (FIRST_PERSON_MAX_DISTANCE - FIRST_PERSON_MIN_DISTANCE));
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