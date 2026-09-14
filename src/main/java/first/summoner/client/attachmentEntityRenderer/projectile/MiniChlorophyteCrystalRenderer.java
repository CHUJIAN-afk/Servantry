package first.summoner.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.dynamicLight.DynamicLightDispatcher;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.RenderUtil;
import first.lyra.client.render.trail.ConeTrailConfig;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.common.projectile.MiniChlorophyteCrystal;
import first.summoner.register.SummonerModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FastColor;

public class MiniChlorophyteCrystalRenderer extends AbstractAttachmentEntityRenderer<MiniChlorophyteCrystal> {

    @Override
    protected RenderContext<MiniChlorophyteCrystal> createContext(MiniChlorophyteCrystal crystal, float partialTick) {
        return RenderContext.<MiniChlorophyteCrystal>builder()
                .model(new ModelConfig<MiniChlorophyteCrystal>()
                        .rotationOffset(0, 90, 0)
                        .scale(0.5f)
                        .translateOffset(-0.5f, -0.5f, -0.5f)
                )
                .trail(new ConeTrailConfig<MiniChlorophyteCrystal>()
                        .timer(crystal.getTrailDuration())
                        .colorRGB(0x1bff10)
                        .historyLength(4)
                        .segmentsPerNode(2)
                        .maxRadius(0.1f)
                        .minRadiusRatio(0.75f)
                        .resolution(4))
                .build();
    }

    @Override
    protected void render(MiniChlorophyteCrystal entity, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<MiniChlorophyteCrystal> context, float partialTick, int packedLight, float alpha) {
        RenderUtil.renderStandalone(SummonerModelRegister.CHLOROPHYTE_CRYSTAL, poseStack, bufferSource, FastColor.ARGB32.color((int) (alpha * 255), 255, 255, 255), RenderUtil.FULL_LIGHT);
        DynamicLightDispatcher.addLightSources(visualNode.pos(), 8);
    }
}
