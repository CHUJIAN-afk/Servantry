package first.summoner.client.attachmentEntityRenderer.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.ModelContext;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.RenderUtil;
import first.lyra.client.render.model.LyraModelRenderer;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.common.minion.minion.ScavengerFairy;
import first.summoner.register.SummonerModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * 拾荒妖精渲染器：使用独立 scavenger_fairy 模型。
 */
public class ScavengerFairyRenderer extends AbstractAttachmentEntityRenderer<ScavengerFairy> {

    @Override
    protected RenderContext<ScavengerFairy> createContext(ScavengerFairy fairy, PathNode visualNode, float partialTick, int packedLight) {
        return super.createContext(fairy, visualNode, partialTick, packedLight)
                .model(new ModelContext()
                               .scale(0.65f)
                               .translateOffset(-0.5f, -0.41f, -0.44f)
                               .alphaDistanceFactor(1.5f));
    }

    @Override
    protected void render(PoseStack poseStack, MultiBufferSource bufferSource) {
        LyraModelRenderer.json(SummonerModelRegister.SCAVENGER_FAIRY)
                .color(context.color.argbInt())
                .light(RenderUtil.FULL_LIGHT)
                .render(poseStack, bufferSource);
    }
}
