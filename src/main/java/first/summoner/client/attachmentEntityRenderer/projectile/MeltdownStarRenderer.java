package first.summoner.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.dynamicLight.DynamicLightDispatcher;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.rendererHelper.SphereRendererHelper;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.common.projectile.MeltdownStar;
import net.minecraft.client.renderer.MultiBufferSource;

public class MeltdownStarRenderer extends AbstractAttachmentEntityRenderer<MeltdownStar> {

    @Override
    protected RenderContext<MeltdownStar> createContext(MeltdownStar star, float partialTick) {
        return RenderContext.<MeltdownStar>builder().build();
    }

    @Override
    protected void render(MeltdownStar star, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<MeltdownStar> context, float partialTick, int packedLight, float alpha) {
        SphereRendererHelper.builder()
                .radius(0.3F)
                .layers(3)
                .sides(6)
                .color(0xFFD24A)
                .alpha(0.85F * alpha)
                .innerRatio(0.5F)
                .render(poseStack, bufferSource);
        DynamicLightDispatcher.addLightSources(visualNode.pos(), 10);
    }
}
