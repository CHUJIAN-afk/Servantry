package first.summoner.client.attachmentEntityRenderer.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.dynamicLight.DynamicLightDispatcher;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.entity.PathNode;
import first.summoner.common.minion.ScavengerFairy;
import first.summoner.utils.RenderUtil;
import net.minecraft.client.renderer.MultiBufferSource;

public class ScavengerFairyRenderer extends AbstractAttachmentEntityRenderer<ScavengerFairy> {

    @Override
    protected RenderContext<ScavengerFairy> createContext(ScavengerFairy minion) {
        return RenderContext.<ScavengerFairy>builder()
                .model(new ModelConfig<ScavengerFairy>()
                        .scale(0.2f)
                        .rotationOffset(180, 0, 0)
                        .translateOffset(0, -0.5f, 0)
                        .alphaDistanceFactor(1.25f)
                )
                .build();
    }

    @Override
    protected void render(ScavengerFairy entity, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<ScavengerFairy> context, float partialTick) {
        DynamicLightDispatcher.addLightSources(visualNode.pos(), 12);
        RenderUtil.renderDebugLabel(poseStack, bufferSource, entity);
    }
}
