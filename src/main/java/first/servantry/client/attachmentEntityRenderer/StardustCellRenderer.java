package first.servantry.client.attachmentEntityRenderer;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.PathNode;
import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.ModelRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.api.client.render.renderConfig.ConeTrailConfig;
import first.servantry.api.client.render.renderConfig.ModelConfig;
import first.servantry.common.servant.StardustCell;
import first.servantry.register.ModelRegister;
import first.servantry.utils.ParticleHelper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;

/**
 * 星尘细胞渲染器。
 * <p>
 * 使用圆锥拖尾，渲染旋转的星尘细胞模型。
 * </p>
 */
public class StardustCellRenderer extends AbstractAttachmentEntityRenderer<StardustCell> {

    @Override
    protected RenderContext<StardustCell> createContext(StardustCell servant) {
        return RenderContext.<StardustCell>builder()
                .trail(new ConeTrailConfig<StardustCell>()
                        .timer(servant.getTrailTimer())
                        .colorRGB(0x8AE0FF)
                        .maxRadius(0.2f)
                        .resolution(12)
                        .fadeOut(progress -> (float) Math.pow(Math.max(0.0f, 1.0f - progress), 2.0)))
                .model(new ModelConfig<StardustCell>()
                        .scale(0.5f)
                        .translateOffset(-0.5f, -0.5f, -0.5f)
                        .alphaDistanceFactor(1.5f)
                )
                .build();
    }

    @Override
    protected void renderEntity(StardustCell servant, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<StardustCell> config) {
        if (config.hasTrail()) {
            int trailTimer = servant.getTrailTimer();
            ParticleHelper.create(servant.getOwner().level())
                    .generic(builder -> builder
                            .color(51, 204, 255)
                            .colorRandom(0.2F, 0.2F, 0.0F)
                            .lifetime(7 - trailTimer)
                            .lifetimeRandom(4)
                            .spin(trailTimer * 0.25f)
                            .spinRandom(0.5F)
                    )
                    .pos(visualNode.pos())
                    .offset(0.25)
                    .emit();
        }
        ModelRenderer.renderModel(ModelRegister.STARDUST_CELL, poseStack, bufferSource, Sheets.translucentItemSheet());
    }

}
