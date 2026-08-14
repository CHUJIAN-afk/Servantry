package first.summoner.client.attachmentEntityRenderer.servant;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.entity.PathNode;
import first.summoner.common.sentryServant.Cannon;
import first.summoner.utils.RenderUtil;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * 玉米加农炮渲染器 - 占位模型。
 */
public class CannonRenderer extends AbstractAttachmentEntityRenderer<Cannon> {

    @Override
    protected RenderContext<Cannon> createContext(Cannon entity) {
        return RenderContext.<Cannon>builder()
                .model(new ModelConfig<Cannon>()
                        .scale(2f)
                        .translateOffset(0f, -0.5f, -0.5f)
                        .rotationOffset(180, 0, 0)
                )
                .build();
    }

    @Override
    protected void render(Cannon cannon, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<Cannon> context, float partialTick) {
        RenderUtil.renderDebugLabel(poseStack, bufferSource, cannon);
    }
}
