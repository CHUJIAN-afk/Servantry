package first.servantry.client.attachmentEntityRenderer.servant;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.PathNode;
import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.ModelRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.api.client.render.renderConfig.ConeTrailConfig;
import first.servantry.api.client.render.renderConfig.ModelConfig;
import first.servantry.common.servant.DeadlySphere;
import first.servantry.register.ModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;

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

    @Override
    protected void renderEntity(DeadlySphere servant, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<DeadlySphere> config) {
        ModelRenderer.renderModel(ModelRegister.TwinsCursedFlame, poseStack, bufferSource);
    }
}
