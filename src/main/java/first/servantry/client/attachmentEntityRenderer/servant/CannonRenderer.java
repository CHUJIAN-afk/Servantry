package first.servantry.client.attachmentEntityRenderer.servant;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.ModelRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.api.client.render.renderConfig.ModelConfig;
import first.servantry.api.entity.PathNode;
import first.servantry.common.sentryServant.Cannon;
import first.servantry.register.ServantryModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * 玉米加农炮渲染器 - 占位模型。
 */
public class CannonRenderer extends AbstractAttachmentEntityRenderer<Cannon> {

    @Override
    protected RenderContext<Cannon> createContext(Cannon entity) {
        return RenderContext.<Cannon>builder()
                .model(new ModelConfig<Cannon>()
                        .scale(0.5f)
                        .translateOffset(-0.5f, -0.25f, -0.5f)
                        .rotationOffset(180, 0, 0)
                )
                .build();
    }

    @Override
    protected void render(Cannon entity, PoseStack poseStack, MultiBufferSource bufferSource,
                          PathNode visualNode, RenderContext<Cannon> context, float partialTick) {
        ModelRenderer.renderModel(ServantryModelRegister.TEST, poseStack, bufferSource);
    }
}
