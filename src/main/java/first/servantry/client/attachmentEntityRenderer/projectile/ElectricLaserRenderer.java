package first.servantry.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.client.dynamicLight.DynamicLightDispatcher;
import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.api.client.render.renderer.LaserRendererHelper;
import first.servantry.api.entity.PathNode;
import first.servantry.common.projectile.ElectricLaser;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.AABB;

/**
 * 电能激光渲染器 - 蓝色激光束。
 */
public class ElectricLaserRenderer extends AbstractAttachmentEntityRenderer<ElectricLaser> {

    @Override
    protected RenderContext<ElectricLaser> createContext(ElectricLaser laser) {
        return RenderContext.<ElectricLaser>builder()
                .build();
    }

    @Override
    protected void render(ElectricLaser entity, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<ElectricLaser> context, float partialTick) {
        AABB hitbox = entity.getHitbox();
        float length = (float) hitbox.getZsize();
        float width = (float) hitbox.getXsize() * 0.5f;
        LaserRendererHelper.builder()
                .length(length)
                .radius(width, width)
                .layers(4)
                .segments(4)
                .color(0x4488FF)
                .alpha(0.95f)
                .innerRatio(0.15f)
                .render(poseStack, bufferSource);
        DynamicLightDispatcher.addLightSources(visualNode, hitbox, 8);
    }
}
