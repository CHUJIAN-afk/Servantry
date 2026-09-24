package first.summoner.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.dynamicLight.DynamicLightDispatcher;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.rendererHelper.LaserRendererHelper;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.common.projectile.MoonPortalLaser;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.AABB;

/**
 * 月亮传送门激光渲染器 - 青绿色激光束。
 */
public class MoonPortalLaserRenderer extends AbstractAttachmentEntityRenderer<MoonPortalLaser> {

    private static final int COLOR = 0x00fdd6;

    @Override
    protected RenderContext<MoonPortalLaser> createContext(MoonPortalLaser laser, float partialTick) {
        return RenderContext.<MoonPortalLaser>builder()
                .model(new ModelConfig<MoonPortalLaser>()
                               .alphaDistanceFactor(0)
                               .rotationOffset(180, 0, 0))
                .build();
    }

    @Override
    protected void render(MoonPortalLaser entity, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<MoonPortalLaser> context, float partialTick, int packedLight, float alpha) {
        AABB hitbox = entity.getHitbox();
        float length = (float) hitbox.getZsize();
        float width = (float) hitbox.getXsize() * 0.5f;
        LaserRendererHelper.builder()
                .length(length)
                .radius(width * 0.25F, width)
                .layers(4)
                .segments(16)
                .color(COLOR)
                .alpha(0.95f)
                .innerRatio(0.35f)
                .render(poseStack, bufferSource);
        DynamicLightDispatcher.addLightSources(visualNode, hitbox, 8);
    }
}
