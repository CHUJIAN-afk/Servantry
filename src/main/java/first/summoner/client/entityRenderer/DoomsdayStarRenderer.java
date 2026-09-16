package first.summoner.client.entityRenderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.model.LyraModelRenderer;
import first.lyra.client.render.model.virtual.VirtualEntityPose;
import first.lyra.client.render.rendererHelper.LaserRendererHelper;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.common.master.DoomsdayStar;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;

public class DoomsdayStarRenderer extends AbstractAttachmentEntityRenderer<DoomsdayStar> {

    @Override
    protected RenderContext<DoomsdayStar> createContext(DoomsdayStar doomsdayStar, float partialTick) {
        return RenderContext.<DoomsdayStar>builder()
                .model(new ModelConfig<DoomsdayStar>()
                               .scale(4.0F)
                               .translateOffset(0, -0.25f, 0)
                               .alphaDistanceFactor(0.0F))
                .build();
    }

    @Override
    public void render(DoomsdayStar doomsdayStar, PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, int packedLight, PathNode visualNode) {
        super.render(doomsdayStar, poseStack, bufferSource, partialTick, packedLight, visualNode);
        if (doomsdayStar.isFallLaserIndicatorRender()) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(-visualNode.yaw() + 45));
            float fallLaserIndicatorLength = (float) (visualNode.pos().y() - doomsdayStar.getFallLaserIndicatorEnd().y());
            LaserRendererHelper.builder()
                    .length(fallLaserIndicatorLength)
                    .radius(1.5F, 1.5F)
                    .layers(4)
                    .segments(4)
                    .color(0x9B4DFF)
                    .alpha(0.75F)
                    .innerRatio(Mth.clamp(0.5F - (fallLaserIndicatorLength * 0.1F / 100.0F), 0.05F, 0.5F))
                    .render(poseStack, bufferSource);
            poseStack.popPose();
        }
    }

    @Override
    protected void render(DoomsdayStar doomsdayStar, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<DoomsdayStar> context, float partialTick, int packedLight, float alpha) {
        poseStack.pushPose();
        float jumpScale = doomsdayStar.getScale(partialTick);
        float v = (1 - jumpScale) * 0.3f;
        poseStack.scale(1 + v, jumpScale, 1 + v);
        LyraModelRenderer.virtualEntity(EntityType.SLIME, partialTick)
                .pose(VirtualEntityPose.create()
                        .ageTicks(doomsdayStar.getTickCount())
                        .look(0.0F, 0.0F, 0.0F)
                        .packedLight(packedLight)
                        .alpha(alpha))
                .render(poseStack, bufferSource);
        poseStack.popPose();
    }
}
