package first.summoner.client.entityRenderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import first.lyra.common.entity.PathNode;
import first.summoner.common.master.PlannedPathEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractBossRenderer<T extends PlannedPathEntity, M extends EntityModel<T>> extends MobRenderer<T, M> {

    private static final float MODEL_Y_OFFSET = 1.501F;

    public AbstractBossRenderer(EntityRendererProvider.Context context, M model, float shadowRadius) {
        super(context, model, shadowRadius);
    }

    @Override
    public void render(T entity, float entityYaw, float partialTick, PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight) {
        PathNode renderNode = entity.getRenderNode(partialTick);
        Vec3 renderOrigin = entity.getPosition(partialTick).add(getRenderOffset(entity, partialTick));
        Vec3 renderOffset = renderNode.pos().subtract(renderOrigin);

        poseStack.pushPose();
        poseStack.translate(renderOffset.x, renderOffset.y, renderOffset.z);
        super.render(entity, renderNode.yaw(), partialTick, poseStack, bufferSource, packedLight);
        poseStack.popPose();
    }

    @Override
    protected void setupRotations(T entity, PoseStack poseStack, float bob, float yBodyRot, float partialTick, float scale) {
        PathNode renderNode = entity.getRenderNode(partialTick);
        poseStack.mulPose(Axis.YN.rotationDegrees(renderNode.yaw()));
        poseStack.mulPose(Axis.XP.rotationDegrees(renderNode.pitch()));
        poseStack.rotateAround(Axis.ZP.rotationDegrees(renderNode.roll()), 0.0F, getRollPivotY(entity), 0.0F);
    }

    protected float getModelScaleY(T entity) {
        return 1.0F;
    }

    private float getRollPivotY(T entity) {
        // LivingEntityRenderer 在渲染器的 Y 缩放之后应用模型基线偏移。
        return MODEL_Y_OFFSET - entity.getBbHeight() / (2.0F * getModelScaleY(entity));
    }
}
