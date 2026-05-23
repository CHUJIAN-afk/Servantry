package first.servantry.client.attachmentEntityRenderer.servant;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.PathNode;
import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.ModelRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.api.client.render.renderConfig.ModelConfig;
import first.servantry.api.client.renderType.ShaderDetector;
import first.servantry.common.servant.OreScout;
import first.servantry.register.ModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;

public class OreScoutServantRenderer extends AbstractAttachmentEntityRenderer<OreScout> {

    @Override
    protected RenderContext<OreScout> createContext(OreScout servant) {
        return RenderContext.<OreScout>builder()
                .model(new ModelConfig<OreScout>()
                        .scale(0.4f)
                        .translateOffset(-0.5f, -0.5f, -0.5f)
                        .alphaDistanceFactor(1.6f)
                        .visualNodeFunction((entity, partialTick, rawNode) -> entity.getInterpolatedIdleState(partialTick))
                )
                .build();
    }

    @Override
    protected void renderEntity(OreScout servant, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<OreScout> config) {
        RenderType renderType = ShaderDetector.isShaderEnabled()
                ? RenderType.entityTranslucentEmissive(net.minecraft.resources.ResourceLocation.parse("servantry:textures/item/stardust_cell.png"))
                : Sheets.translucentItemSheet();
        ModelRenderer.renderModel(ModelRegister.STARDUST_CELL, poseStack, bufferSource, renderType);
    }
}
