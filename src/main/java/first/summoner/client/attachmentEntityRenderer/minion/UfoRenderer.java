package first.summoner.client.attachmentEntityRenderer.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.RenderUtil;
import first.lyra.client.render.model.LyraModelRenderer;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.attachmentEntity.PathNode;
import first.lyra.common.particle.genericParticle.GenericParticleBuilder;
import first.summoner.Summoner;
import first.summoner.common.minion.minion.UFO;
import first.summoner.utils.ParticleHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FastColor;

/**
 * UFO 渲染器：渲染 geo/ufo 静态模型。
 */
public class UfoRenderer extends AbstractAttachmentEntityRenderer<UFO> {

    @Override
    protected RenderContext<UFO> createContext(UFO ufo, float partialTick) {
        return RenderContext.<UFO>builder()
                .model(new ModelConfig<UFO>()
                               .scale(0.8f)
                               .translateOffset(0, -0.125f, 0)
                               .alphaDistanceFactor(1.5f))
                .build();
    }

    @Override
    protected void render(UFO ufo, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<UFO> context, float partialTick, int packedLight, float alpha) {
        if (ufo.getTrailTimer() > 0 && !Minecraft.getInstance().isPaused()) {
            ParticleHelper.create(ufo.getLevel())
                    .generic(GenericParticleBuilder.create()
                                     .centerColor(0x46f7ff)
                                     .edgeColor(0x3dd6dd)
                                     .lifetime(3)
                                     .lifetimeRandom(2)
                                     .scale(0.015f)
                                     .scaleRandom(0.005f)
                    )
                    .pos(visualNode.pos())
                    .offset(0.2)
                    .count(10)
                    .emit();
        }
        LyraModelRenderer.geo(Summoner.rl("ufo"))
                .light(RenderUtil.FULL_LIGHT)
                .color(FastColor.ARGB32.color((int) (alpha * 255), 255, 255, 255))
                .render(poseStack, bufferSource);
    }
}
