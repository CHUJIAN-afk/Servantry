package first.summoner.client.attachmentEntityRenderer.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.dynamicLight.DynamicLightDispatcher;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.entity.PathNode;
import first.summoner.common.minion.VoidEater;
import first.summoner.utils.RenderUtil;
import net.minecraft.client.renderer.MultiBufferSource;

public class VoidEaterRenderer extends AbstractAttachmentEntityRenderer<VoidEater> {

    @Override
    protected RenderContext<VoidEater> createContext(VoidEater dragon) {
        int total = dragon.getTotalSegments();
        int index = dragon.getSegmentIndex();
        boolean isTail = index == total - 1;
        return RenderContext.<VoidEater>builder()
                .model(new ModelConfig<VoidEater>()
                               .translateOffset(-0.5f, isTail ? -0.4845f : -0.425f, isTail ? -0.103075f : -0.5f)
                               .rotationOffset(180, 0, 0))
                .build();
    }

    @Override
    protected void render(VoidEater voidEater, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<VoidEater> context, float partialTick) {
        RenderUtil.renderDebugLabel(poseStack, bufferSource, voidEater);
        DynamicLightDispatcher.addLightSources(visualNode.pos(), 8);
    }
}
