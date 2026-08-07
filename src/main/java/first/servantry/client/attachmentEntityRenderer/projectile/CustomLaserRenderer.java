package first.servantry.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.dynamicLight.DynamicLightDispatcher;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.rendererHelper.LaserRendererHelper;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.entity.PathNode;
import first.servantry.common.projectile.CustomLaser;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.AABB;

/**
 * 自定义激光投射物渲染器。
 * <p>
 * 使用 {@link LaserRendererHelper} 多层同心壳 + 自定义着色器实现发光、流动、体积雾效果。
 * 替代旧的四叉面伪圆柱实现。
 * </p>
 */
public class CustomLaserRenderer extends AbstractAttachmentEntityRenderer<CustomLaser> {

    @Override
    protected RenderContext<CustomLaser> createContext(CustomLaser laser) {
        return RenderContext.<CustomLaser>builder()
                .model(new ModelConfig<CustomLaser>()
                               .alphaDistanceFactor(0)
                               .rotationOffset(180, 0, 0))
                .build();
    }

    @Override
    protected void render(CustomLaser entity, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<CustomLaser> context, float partialTick) {
        int color = entity.getColor();
        AABB hitbox = entity.getHitbox();
        float length = (float) hitbox.getZsize();
        float width = (float) hitbox.getXsize() * 0.5f;
        LaserRendererHelper.builder()
                .length(length)
                .radius(width * 0.25F, width)
                .layers(4)
                .segments(16)
                .color(color)
                .alpha(entity.getAlpha())
                .innerRatio(0.35f)
                .render(poseStack, bufferSource);
        DynamicLightDispatcher.addLightSources(visualNode, hitbox, 8);
    }
}
