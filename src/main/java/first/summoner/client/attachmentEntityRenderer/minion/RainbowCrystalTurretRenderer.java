package first.summoner.client.attachmentEntityRenderer.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.RenderUtil;
import first.lyra.client.render.model.LyraModelRenderer;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.common.minion.sentry.RainbowCrystal;
import first.summoner.register.SummonerModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FastColor;

/**
 * 彩虹水晶哨兵渲染器。
 */
public class RainbowCrystalTurretRenderer extends AbstractAttachmentEntityRenderer<RainbowCrystal> {

    @Override
    protected RenderContext<RainbowCrystal> createContext(RainbowCrystal crystal, float partialTick) {
        return RenderContext.<RainbowCrystal>builder()
                .model(new ModelConfig<RainbowCrystal>()
                               .scale(0.7f)
                               .translateOffset(-0.5f, -0.5f, -0.5f)
                               .alphaDistanceFactor(1.5f))
                .build();
    }

    @Override
    protected void render(RainbowCrystal crystal, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<RainbowCrystal> context, float partialTick, int packedLight, float alpha) {
        LyraModelRenderer.json(SummonerModelRegister.RAINBOW_CRYSTAL)
                .color(FastColor.ARGB32.color((int) (alpha * 255), 255, 255, 255))
                .light(RenderUtil.FULL_LIGHT)
                .render(poseStack, bufferSource);
    }
}
