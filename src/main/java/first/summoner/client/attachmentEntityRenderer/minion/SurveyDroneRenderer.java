package first.summoner.client.attachmentEntityRenderer.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.RenderUtil;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.entity.PathNode;
import first.summoner.common.minion.OreScout;
import first.summoner.register.SummonerModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FastColor;

/**
 * 探矿无人机渲染器：使用独立 survey_drone 模型。
 */
public class SurveyDroneRenderer extends AbstractAttachmentEntityRenderer<OreScout> {

    @Override
    protected RenderContext<OreScout> createContext(OreScout scout, float partialTick) {
        return RenderContext.<OreScout>builder()
                .model(new ModelConfig<OreScout>()
                               .scale(0.7f)
                               .translateOffset(-0.5f, -0.19f, -0.44f)
                               .alphaDistanceFactor(1.5f))
                .build();
    }

    @Override
    protected void render(OreScout scout, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<OreScout> context, float partialTick, int packedLight, float alpha) {
        RenderUtil.renderStandalone(SummonerModelRegister.SURVEY_DRONE, poseStack, bufferSource,
                FastColor.ARGB32.color((int) (alpha * 255), 255, 255, 255), RenderUtil.FULL_LIGHT);
    }
}
