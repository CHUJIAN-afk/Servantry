package first.servantry.client.renderer.servant;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.PathNode;
import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.ModelRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.common.servent.StardustCell;
import first.servantry.register.ModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;

/**
 * 星尘细胞渲染器。
 * <p>
 * 使用圆锥拖尾，渲染旋转的星尘细胞模型。
 * </p>
 */
public class StardustCellRendererServant extends AbstractAttachmentEntityRenderer<StardustCell> {

    @Override
    protected RenderContext<StardustCell> createContext(StardustCell servant) {
        return RenderContext.<StardustCell>cone(servant.trailTimer, 0x8AE0FF, 0.2f)
                .trailResolution(12)
                .trailFadeOut(progress -> (float) Math.pow(Math.max(0.0f, 1.0f - progress), 2.0))
                .trailShaderType(RenderContext.ShaderType.UNLIT)
                .modelTranslateOffset(-0.25f, -0.25f, -0.25f)
                .modelScale(0.5f)
                .visualNodeFunction((cell, partialTick, rawNode) -> {
                    float y = cell.getRenderYaw(partialTick);
                    float p = cell.getRenderPitch(partialTick);
                    float r = cell.getRenderRoll(partialTick);
                    return new PathNode(rawNode.pos(), y, p, r);
                });
    }

    @Override
    protected void renderEntity(StardustCell servant, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<StardustCell> config) {
        ModelRenderer.renderModel(ModelRegister.STARDUST_CELL, poseStack, bufferSource, Sheets.translucentItemSheet());
    }

}