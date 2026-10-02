package first.summoner.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.ModelContext;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.RenderUtil;
import first.lyra.client.render.model.LyraModelRenderer;
import first.lyra.client.render.trail.ConeTrailContext;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.common.projectile.MiniChlorophyteCrystal;
import first.summoner.register.SummonerModelRegister;
import first.summoner.utils.DynamicLightUtil;
import net.minecraft.client.renderer.MultiBufferSource;

public class MiniChlorophyteCrystalRenderer extends AbstractAttachmentEntityRenderer<MiniChlorophyteCrystal> {

    @Override
    protected RenderContext<MiniChlorophyteCrystal> createContext(MiniChlorophyteCrystal crystal, PathNode visualNode, float partialTick, int packedLight) {
        return super.createContext(crystal, visualNode, partialTick, packedLight)
                .model(new ModelContext()
                        .rotationOffset(0, 90, 0)
                        .scale(0.5f)
                        .translateOffset(-0.5f, -0.5f, -0.5f)
                )
                .trail(new ConeTrailContext<MiniChlorophyteCrystal>()
                        .maxRadius(0.1f)
                        .minRadiusRatio(0.75f)
                        .resolution(4)
                        .timer(crystal.getTrailDuration())
                        .colorRGB(0x1bff10)
                        .historyLength(4)
                        .segmentsPerNode(2));
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
