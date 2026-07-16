package first.servantry.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.ModelRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.api.client.render.renderConfig.ModelConfig;
import first.servantry.api.entity.PathNode;
import first.servantry.common.projectile.HornetStinger;
import first.servantry.register.ServantryModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * 黄蜂毒刺渲染器 - 暂用星细胞模型占位。
 */
public class HornetStingerRenderer extends AbstractAttachmentEntityRenderer<HornetStinger> {

    @Override
    protected RenderContext<HornetStinger> createContext(HornetStinger entity) {
        return RenderContext.<HornetStinger>builder()
                .model(new ModelConfig<HornetStinger>()
                        .scale(0.25f)
                        .translateOffset(-0.5f, -0.5f, -0.5f)
                )
                .build();
    }

    @Override
    protected void render(HornetStinger entity, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<HornetStinger> context, float partialTick) {
        ModelRenderer.renderModel(ServantryModelRegister.STARDUST_CELL, poseStack, bufferSource);
    }
}
