package first.summoner.client.attachmentEntityRenderer.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.RenderUtil;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.entity.PathNode;
import first.summoner.common.minion.NecroSpirit;
import first.summoner.register.SummonerModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FastColor;

/**
 * 死魂灵仆从渲染器。
 */
public class NecroSpiritRenderer extends AbstractAttachmentEntityRenderer<NecroSpirit> {

    @Override
    protected RenderContext<NecroSpirit> createContext(NecroSpirit spirit, float partialTick) {
        return RenderContext.<NecroSpirit>builder()
                .model(new ModelConfig<NecroSpirit>()
                               .scale(0.55f)
                               .translateOffset(-0.5f, -0.5f, -0.5f)
                               .alphaDistanceFactor(1.5f))
                .build();
    }

    @Override
    protected void render(NecroSpirit spirit, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<NecroSpirit> context, float partialTick, int packedLight, float alpha) {
        RenderUtil.renderStandalone(SummonerModelRegister.NECRO_SPIRIT, poseStack, bufferSource,
                FastColor.ARGB32.color((int) (alpha * 255), 255, 255, 255), RenderUtil.FULL_LIGHT);
    }
}
