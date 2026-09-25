package first.summoner.client.attachmentEntityRenderer.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.ModelContext;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.RenderUtil;
import first.lyra.client.render.model.LyraModelRenderer;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.common.minion.minion.NecroSpirit;
import first.summoner.register.SummonerModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * 死魂灵仆从渲染器。
 */
public class NecroSpiritRenderer extends AbstractAttachmentEntityRenderer<NecroSpirit> {

    @Override
    protected RenderContext<NecroSpirit> createContext(NecroSpirit spirit, PathNode visualNode, float partialTick, int packedLight) {
        return super.createContext(spirit, visualNode, partialTick, packedLight)
                .model(new ModelContext()
                               .scale(0.55f)
                               .translateOffset(-0.5f, -0.5f, -0.5f)
                               .alphaDistanceFactor(1.5f));
    }

    @Override
    protected void render(PoseStack poseStack, MultiBufferSource bufferSource) {
        LyraModelRenderer.json(SummonerModelRegister.NECRO_SPIRIT)
                .color(context.color.argbInt())
                .light(RenderUtil.FULL_LIGHT)
                .render(poseStack, bufferSource);
    }
}
