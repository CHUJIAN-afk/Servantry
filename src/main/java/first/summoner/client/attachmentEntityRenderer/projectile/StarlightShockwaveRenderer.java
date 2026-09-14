package first.summoner.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import first.lyra.client.dynamicLight.DynamicLightDispatcher;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.rendererHelper.LaserRendererHelper;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.common.projectile.StarlightShockwave;
import net.minecraft.client.renderer.MultiBufferSource;

public class StarlightShockwaveRenderer extends AbstractAttachmentEntityRenderer<StarlightShockwave> {

    @Override
    protected RenderContext<StarlightShockwave> createContext(StarlightShockwave shockwave, float partialTick) {
        return RenderContext.<StarlightShockwave>builder().build();
    }

    @Override
    protected void render(StarlightShockwave shockwave, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<StarlightShockwave> context, float partialTick, int packedLight, float alpha) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        LaserRendererHelper.builder()
                .length(0.8F)
                .radius(0.08F, 0.08F)
                .layers(1)
                .segments(4)
                .color(0x9B4DFF)
                .alpha(0.75F * alpha)
                .innerRatio(1.0F)
                .render(poseStack, bufferSource);
        poseStack.popPose();
        DynamicLightDispatcher.addLightSources(visualNode.pos(), 6);
    }
}
