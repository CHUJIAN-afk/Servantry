package first.summoner.client.attachmentEntityRenderer.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.RenderUtil;
import first.lyra.client.render.trail.ConeTrailConfig;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.entity.PathNode;
import first.summoner.common.minion.Twins;
import first.summoner.register.SummonerModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FastColor;

public class TwinsRenderer extends AbstractAttachmentEntityRenderer<Twins> {

    @Override
    protected RenderContext<Twins> createContext(Twins twins, float partialTick) {
        int trailTimer = twins.getTrailTimer();
        return RenderContext.<Twins>builder()
                .trail(new ConeTrailConfig<Twins>()
                        .timer(trailTimer)
                        .colorRGB(0xce4949)
                        .historyLength(trailTimer / 2)
                        .maxRadius(0.2f)
                        .minRadiusRatio(0.75f)
                        .resolution(4)
                        .fadeOut(progress -> (1 - progress) * (1 - progress))
                )
                .model(new ModelConfig<Twins>()
                        .scale(0.5f)
                        .translateOffset(-0.5f, -0.25f, -0.5f)
                        .rotationOffset(180, 0, 0)
                )
                .build();
    }

    @Override
    protected void render(Twins minion, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<Twins> context, float partialTick, float alpha) {
        if (minion.isLaserEye()) {
            RenderUtil.renderStandalone(SummonerModelRegister.TWINS_LASER, poseStack, bufferSource, FastColor.ARGB32.color((int) (alpha * 255), 255, 255, 255), RenderUtil.FULL_LIGHT);
        } else {
            RenderUtil.renderStandalone(SummonerModelRegister.TWINS_CURSED_FLAME, poseStack, bufferSource, FastColor.ARGB32.color((int) (alpha * 255), 255, 255, 255), RenderUtil.FULL_LIGHT);
        }
    }
}
