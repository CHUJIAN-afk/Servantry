package first.servantry.client.attachmentEntityRenderer.servant;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.ModelRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.entity.PathNode;
import first.servantry.common.servant.Hornet;
import first.servantry.register.ServantryModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * 黄蜂渲染器 - 暂用星细胞模型占位。
 */
public class HornetRenderer extends AbstractAttachmentEntityRenderer<Hornet> {

    @Override
    protected RenderContext<Hornet> createContext(Hornet entity) {
        return RenderContext.<Hornet>builder()
                .model(new ModelConfig<Hornet>()
                        .scale(0.5f)
                        .translateOffset(-0.5f, -0.5f, -0.5f)
                        .alphaDistanceFactor(1.5f)
                )
                .build();
    }

    @Override
    protected void render(Hornet entity, PoseStack poseStack, MultiBufferSource bufferSource,
                          PathNode visualNode, RenderContext<Hornet> context, float partialTick) {
        ModelRenderer.renderModel(ServantryModelRegister.STARDUST_CELL, poseStack, bufferSource);
    }
}
