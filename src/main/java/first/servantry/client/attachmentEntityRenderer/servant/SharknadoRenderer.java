package first.servantry.client.attachmentEntityRenderer.servant;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.ModelRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.entity.PathNode;
import first.servantry.common.servant.Sharknado;
import first.servantry.register.ServantryModelRegister;
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
    protected void render(Sharknado entity, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<Sharknado> context, float partialTick) {
        ModelRenderer.renderModel(ServantryModelRegister.SHARKNADO, poseStack, bufferSource);
    }
}
