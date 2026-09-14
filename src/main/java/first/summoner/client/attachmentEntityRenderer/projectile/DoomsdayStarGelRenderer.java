package first.summoner.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.dynamicLight.DynamicLightDispatcher;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.rendererHelper.SphereRendererHelper;
import first.lyra.common.entity.PathNode;
import first.summoner.common.projectile.DoomsdayStarGel;
import net.minecraft.client.renderer.MultiBufferSource;

public class DoomsdayStarGelRenderer extends AbstractAttachmentEntityRenderer<DoomsdayStarGel> {

    @Override
    protected RenderContext<DoomsdayStarGel> createContext(DoomsdayStarGel gel, float partialTick) {
        return RenderContext.<DoomsdayStarGel>builder().build();
    }

    @Override
    protected void render(DoomsdayStarGel gel, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<DoomsdayStarGel> context, float partialTick, int packedLight, float alpha) {
        SphereRendererHelper.builder()
                .radius(0.25F)
                .layers(3)
                .sides(6)
                .color(0x9B4DFF)
                .alpha(0.75F * alpha)
                .innerRatio(0.5F)
                .render(poseStack, bufferSource);
        DynamicLightDispatcher.addLightSources(visualNode.pos(), 8);
    }
}
