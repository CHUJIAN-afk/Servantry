package first.servantry.client.attachmentEntityRenderer.servant;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.PathNode;
import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.ModelRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.api.client.render.renderConfig.ModelConfig;
import first.servantry.common.servant.Sharknado;
import first.servantry.register.ModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;

public class SharknadoRenderer extends AbstractAttachmentEntityRenderer<Sharknado> {

    @Override
    protected RenderContext<Sharknado> createContext(Sharknado entity) {
        return RenderContext.<Sharknado>builder()
                .model(new ModelConfig<Sharknado>()
                        .scale(0.5f)
                        .translateOffset(-0.5f, -0.5f, -0.5f)
                        .alphaDistanceFactor(1.5f)
                )
                .build();
    }

    @Override
    protected void renderEntity(Sharknado entity, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<Sharknado> config) {
        ModelRenderer.renderModel(ModelRegister.STARDUST_DRAGON_BODY1, poseStack, bufferSource);
    }
}
