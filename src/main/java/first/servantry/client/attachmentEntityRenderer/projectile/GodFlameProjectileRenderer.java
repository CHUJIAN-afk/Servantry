package first.servantry.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.api.client.render.laser.LaserRenderer;
import first.servantry.api.entity.PathNode;
import first.servantry.common.projectile.GodFlameProjectile;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.AABB;

public class GodFlameProjectileRenderer extends AbstractAttachmentEntityRenderer<GodFlameProjectile> {

    @Override
    protected RenderContext<GodFlameProjectile> createContext(GodFlameProjectile projectile) {
        return RenderContext.<GodFlameProjectile>builder()
                .build();
    }

    @Override
    protected void renderEntity(GodFlameProjectile entity, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<GodFlameProjectile> config) {
        AABB hitbox = entity.getHitbox();
        float length = (float) hitbox.getZsize();
        float width = (float) hitbox.getXsize() * 0.5f;
        LaserRenderer.builder()
                .length(length)
                .radius(width, width)
                .layers(4)
                .segments(4)
                .color(0x6f19d4)
                .alpha(0.95f)
                .innerRatio(0.15f)
                .render(poseStack, bufferSource);
    }
}
