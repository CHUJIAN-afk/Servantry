package first.summoner.client.attachmentEntityRenderer.servant;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.entity.PathNode;
import first.summoner.common.servant.Imp;
import first.summoner.utils.RenderUtil;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * 小鬼渲染器 - 暂用星细胞模型占位。
 */
public class ImpRenderer extends AbstractAttachmentEntityRenderer<Imp> {

    @Override
    protected RenderContext<Imp> createContext(Imp entity) {
        return RenderContext.<Imp>builder()
                .model(new ModelConfig<Imp>()
                        .scale(0.5f)
                        .translateOffset(-0.5f, -0.5f, -0.5f)
                )
                .build();
    }

    @Override
    protected void render(Imp entity, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<Imp> context, float partialTick) {
        RenderUtil.renderDebugLabel(poseStack, bufferSource, entity);
    }
}
