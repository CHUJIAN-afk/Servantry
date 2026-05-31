package first.servantry.client.attachmentEntityRenderer.servant;

import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.api.client.render.renderConfig.ConeTrailConfig;
import first.servantry.api.client.render.renderConfig.ModelConfig;
import first.servantry.common.servant.DeadlySphere;

public class DeadlySphereRenderer extends AbstractAttachmentEntityRenderer<DeadlySphere> {

    @Override
    protected RenderContext<DeadlySphere> createContext(DeadlySphere deadlySphere) {
        int trailTimer = deadlySphere.getTrailTimer();
        return RenderContext.<DeadlySphere>builder()
                .trail(new ConeTrailConfig<DeadlySphere>()
                        .timer(trailTimer)
                        .colorRGB(deadlySphere.getAppearance().getEdgeColor())
                        .historyLength(trailTimer / 2)
                        .maxRadius(0.2f)
                        .minRadiusRatio(0.75f)
                        .resolution(4)
                        .fadeOut(progress -> (1 - progress) * (1 - progress))
                )
                .model(new ModelConfig<DeadlySphere>()
                        .scale(0.5f)
                        .translateOffset(-0.5f, -0.25f, -0.5f)
                        .rotationOffset(180, 0, 0)
                )
                .build();
    }
}
