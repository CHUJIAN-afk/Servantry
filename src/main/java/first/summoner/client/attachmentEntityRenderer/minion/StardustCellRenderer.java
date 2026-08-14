package first.summoner.client.attachmentEntityRenderer.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.dynamicLight.DynamicLightDispatcher;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.ModelRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.trail.ConeTrailConfig;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.entity.PathNode;
import first.lyra.common.particle.genericParticle.GenericParticleBuilder;
import first.summoner.common.minion.StardustCell;
import first.summoner.register.SummonerModelRegister;
import first.summoner.utils.ParticleHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * 星尘细胞渲染器。
 * <p>
 * 使用圆锥拖尾，渲染旋转的星尘细胞模型。
 * </p>
 */
public class StardustCellRenderer extends AbstractAttachmentEntityRenderer<StardustCell> {

    @Override
    protected RenderContext<StardustCell> createContext(StardustCell minion) {
        return RenderContext.<StardustCell>builder()
                .trail(new ConeTrailConfig<StardustCell>()
                        .timer(minion.getTrailTimer())
                        .colorRGB(0x8AE0FF)
                        .maxRadius(0.2f)
                        .resolution(12)
                        .fadeOut(progress -> (float) Math.pow(Math.max(0.0f, 1.0f - progress), 2.0))
                )
                .model(new ModelConfig<StardustCell>()
                        .scale(0.5f)
                        .translateOffset(-0.5f, -0.5f, -0.5f)
                        .alphaDistanceFactor(1.5f)
                )
                .build();
    }

    @Override
    protected void render(StardustCell minion, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<StardustCell> context, float partialTick) {
        if (context.hasTrail() && !Minecraft.getInstance().isPaused()) {
            int trailTimer = minion.getTrailTimer();
            ParticleHelper.create(minion.getOwner().level())
                    .generic(GenericParticleBuilder.create()
                            .centerColor(0x2fb2e1)
                            .edgeColor(0x33ccff)
                            .lifetime(7 - trailTimer)
                            .lifetimeRandom(4)
                            .spin(trailTimer * 0.25f)
                            .spinRandom(0.5F)
                            .friction(0.75F)
                            .scale(0.035f)
                            .scaleRandom(0.005f)
                    )
                    .pos(visualNode.pos())
                    .offset(0.25)
                    .emit();
        }
        ModelRenderer.renderModel(SummonerModelRegister.STARDUST_CELL, poseStack, bufferSource);
        DynamicLightDispatcher.addLightSources(visualNode.pos(), 8);
    }

}
