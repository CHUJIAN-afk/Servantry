package first.servantry.client.attachmentEntityRenderer.servant;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.PathNode;
import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.ModelRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.api.client.render.renderConfig.ModelConfig;
import first.servantry.api.client.renderType.ShaderDetector;
import first.servantry.common.servant.ScavengerFairy;
import first.servantry.register.ModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.resources.ResourceLocation;

public class ScavengerFairyRenderer extends AbstractAttachmentEntityRenderer<ScavengerFairy> {

    @Override
    protected RenderContext<ScavengerFairy> createContext(ScavengerFairy servant) {
        return RenderContext.<ScavengerFairy>builder()
                .model(new ModelConfig<ScavengerFairy>()
                        .scale(0.28f)
                        .translateOffset(-0.5f, -0.5f, -0.5f)
                        .alphaDistanceFactor(1.4f)
                )
                .build();
    }

    @Override
    protected void renderEntity(ScavengerFairy servant, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<ScavengerFairy> config) {
        RenderType renderType = ShaderDetector.isShaderEnabled()
                ? RenderType.entityTranslucentEmissive(ResourceLocation.parse("servantry:textures/item/stardust_cell.png"))
                : Sheets.translucentItemSheet();
        ModelRenderer.renderModel(ModelRegister.STARDUST_CELL, poseStack, bufferSource, renderType);
    }
}
