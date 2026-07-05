package first.servantry.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.api.client.render.laser.LaserRenderer;
import first.servantry.api.client.render.renderConfig.ModelConfig;
import first.servantry.api.entity.PathNode;
import first.servantry.common.projectile.CustomLaserProjectile;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.AABB;

/**
 * 自定义激光投射物渲染器。
 * <p>
 * 使用 {@link LaserRenderer} 多层同心壳 + 自定义着色器实现发光、流动、体积雾效果。
 * 替代旧的四叉面伪圆柱实现。
 * </p>
 */
public class CustomLaserProjectileRenderer extends AbstractAttachmentEntityRenderer<CustomLaserProjectile> {

    @Override
    protected RenderContext<CustomLaserProjectile> createContext(CustomLaserProjectile laser) {
        return RenderContext.<CustomLaserProjectile>builder()
                .model(new ModelConfig<CustomLaserProjectile>()
                               .alphaDistanceFactor(0)
                               .rotationOffset(180, 0, 0))
                .build();
    }

    @Override
    protected void renderEntity(CustomLaserProjectile entity, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<CustomLaserProjectile> config) {
        int color = entity.getColor();
        AABB hitbox = entity.getHitbox();
        float length = (float) hitbox.getZsize();
        float width = (float) hitbox.getXsize() * 0.5f;
        LaserRenderer.builder()
                .length(length)
                .radius(width * 0.25F, width)
                .layers(4)
                .segments(16)
                .color(color)
                .alpha(entity.getAlpha())
                .innerRatio(0.35f)
                .render(poseStack, bufferSource);
    }
}
