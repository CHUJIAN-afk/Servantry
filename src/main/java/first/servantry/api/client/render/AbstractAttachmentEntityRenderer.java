package first.servantry.api.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import first.servantry.api.client.dynamicLight.DynamicLightDispatcher;
import first.servantry.api.client.dynamicLight.DynamicLightRenderer;
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

    /**
     * 为指定附件实体创建渲染上下文
     */
    protected abstract RenderContext<T> createContext(T entity);

    /** 渲染附件实体本体 */
    protected abstract void render(T entity, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<T> context);

    @Override
    public void render(T entity, PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, int packedLight, PathNode visualNode) {
        RenderContext<T> context = createContext(entity);
        if (context != null) {
            poseStack.pushPose();
            AlphaBufferSource alphaBufferSource = new AlphaBufferSource(bufferSource);
            alphaBufferSource.setAlpha(getAlphaModify(context, visualNode, partialTick));
            if (context.hasTrail()) {
                context.trail.render(entity, poseStack, alphaBufferSource, partialTick, visualNode, TrailRenderType.getTrail());
            }
            modelModify(entity, poseStack, alphaBufferSource, visualNode, context);
            poseStack.popPose();
            if (this instanceof DynamicLightRenderer<?>) {
                @SuppressWarnings("unchecked") DynamicLightRenderer<T> dynamicLightRenderer = (DynamicLightRenderer<T>) this;
                DynamicLightDispatcher.addLightSources(dynamicLightRenderer.getDynamicLight(entity, context, visualNode));
            }
        }
    }

    protected void modelModify(T entity, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<T> context) {
        ModelConfig<T> model = context.model;
        poseStack.pushPose();

        poseStack.mulPose(Axis.YN.rotationDegrees(visualNode.yaw()));
        poseStack.mulPose(Axis.XP.rotationDegrees(visualNode.pitch()));
        poseStack.mulPose(Axis.ZP.rotationDegrees(visualNode.roll()));

        poseStack.mulPose(Axis.YN.rotationDegrees(model.yawOffset));
        poseStack.mulPose(Axis.XP.rotationDegrees(model.pitchOffset));
        poseStack.mulPose(Axis.ZP.rotationDegrees(model.rollOffset));

        poseStack.scale(model.scale, model.scale, model.scale);
        poseStack.translate(model.translateX, model.translateY, model.translateZ);

        render(entity, poseStack, bufferSource, visualNode, context);
        poseStack.popPose();
    }

    protected float getAlphaModify(RenderContext<T> config, PathNode visualNode, float partialTick) {
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
