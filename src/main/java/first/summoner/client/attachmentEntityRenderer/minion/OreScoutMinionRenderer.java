package first.summoner.client.attachmentEntityRenderer.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.dynamicLight.DynamicLightDispatcher;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.entity.PathNode;
import first.summoner.common.minion.OreScout;
import first.summoner.utils.RenderUtil;
import net.minecraft.client.renderer.MultiBufferSource;

public class OreScoutMinionRenderer extends AbstractAttachmentEntityRenderer<OreScout> {

    @Override
    protected RenderContext<OreScout> createContext(OreScout minion) {
        return RenderContext.<OreScout>builder()
                .model(new ModelConfig<OreScout>()
                        .scale(0.4f)
                        .translateOffset(-0.5f, -0.5f, -0.5f)
                        .alphaDistanceFactor(1.6f)
                )
                .build();
    }

    @Override
    protected void render(OreScout minion, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<OreScout> context, float partialTick) {
        DynamicLightDispatcher.addLightSources(visualNode.pos(), 12);
        RenderUtil.renderDebugLabel(poseStack, bufferSource, minion);
    }
}
