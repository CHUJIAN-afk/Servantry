package first.summoner.client.entityRenderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import first.lyra.client.render.rendererHelper.LaserRendererHelper;
import first.lyra.common.entity.PathNode;
import first.summoner.common.master.DoomsdayStar;
import net.minecraft.client.model.SlimeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.SlimeOuterLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class DoomsdayStarRenderer extends AbstractBossRenderer<DoomsdayStar, SlimeModel<DoomsdayStar>> {

    public DoomsdayStarRenderer(EntityRendererProvider.Context context) {
        super(context, new SlimeModel<>(context.bakeLayer(ModelLayers.SLIME)), 1.0F);
        this.addLayer(new SlimeOuterLayer<>(this, context.getModelSet()));
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull DoomsdayStar entity) {
        return ResourceLocation.withDefaultNamespace("textures/entity/slime/slime.png");
    }

    @Override
    protected void scale(@NotNull DoomsdayStar entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(4.0F, 4.0F, 4.0F);
    }

    @Override
    public boolean shouldRender(@NotNull DoomsdayStar livingEntity, @NotNull Frustum camera, double camX, double camY, double camZ) {
        return true;
    }

    @Override
    protected float getModelScaleY(DoomsdayStar entity) {
        return 4.0F;
    }

    @Override
    public void render(@NotNull DoomsdayStar entity, float entityYaw, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight) {
        if (entity.isSummonBeamVisible()) {
            PathNode renderNode = entity.getRenderNode(partialTick);
            Vec3 visualPos = renderNode.pos();
            Vec3 renderOrigin = entity.getPosition(partialTick);
            Vec3 renderOffset = visualPos.subtract(renderOrigin);
            poseStack.pushPose();
            poseStack.translate(renderOffset.x, renderOffset.y, renderOffset.z);
            poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(renderNode.yaw()));
            float length = (float) (visualPos.y() - entity.getSummonBeamEnd().y());
            LaserRendererHelper.builder()
                    .length(length)
                    .radius(1.25F, 1.25F)
                    .layers(4)
                    .segments(4)
                    .color(0x9B4DFF)
                    .alpha(0.75F)
                    .innerRatio((float) Mth.clamp(0.5f - (length * 0.1f / 100), 0.05, 0.5f))
                    .render(poseStack, bufferSource);
            poseStack.popPose();
        }
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}
