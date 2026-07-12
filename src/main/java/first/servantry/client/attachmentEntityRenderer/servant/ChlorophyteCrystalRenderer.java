package first.servantry.client.attachmentEntityRenderer.servant;


import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.ModelRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.api.client.render.renderConfig.ModelConfig;
import first.servantry.api.entity.PathNode;
import first.servantry.common.servant.ChlorophyteCrystal;
import first.servantry.register.ServantryModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;

public class ChlorophyteCrystalRenderer extends AbstractAttachmentEntityRenderer<ChlorophyteCrystal> {

    @Override
    protected RenderContext<ChlorophyteCrystal> createContext(ChlorophyteCrystal servant) {
        return RenderContext.<ChlorophyteCrystal>builder()
                .model(new ModelConfig<ChlorophyteCrystal>()
                        .scale(0.75f)
                        .translateOffset(-0.5f, -0.5f, -0.5f)
                )
                .build();
    }

    @Override
    protected void renderEntity(ChlorophyteCrystal servant, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<ChlorophyteCrystal> config) {
        ModelRenderer.renderModel(ServantryModelRegister.CHLOROPHYTE_CRYSTAL, poseStack, bufferSource);
    }
}
