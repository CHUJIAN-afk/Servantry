package first.servantry.client.attachmentEntityRenderer;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.PathNode;
import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.common.projectile.LaserProjectile;
import net.minecraft.client.renderer.MultiBufferSource;

public class LaserProjectileRenderer extends AbstractAttachmentEntityRenderer<LaserProjectile> {

    @Override
    protected RenderContext<LaserProjectile> createContext(LaserProjectile laser) {
        return RenderContext.<LaserProjectile>droplet(laser.getTrailDuration(), 0xFF3333, 0.04f)
                .trailHistoryLength(6)
                .trailResolution(16)
                //.modelTranslateOffset(-0.47f, -0.03f, 0f)
                //.modelRotationOffset(180, 0, 0)
                .trailSegmentsPerNode(2);
    }

    @Override
    protected void renderEntity(LaserProjectile entity, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<LaserProjectile> config) {
        //ModelRenderer.renderModel(ModelRegister.LaserProjectile, poseStack, bufferSource);
    }
}
