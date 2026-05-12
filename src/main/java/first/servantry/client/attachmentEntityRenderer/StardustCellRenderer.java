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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

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
                        .timer(servant.trailTimer)
                        .colorRGB(0x8AE0FF)
                        .maxRadius(0.2f)
                        .resolution(12)
                        .fadeOut(progress -> (float) Math.pow(Math.max(0.0f, 1.0f - progress), 2.0)))
                .model(new ModelConfig<StardustCell>()
                        .scale(0.5f)
                        .translateOffset(-0.5f, -0.5f, -0.5f)
                        .alphaDistanceFactor(1.5f)
                        .visualNodeFunction((cell, partialTick, rawNode) -> new PathNode(rawNode.pos(), cell.getRenderYaw(partialTick), cell.getRenderPitch(partialTick), cell.getRenderRoll(partialTick))))
                .build();
    }

    @Override
    protected void renderEntity(StardustCell servant, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<StardustCell> config) {
        if (config.hasTrail()) {
            Player owner = servant.getOwner();
            Vec3 pos = visualNode.pos().offsetRandom(owner.getRandom(), 0.25f);
            ParticleHelper.create(owner.level())
                    .generic(builder -> builder
                            .color(51, 204, 255)
                            .colorRandom(0.2F, 0.2F, 0.0F)
                            .lifetime(5)
                            .lifetimeRandom(10)
                            .friction(0.75F)
                            .spinRandom(0.5F)
                    )
                    .pos(pos)
                    .velocity(Vec3.ZERO)
                    .spread(0)
                    .speed(0)
                    .emit();
        }
        ModelRenderer.renderModel(ModelRegister.STARDUST_CELL, poseStack, bufferSource, Sheets.translucentItemSheet());
    }

}
