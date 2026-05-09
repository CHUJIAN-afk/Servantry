package first.servantry.client.attachmentEntityRenderer;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.PathNode;
import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.ModelRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.common.servant.Twins;
import first.servantry.register.ModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;

public class TwinsRenderer extends AbstractAttachmentEntityRenderer<Twins> {

    @Override
    protected RenderContext<Twins> createContext(Twins servant) {
        return RenderContext.<Twins>none()
                .modelTranslateOffset(-0.5f, -0.5f, -0.5f)
                .modelScale(0.5f);
    }

    @Override
    protected void renderEntity(Twins servant, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<Twins> config) {
        ModelRenderer.renderModel(ModelRegister.STARDUST_CELL, poseStack, bufferSource, Sheets.translucentItemSheet());
    }

}
