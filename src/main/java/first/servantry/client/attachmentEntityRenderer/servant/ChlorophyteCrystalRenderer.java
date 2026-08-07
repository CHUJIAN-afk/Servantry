package first.servantry.client.attachmentEntityRenderer.servant;


import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.dynamicLight.DynamicLightDispatcher;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.ModelRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.entity.PathNode;
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
    protected void render(ChlorophyteCrystal servant, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<ChlorophyteCrystal> context, float partialTick) {
        ModelRenderer.renderModel(ServantryModelRegister.CHLOROPHYTE_CRYSTAL, poseStack, bufferSource);
        DynamicLightDispatcher.addLightSources(visualNode.pos(), 8);
    }
}
