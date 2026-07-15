package first.servantry.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.client.dynamicLight.DynamicLightDispatcher;
import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.ModelRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.api.client.render.renderConfig.ConeTrailConfig;
import first.servantry.api.client.render.renderConfig.ModelConfig;
import first.servantry.api.entity.PathNode;
import first.servantry.common.projectile.MiniStardustCell;
import first.servantry.register.ServantryModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * 星细胞射弹渲染器。
 * <p>
 * 使用圆锥拖尾，渲染缩小版星尘细胞模型（0.25倍）。
 * </p>
 */
public class MiniStardustProjectileRenderer extends AbstractAttachmentEntityRenderer<MiniStardustCell> {

    @Override
    protected RenderContext<MiniStardustCell> createContext(MiniStardustCell projectile) {
        return RenderContext.<MiniStardustCell>builder()
                .trail(new ConeTrailConfig<MiniStardustCell>()
                        .timer(projectile.getTrailTimer())
                        .colorRGB(0x8AE0FF)
                        .historyLength(5)
                        .segmentsPerNode(16)
                        .maxRadius(0.075f)
                        .resolution(4)
                        .fadeOut(progress -> (float) Math.pow(Math.max(0.0f, 1.0f - progress), 2.0)))
                .model(new ModelConfig<MiniStardustCell>()
                        .scale(0.2f)
                        .translateOffset(-0.5f, -0.5f, -0.5f)
                        .rotationOffset(0, 0, 45))
                .build();
    }

    @Override
    protected void render(MiniStardustCell projectile, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<MiniStardustCell> context, float partialTick) {
        ModelRenderer.renderModel(ServantryModelRegister.STARDUST_CELL, poseStack, bufferSource);
        DynamicLightDispatcher.addLightSources(visualNode.pos(), 8);
    }

}
