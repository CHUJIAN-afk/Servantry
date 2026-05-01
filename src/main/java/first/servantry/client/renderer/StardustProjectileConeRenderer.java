package first.servantry.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.PathNode;
import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.ModelRenderer;
import first.servantry.api.client.render.RenderContext;
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
public class StardustProjectileConeRenderer extends AbstractAttachmentEntityRenderer<StardustProjectile> {

    @Override
    protected RenderContext<StardustProjectile> createContext(StardustProjectile projectile) {
        return RenderContext.<StardustProjectile>cone(projectile.getTrailTimer(), 0x8AE0FF, 0.125f)
                .trailHistoryLength(5)
                .trailMaxRadius(0.105f)
                .trailSegmentsPerNode(16)
                .trailResolution(4)
                .trailFadeOut(progress -> (float) Math.pow(Math.max(0.0f, 1.0f - progress), 2.0))
                .modelTranslateOffset(-0.1f, -0.1f, -0.1f)
                .modelRotationOffset(0, 0, 45)
                .modelScale(0.2f);
    }

    @Override
    protected void renderEntity(StardustProjectile projectile, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<StardustProjectile> config) {
        ModelRenderer.renderModel(ModelRegister.STARDUST_CELL, poseStack, bufferSource, Sheets.translucentItemSheet());
    }

}