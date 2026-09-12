package first.summoner.client.attachmentEntityRenderer.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.RenderUtil;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.entity.PathNode;
import first.summoner.common.minion.minion.ScavengerFairy;
import first.summoner.register.SummonerModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FastColor;

/**
 * 拾荒妖精渲染器：使用独立 scavenger_fairy 模型。
 */
public class ScavengerFairyRenderer extends AbstractAttachmentEntityRenderer<ScavengerFairy> {

    @Override
    protected RenderContext<ScavengerFairy> createContext(ScavengerFairy fairy, float partialTick) {
        return RenderContext.<ScavengerFairy>builder()
                .model(new ModelConfig<ScavengerFairy>()
                               .scale(0.65f)
                               .translateOffset(-0.5f, -0.41f, -0.44f)
                               .alphaDistanceFactor(1.5f))
                .build();
    }

    @Override
    protected void render(ScavengerFairy fairy, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<ScavengerFairy> context, float partialTick, int packedLight, float alpha) {
        RenderUtil.renderStandalone(SummonerModelRegister.SCAVENGER_FAIRY, poseStack, bufferSource,
                FastColor.ARGB32.color((int) (alpha * 255), 255, 255, 255), RenderUtil.FULL_LIGHT);
    }
}
