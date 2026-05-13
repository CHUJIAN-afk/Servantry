package first.servantry.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.PathNode;
import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.ModelRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.api.client.render.renderConfig.ConeTrailConfig;
import first.servantry.api.client.render.renderConfig.ModelConfig;
import first.servantry.common.projectile.StardustProjectile;
import first.servantry.register.ModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;

/**
 * 星细胞射弹渲染器。
 * <p>
 * 使用圆锥拖尾，渲染缩小版星尘细胞模型（0.25倍）。
 * </p>
 */
public class StardustProjectileRenderer extends AbstractAttachmentEntityRenderer<StardustProjectile> {

    @Override
    protected RenderContext<StardustProjectile> createContext(StardustProjectile projectile) {
        return RenderContext.<StardustProjectile>builder()
                .trail(new ConeTrailConfig<StardustProjectile>()
                        .timer(projectile.getTrailTimer())
                        .colorRGB(0x8AE0FF)
                        .historyLength(5)
                        .segmentsPerNode(16)
                        .startIndex(0)
                        .maxRadius(0.105f)
                        .minRadiusRatio(0f)
                        .resolution(4)
                        .fadeOut(progress -> (float) Math.pow(Math.max(0.0f, 1.0f - progress), 2.0)))
                .model(new ModelConfig<StardustProjectile>()
                        .scale(0.2f)
                        .translateOffset(-0.5f, -0.5f, -0.5f)
                        .rotationOffset(0, 0, 45))
                .build();
    }

    @Override
    protected void renderEntity(StardustProjectile projectile, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<StardustProjectile> config) {
        ModelRenderer.renderModel(ModelRegister.STARDUST_CELL, poseStack, bufferSource, Sheets.translucentItemSheet());
    }

}
