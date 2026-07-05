package first.servantry.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.api.client.render.laser.LaserRenderer;
import first.servantry.api.entity.PathNode;
import first.servantry.common.projectile.LaserProjectile;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.AABB;

public class LaserProjectileRenderer extends AbstractAttachmentEntityRenderer<LaserProjectile> {

    @Override
    protected RenderContext<LaserProjectile> createContext(LaserProjectile laser) {
        return RenderContext.<LaserProjectile>builder()
                .build();
    }

    @Override
    protected void renderEntity(LaserProjectile entity, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<LaserProjectile> config) {
        AABB hitbox = entity.getHitbox();
        float length = (float) hitbox.getZsize();
        float width = (float) hitbox.getXsize() * 0.5f;
        LaserRenderer.builder()
                .length(length)
                .radius(width, width)
                .layers(4)
                .segments(4)
                .color(0xFF3333)
                .alpha(0.95f)
                .innerRatio(0.15f)
                .render(poseStack, bufferSource);
    }
}
