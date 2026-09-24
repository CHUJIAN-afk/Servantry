package first.summoner.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.dynamicLight.DynamicLightDispatcher;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.rendererHelper.LaserRendererHelper;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.common.projectile.EyeLaser;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.AABB;

/**
 * 眼球激光塔渲染器 - 淡红色激光束。
 */
public class EyeLaserRenderer extends AbstractAttachmentEntityRenderer<EyeLaser> {

    private static final int COLOR = 0xFF6B6B;

    @Override
    protected RenderContext<EyeLaser> createContext(EyeLaser laser, float partialTick) {
        return RenderContext.<EyeLaser>builder()
                .model(new ModelConfig<EyeLaser>()
                               .alphaDistanceFactor(0)
                               .rotationOffset(180, 0, 0))
                .build();
    }

    @Override
    protected void render(EyeLaser entity, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<EyeLaser> context, float partialTick, int packedLight, float alpha) {
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
