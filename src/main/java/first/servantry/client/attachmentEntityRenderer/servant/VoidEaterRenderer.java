package first.servantry.client.attachmentEntityRenderer.servant;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.ModelRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.api.client.render.renderConfig.ModelConfig;
import first.servantry.api.entity.PathNode;
import first.servantry.common.servant.VoidEater;
import first.servantry.register.ModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.ModelResourceLocation;

public class VoidEaterRenderer extends AbstractAttachmentEntityRenderer<VoidEater> {

    @Override
    protected RenderContext<VoidEater> createContext(VoidEater dragon) {
        int total = dragon.getTotalSegments();
        int index = dragon.getSegmentIndex();
        boolean isTail = index == total - 1;
        return RenderContext.<VoidEater>builder()
                .model(new ModelConfig<VoidEater>()
                               .translateOffset(-0.5f, isTail ? -0.4845f : -0.425f, isTail ? -0.103075f : -0.5f)
                               .rotationOffset(180, 0, 0))
                .build();
    }

    @Override
    protected void renderEntity(VoidEater dragon, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<VoidEater> config) {
        int total = dragon.getTotalSegments();
        int index = dragon.getSegmentIndex();
        ModelResourceLocation model;
        if (index == 0) {
            model = ModelRegister.STARDUST_DRAGON_HEAD;
        } else if (index == total - 1) {
            model = ModelRegister.STARDUST_DRAGON_BODY3;
        } else if (index % 2 == 0) {
            model = ModelRegister.STARDUST_DRAGON_BODY1;
        } else {
            model = ModelRegister.STARDUST_DRAGON_BODY2;
        }
        ModelRenderer.renderModel(model, poseStack, bufferSource);
    }
}
