package first.summoner.client.attachmentEntityRenderer.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.ModelContext;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.RenderUtil;
import first.lyra.client.render.model.LyraModelRenderer;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.common.minion.minion.ChlorophyteCrystal;
import first.summoner.register.SummonerModelRegister;
import first.summoner.utils.DynamicLightUtil;
import net.minecraft.client.renderer.MultiBufferSource;

public class ChlorophyteCrystalRenderer extends AbstractAttachmentEntityRenderer<ChlorophyteCrystal> {

    @Override
    protected RenderContext<ChlorophyteCrystal> createContext(ChlorophyteCrystal minion, PathNode visualNode, float partialTick, int packedLight) {
        return super.createContext(minion, visualNode, partialTick, packedLight)
                .model(new ModelContext()
                        .scale(0.75f)
                        .translateOffset(-0.5f, -0.5f, -0.5f)
                );
    }

    @Override
    protected void render(PoseStack poseStack, MultiBufferSource bufferSource) {
        LyraModelRenderer.json(SummonerModelRegister.CHLOROPHYTE_CRYSTAL)
                .color(context.color.argbInt())
                .light(RenderUtil.FULL_LIGHT)
                .render(poseStack, bufferSource);
        DynamicLightUtil.addLight(context.visualNode.pos(), 8);
    }
}
