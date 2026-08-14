package first.summoner.client.attachmentEntityRenderer.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.entity.PathNode;
import first.summoner.common.sentry.Ballista;
import first.summoner.utils.RenderUtil;
import net.minecraft.client.renderer.MultiBufferSource;

public class BallistaRenderer extends AbstractAttachmentEntityRenderer<Ballista> {

    @Override
    protected RenderContext<Ballista> createContext(Ballista entity) {
        return RenderContext.<Ballista>builder()
                .model(new ModelConfig<Ballista>()
                               .scale(0.5f)
                               .translateOffset(-0.5f, -0.25f, -0.5f)
                               .rotationOffset(180, 0, 0))
                .build();
    }

    @Override
    protected void render(Ballista entity, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<Ballista> context, float partialTick) {
        RenderUtil.renderDebugLabel(poseStack, bufferSource, entity);
    }
}
