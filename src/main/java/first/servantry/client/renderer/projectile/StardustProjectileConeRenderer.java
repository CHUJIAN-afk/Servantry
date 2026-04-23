package first.servantry.client.renderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.PathNode;
import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.common.projectile.StardustProjectile;
import first.servantry.register.ItemRegister;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;

/**
 * 星细胞射弹渲染器。
 * <p>
 * 使用圆锥拖尾，渲染缩小版星尘细胞模型（0.25倍）。
 * </p>
 */
public class StardustProjectileConeRenderer extends AbstractAttachmentEntityRenderer<StardustProjectile> {

    @Override
    protected RenderContext<StardustProjectile> createContext(StardustProjectile projectile) {
        return RenderContext.<StardustProjectile>cone(projectile.getTrailTimer(), 0x8AE0FF, 0.15f)
                .trailHistoryLength(6)
                .trailSegmentsPerNode(3)
                .trailResolution(8)
                .trailFadeOut(progress -> (float) Math.pow(Math.max(0.0f, 1.0f - progress), 2.0))
                .modelScale(0.25f);
    }

    @Override
    protected void renderEntity(StardustProjectile projectile, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<StardustProjectile> config) {
        Minecraft.getInstance().getItemRenderer().renderStatic(
                ItemRegister.StardustCell.get().getDefaultInstance(),
                ItemDisplayContext.FIXED,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                bufferSource,
                Minecraft.getInstance().level,
                0
        );
    }

}