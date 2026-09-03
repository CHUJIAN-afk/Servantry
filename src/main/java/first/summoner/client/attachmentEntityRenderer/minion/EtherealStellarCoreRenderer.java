package first.summoner.client.attachmentEntityRenderer.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.dynamicLight.DynamicLightDispatcher;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.RenderUtil;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.entity.PathNode;
import first.summoner.common.minion.EtherealStellarCore;
import first.summoner.register.SummonerModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FastColor;

public class EtherealStellarCoreRenderer extends AbstractAttachmentEntityRenderer<EtherealStellarCore> {

    @Override
    protected RenderContext<EtherealStellarCore> createContext(EtherealStellarCore minion, float partialTick) {
        return RenderContext.<EtherealStellarCore>builder()
                .model(new ModelConfig<EtherealStellarCore>()
                               .scale(0.5f)
                               .translateOffset(-0.5f, -0.5f, -0.5f)
                               .alphaDistanceFactor(1.5f)
                )
                .build();
    }

    @Override
    protected void render(EtherealStellarCore minion, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<EtherealStellarCore> context, float partialTick, int packedLight, float alpha) {
        RenderUtil.renderStandalone(SummonerModelRegister.STARDUST_CELL, poseStack, bufferSource, FastColor.ARGB32.color((int) (alpha * 255), 255, 255, 255), RenderUtil.FULL_LIGHT);
        DynamicLightDispatcher.addLightSources(visualNode.pos(), 8);
    }
}
