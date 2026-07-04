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
                /*
                .trail(new DropletTrailConfig<LaserProjectile>()
                        .timer(laser.getTrailDuration())
                        .colorRGB(0xFF3333)
                        .historyLength(4)
                        .segmentsPerNode(2)
                        .maxRadius(0.04f)
                        .minRadiusRatio(0.5f)
                        .resolution(16))
                */
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
