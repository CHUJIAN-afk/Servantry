package first.servantry.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.client.dynamicLight.DynamicLightDispatcher;
import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.api.entity.PathNode;
import first.servantry.common.projectile.DemonFlame;
import net.minecraft.client.renderer.MultiBufferSource;

public class DemonFlameRenderer extends AbstractAttachmentEntityRenderer<DemonFlame> {

    @Override
    protected RenderContext<DemonFlame> createContext(DemonFlame projectile) {
        return RenderContext.<DemonFlame>builder()
                .build();
    }

    @Override
    protected void render(DemonFlame entity, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<DemonFlame> context, float partialTick) {
        DynamicLightDispatcher.addLightSources(visualNode.pos(), 8);
    }
}
