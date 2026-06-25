package first.servantry.client.attachmentEntityRenderer.servant;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.ModelRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.api.client.render.renderConfig.ModelConfig;
import first.servantry.api.entity.PathNode;
import first.servantry.common.servant.Sharknado;
import first.servantry.register.ModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;

public class SharknadoRenderer extends AbstractAttachmentEntityRenderer<Sharknado> {

    @Override
    protected RenderContext<Sharknado> createContext(Sharknado entity) {
        return RenderContext.<Sharknado>builder()
                .model(new ModelConfig<Sharknado>()
                        .translateOffset(-0.5f, -0.5f, -0.5f)
                        .alphaDistanceFactor(1.5f)
                )
                .build();
    }

    @Override
    protected void renderEntityModel(Sharknado entity, PoseStack poseStack, MultiBufferSource bufferSource, PathNode node, RenderContext<Sharknado> config) {
        super.renderEntityModel(entity, poseStack, bufferSource, node, config);
    }

    @Override
    protected void renderEntity(Sharknado entity, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<Sharknado> config) {
        ModelRenderer.renderModel(ModelRegister.SHARKNADO, poseStack, bufferSource);
    }
}
