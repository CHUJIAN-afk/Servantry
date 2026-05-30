package first.servantry.client.attachmentEntityRenderer.servant;

import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.api.client.render.renderConfig.ModelConfig;
import first.servantry.common.servant.ScavengerFairy;

public class ScavengerFairyRenderer extends AbstractAttachmentEntityRenderer<ScavengerFairy> {

    @Override
    protected RenderContext<ScavengerFairy> createContext(ScavengerFairy servant) {
        return RenderContext.<ScavengerFairy>builder()
                .model(new ModelConfig<ScavengerFairy>()
                        .scale(0.2f)
                        .rotationOffset(180, 0, 0)
                        .translateOffset(0, -0.5f, 0)
                        .alphaDistanceFactor(1.25f)
                        .visualNodeFunction((fairy, partialTick, rawNode) -> rawNode)
                )
                .build();
    }
}
