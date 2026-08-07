package first.servantry.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.ModelRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.entity.PathNode;
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
