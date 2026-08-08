package first.servantry.client.attachmentEntityRenderer.servant;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.common.entity.AttachmentEntity;
import first.lyra.common.entity.PathNode;
import first.servantry.utils.RenderUtil;
import net.minecraft.client.renderer.MultiBufferSource;

public class TextOnlyRenderer<T extends AttachmentEntity> extends AbstractAttachmentEntityRenderer<T> {

    @Override
    protected RenderContext<T> createContext(T entity) {
        return RenderContext.<T>builder()
                .build();
    }

    @Override
    protected void render(T entity, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<T> context, float partialTick) {
        RenderUtil.renderDebugLabel(poseStack, bufferSource, entity);
    }
}
