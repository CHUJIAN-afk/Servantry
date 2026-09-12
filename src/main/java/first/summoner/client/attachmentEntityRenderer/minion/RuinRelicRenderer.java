package first.summoner.client.attachmentEntityRenderer.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.RenderUtil;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.entity.PathNode;
import first.summoner.common.minion.minion.RuinRelic;
import first.summoner.register.SummonerModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FastColor;

/**
 * 禁戒遗迹仆从渲染器。
 */
public class RuinRelicRenderer extends AbstractAttachmentEntityRenderer<RuinRelic> {

    @Override
    protected RenderContext<RuinRelic> createContext(RuinRelic relic, float partialTick) {
        return RenderContext.<RuinRelic>builder()
                .model(new ModelConfig<RuinRelic>()
                               .translateOffset(-0.5f, -0.5f, -0.5f))
                .build();
    }

    @Override
    protected void render(RuinRelic relic, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<RuinRelic> context, float partialTick, int packedLight, float alpha) {
        RenderUtil.renderStandalone(SummonerModelRegister.RUIN_RELIC, poseStack, bufferSource, FastColor.ARGB32.color((int) (alpha * 255), 255, 255, 255), packedLight);
    }
}
