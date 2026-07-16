package first.servantry.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.client.dynamicLight.DynamicLightDispatcher;
import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.api.client.render.renderer.LaserRendererHelper;
import first.servantry.api.entity.PathNode;
import first.servantry.common.projectile.Laser;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.AABB;

public class LaserRenderer extends AbstractAttachmentEntityRenderer<Laser> {

    @Override
    protected RenderContext<Laser> createContext(Laser laser) {
        return RenderContext.<Laser>builder()
                .build();
    }

    @Override
    protected void render(Laser entity, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<Laser> context, float partialTick) {
        AABB hitbox = entity.getHitbox();
        float length = (float) hitbox.getZsize();
        float width = (float) hitbox.getXsize() * 0.5f;
        LaserRendererHelper.builder()
                .length(length)
                .radius(width, width)
                .layers(4)
                .segments(4)
                .color(0xFF3333)
                .alpha(0.95f)
                .innerRatio(0.15f)
                .render(poseStack, bufferSource);
        DynamicLightDispatcher.addLightSources(visualNode, hitbox, 8);
    }
}
