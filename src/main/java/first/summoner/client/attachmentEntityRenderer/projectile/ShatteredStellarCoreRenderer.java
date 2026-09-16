package first.summoner.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.dynamicLight.DynamicLightDispatcher;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.RenderUtil;
import first.lyra.client.render.model.LyraModelRenderer;
import first.lyra.client.render.trail.ConeTrailConfig;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.common.projectile.ShatteredStellarCore;
import first.summoner.register.SummonerModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FastColor;

public class ShatteredStellarCoreRenderer extends AbstractAttachmentEntityRenderer<ShatteredStellarCore> {

    @Override
    protected RenderContext<ShatteredStellarCore> createContext(ShatteredStellarCore projectile, float partialTick) {
        return RenderContext.<ShatteredStellarCore>builder()
                .trail(new ConeTrailConfig<ShatteredStellarCore>()
                               .timer(15)
                               .colorRGB(0x8AE0FF)
                               .historyLength(4)
                               .maxRadius(0.075f)
                               .minRadiusRatio(0.025f)
                               .resolution(4)
                               .fadeOut(progress -> (float) Math.pow(Math.max(0.0f, 1.0f - progress), 2.0)))
                .model(new ModelConfig<ShatteredStellarCore>()
                               .scale(0.15f)
                               .translateOffset(-0.5f, -0.5f, -0.2f)
                               .rotationOffset(0, 0, 45))
                .build();
    }

    @Override
    protected void render(ShatteredStellarCore projectile, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<ShatteredStellarCore> context, float partialTick, int packedLight, float alpha) {
        LyraModelRenderer.json(SummonerModelRegister.STARDUST_CELL)
                .color(FastColor.ARGB32.color((int) (alpha * 255), 255, 255, 255))
                .light(RenderUtil.FULL_LIGHT)
                .render(poseStack, bufferSource);
        DynamicLightDispatcher.addLightSources(visualNode.pos(), 8);
    }
}
