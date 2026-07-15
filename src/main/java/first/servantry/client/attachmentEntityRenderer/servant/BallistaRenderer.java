package first.servantry.client.attachmentEntityRenderer.servant;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.ModelRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.api.client.render.renderConfig.ModelConfig;
import first.servantry.api.entity.PathNode;
import first.servantry.common.sentryServant.Ballista;
import first.servantry.register.ServantryModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;

public class BallistaRenderer extends AbstractAttachmentEntityRenderer<Ballista> {

    @Override
    protected RenderContext<Ballista> createContext(Ballista entity) {
        return RenderContext.<Ballista>builder()
                .model(new ModelConfig<Ballista>()
                               .scale(0.5f)
                               .translateOffset(-0.5f, -0.25f, -0.5f)
                               .rotationOffset(180, 0, 0))
                .build();
    }

    @Override
    protected void render(Ballista entity, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<Ballista> context, float partialTick) {
        ModelRenderer.renderModel(ServantryModelRegister.BALLISTA, poseStack, bufferSource);
    }
}
