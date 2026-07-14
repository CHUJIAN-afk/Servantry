package first.servantry.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.api.client.render.renderer.LaserRenderer;
import first.servantry.api.entity.PathNode;
import first.servantry.common.projectile.DestructionBullet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.AABB;

public class DestructionBulletRenderer extends AbstractAttachmentEntityRenderer<DestructionBullet> {

    @Override
    protected RenderContext<DestructionBullet> createContext(DestructionBullet laser) {
        return RenderContext.<DestructionBullet>builder()
                .build();
    }

    @Override
    protected void render(DestructionBullet entity, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<DestructionBullet> context) {
        AABB hitbox = entity.getHitbox();
        float length = (float) hitbox.getZsize();
        float width = (float) hitbox.getXsize() * 0.5f;
        LaserRenderer.builder()
                .length(length)
                .radius(width, width)
                .layers(2)
                .segments(4)
                .color(0xff3d00)
                .alpha(0.5f)
                .innerRatio(0.4f)
                .render(poseStack, bufferSource);
    }
}
