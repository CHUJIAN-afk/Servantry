package first.servantry.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.api.client.render.renderer.LaserRenderer;
import first.servantry.api.entity.PathNode;
import first.servantry.common.projectile.Rain;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.AABB;

public class RainRenderer extends AbstractAttachmentEntityRenderer<Rain> {

    @Override
    protected RenderContext<Rain> createContext(Rain laser) {
        return RenderContext.<Rain>builder()
                .build();
    }

    @Override
    protected void renderEntity(Rain entity, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<Rain> config) {
        AABB hitbox = entity.getHitbox();
        float length = (float) hitbox.getZsize();
        float width = (float) hitbox.getXsize() * 0.25f;
        LaserRenderer.builder()
                .length(length)
                .radius(width, width)
                .layers(2)
                .segments(4)
                .color(0x01adff)
                .alpha(0.5f)
                .innerRatio(0.4f)
                .render(poseStack, bufferSource);
    }
}
