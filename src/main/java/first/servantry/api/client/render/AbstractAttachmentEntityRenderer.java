package first.servantry.api.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import first.servantry.api.client.render.renderConfig.ModelConfig;
import first.servantry.api.client.render.renderConfig.TrailConfig;
import first.servantry.api.client.renderType.TrailRenderType;
import first.servantry.api.entity.AttachmentEntity;
import first.servantry.api.entity.PathNode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * 附件实体渲染器抽象基类。
 * <p>
 * 提供完整的拖尾渲染和本体渲染框架，支持强类型配置分离。
 * </p>
 *
 * @param <T> 附件实体类型
 * @see RenderContext
 * @see TrailConfig
 * @see ModelConfig
 */
public abstract class AbstractAttachmentEntityRenderer<T extends AttachmentEntity> implements IAttachmentEntityRenderer<T> {

    // ===================== 核心抽象方法 =====================

    /**
     * 为指定附件实体创建渲染上下文
     */
    protected abstract RenderContext<T> createContext(T entity);

    /** 渲染附件实体本体 */
    protected void renderEntity(T entity, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<T> config) {
    }

    // ===================== 主渲染入口 =====================

    @Override
    public void render(T entity, PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, int packedLight, PathNode renderNode) {
        RenderContext<T> config = createContext(entity);
        if (config == null) return;

        poseStack.pushPose();
        PathNode visualNode = config.model.visualNodeFunction.getVisualNode(entity, partialTick, renderNode);

        AlphaBufferSource alphaBufferSource = new AlphaBufferSource(bufferSource);
        alphaBufferSource.setAlpha(calculateFirstPersonAlpha(config, visualNode, partialTick));

        Vec3 offset = visualNode.pos().subtract(renderNode.pos());
        poseStack.translate(offset.x, offset.y, offset.z);

        if (config.hasTrail()) {
            config.trail.render(entity, poseStack, alphaBufferSource, partialTick, visualNode, TrailRenderType.getTrail());
        }

        renderEntityModel(entity, poseStack, alphaBufferSource, visualNode, config);
        poseStack.popPose();
    }

    // ===================== 模型渲染 =====================

    protected void renderEntityModel(T entity, PoseStack poseStack, MultiBufferSource bufferSource, PathNode node, RenderContext<T> config) {
        ModelConfig<T> model = config.model;
        poseStack.pushPose();

        poseStack.mulPose(Axis.YN.rotationDegrees(node.yaw()));
        poseStack.mulPose(Axis.XP.rotationDegrees(node.pitch()));
        poseStack.mulPose(Axis.ZP.rotationDegrees(node.roll()));

        poseStack.mulPose(Axis.YN.rotationDegrees(model.yawOffset));
        poseStack.mulPose(Axis.XP.rotationDegrees(model.pitchOffset));
        poseStack.mulPose(Axis.ZP.rotationDegrees(model.rollOffset));

        poseStack.scale(model.scale, model.scale, model.scale);
        poseStack.translate(model.translateX, model.translateY, model.translateZ);

        renderEntity(entity, poseStack, bufferSource, node, config);
        poseStack.popPose();
    }

    // ===================== 第一人称透明度计算 =====================

    private float calculateFirstPersonAlpha(RenderContext<T> config, PathNode visualNode, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || !minecraft.options.getCameraType().isFirstPerson()) {
            return 1.0f;
        }

        Vec3 entityPos = visualNode.pos();
        Vec3 eyePos = player.getEyePosition(partialTick);
        double distance = entityPos.distanceTo(eyePos);

        float minDistance = 0.5f * config.model.alphaDistanceFactor;
        float maxDistance = 4.0f * config.model.alphaDistanceFactor;

        if (distance <= minDistance) {
            return 0.0f;
        }
        if (distance >= maxDistance) {
            return 1.0f;
        }

        float alpha = (float) ((distance - minDistance) / (maxDistance - minDistance));
        return Math.max(0.102f, Math.min(1.0f, alpha));
    }
}
