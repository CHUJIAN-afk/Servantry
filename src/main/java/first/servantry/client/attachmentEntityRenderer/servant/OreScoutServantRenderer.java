package first.servantry.client.attachmentEntityRenderer.servant;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.client.dynamicLight.DynamicLightDispatcher;
import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.ModelRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.api.client.render.renderConfig.ModelConfig;
import first.servantry.api.entity.PathNode;
import first.servantry.common.servant.OreScout;
import first.servantry.register.ServantryModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;

public class OreScoutServantRenderer extends AbstractAttachmentEntityRenderer<OreScout> {

    @Override
    protected RenderContext<OreScout> createContext(OreScout servant) {
        return RenderContext.<OreScout>builder()
                .model(new ModelConfig<OreScout>()
                        .scale(0.4f)
                        .translateOffset(-0.5f, -0.5f, -0.5f)
                        .alphaDistanceFactor(1.6f)
                )
                .build();
    }

    @Override
    protected void render(OreScout servant, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<OreScout> context, float partialTick) {
        ModelRenderer.renderModel(ServantryModelRegister.STARDUST_CELL, poseStack, bufferSource);
        DynamicLightDispatcher.addLightSources(visualNode.pos(), 12);
    }
}
