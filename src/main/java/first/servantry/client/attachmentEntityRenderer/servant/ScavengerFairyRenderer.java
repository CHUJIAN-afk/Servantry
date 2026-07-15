package first.servantry.client.attachmentEntityRenderer.servant;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.client.dynamicLight.DynamicLightDispatcher;
import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.api.client.render.renderConfig.ModelConfig;
import first.servantry.api.entity.PathNode;
import first.servantry.common.servant.ScavengerFairy;
import net.minecraft.client.renderer.MultiBufferSource;

public class ScavengerFairyRenderer extends AbstractAttachmentEntityRenderer<ScavengerFairy> {

    @Override
    protected RenderContext<ScavengerFairy> createContext(ScavengerFairy servant) {
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
    }
}
