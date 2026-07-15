package first.servantry.client.attachmentEntityRenderer.servant;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.ModelRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.api.client.render.renderConfig.ConeTrailConfig;
import first.servantry.api.client.render.renderConfig.ModelConfig;
import first.servantry.api.entity.PathNode;
import first.servantry.common.servant.Twins;
import first.servantry.register.ServantryModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;

public class TwinsRenderer extends AbstractAttachmentEntityRenderer<Twins> {

    @Override
    protected RenderContext<Twins> createContext(Twins twins) {
        int trailTimer = twins.getTrailTimer();
        return RenderContext.<Twins>builder()
                .trail(new ConeTrailConfig<Twins>()
                        .timer(trailTimer)
                        .colorRGB(0xce4949)
                        .historyLength(trailTimer / 2)
                        .maxRadius(0.2f)
                        .minRadiusRatio(0.75f)
                        .resolution(4)
                        .fadeOut(progress -> (1 - progress) * (1 - progress))
                )
                .model(new ModelConfig<Twins>()
                        .scale(0.5f)
                        .translateOffset(-0.5f, -0.25f, -0.5f)
                        .rotationOffset(180, 0, 0)
                )
                .build();
    }

    @Override
    protected void render(Twins servant, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<Twins> context, float partialTick) {
        if (servant.isLaserEye()) {
            ModelRenderer.renderModel(ServantryModelRegister.TWINS_LASER, poseStack, bufferSource);
        } else {
            ModelRenderer.renderModel(ServantryModelRegister.TWINS_CURSED_FLAME, poseStack, bufferSource);
        }
    }
}
