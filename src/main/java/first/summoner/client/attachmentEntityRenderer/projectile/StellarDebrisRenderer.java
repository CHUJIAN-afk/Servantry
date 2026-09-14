package first.summoner.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.dynamicLight.DynamicLightDispatcher;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.rendererHelper.SphereRendererHelper;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.common.projectile.StellarDebris;
import net.minecraft.client.renderer.MultiBufferSource;

public class StellarDebrisRenderer extends AbstractAttachmentEntityRenderer<StellarDebris> {

    @Override
    protected RenderContext<StellarDebris> createContext(StellarDebris debris, float partialTick) {
        return RenderContext.<StellarDebris>builder().build();
    }

    @Override
    protected void render(StellarDebris debris, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<StellarDebris> context, float partialTick, int packedLight, float alpha) {
        SphereRendererHelper.builder()
                .radius(0.28F)
                .layers(1)
                .sides(8)
                .color(0x9B4DFF)
                .alpha(0.65F * alpha)
                .innerRatio(1.0F)
                .render(poseStack, bufferSource);
        SphereRendererHelper.builder()
                .radius(0.17F)
                .layers(1)
                .sides(8)
                .color(0xFFD24A)
                .alpha(0.9F * alpha)
                .innerRatio(1.0F)
                .render(poseStack, bufferSource);
        DynamicLightDispatcher.addLightSources(visualNode.pos(), 8);
    }
}
