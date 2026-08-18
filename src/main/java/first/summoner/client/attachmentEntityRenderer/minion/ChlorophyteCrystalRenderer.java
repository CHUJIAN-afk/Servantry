package first.summoner.client.attachmentEntityRenderer.minion;


import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.dynamicLight.DynamicLightDispatcher;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.RenderUtil;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.entity.PathNode;
import first.summoner.common.minion.ChlorophyteCrystal;
import first.summoner.register.SummonerModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FastColor;

public class ChlorophyteCrystalRenderer extends AbstractAttachmentEntityRenderer<ChlorophyteCrystal> {

    @Override
    protected RenderContext<ChlorophyteCrystal> createContext(ChlorophyteCrystal minion, float partialTick) {
        return RenderContext.<ChlorophyteCrystal>builder()
                .model(new ModelConfig<ChlorophyteCrystal>()
                        .scale(0.75f)
                        .translateOffset(-0.5f, -0.5f, -0.5f)
                )
                .build();
    }

    @Override
    protected void render(ChlorophyteCrystal minion, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<ChlorophyteCrystal> context, float partialTick, float alpha) {
        RenderUtil.renderStandalone(SummonerModelRegister.CHLOROPHYTE_CRYSTAL, poseStack, bufferSource, FastColor.ARGB32.color((int) (alpha * 255), 255, 255, 255), RenderUtil.FULL_LIGHT);
        DynamicLightDispatcher.addLightSources(visualNode.pos(), 8);
    }
}
