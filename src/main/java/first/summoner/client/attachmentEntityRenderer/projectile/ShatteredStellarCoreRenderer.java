package first.summoner.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.api.LyraAPI;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.ModelContext;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.RenderUtil;
import first.lyra.client.render.model.LyraModelRenderer;
import first.lyra.client.render.trail.ConeTrailContext;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.common.projectile.ShatteredStellarCore;
import first.summoner.register.SummonerModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;

public class ShatteredStellarCoreRenderer extends AbstractAttachmentEntityRenderer<ShatteredStellarCore> {

    @Override
    protected RenderContext<ShatteredStellarCore> createContext(ShatteredStellarCore projectile, PathNode visualNode, float partialTick, int packedLight) {
        return super.createContext(projectile, visualNode, partialTick, packedLight)
                .trail(new ConeTrailContext<ShatteredStellarCore>()
                               .maxRadius(0.075f)
                               .minRadiusRatio(0.025f)
                               .resolution(4)
                               .timer(15)
                               .colorRGB(0x8AE0FF)
                               .historyLength(4)
                               .fadeOut(progress -> (float) Math.pow(Math.max(0.0f, 1.0f - progress), 2.0)))
                .model(new ModelContext()
                               .scale(0.15f)
                               .translateOffset(-0.5f, -0.5f, -0.2f)
                               .rotationOffset(0, 0, 45));
    }

    @Override
    protected void render(PoseStack poseStack, MultiBufferSource bufferSource) {
        LyraModelRenderer.json(SummonerModelRegister.STARDUST_CELL)
                .color(context.color.argbInt())
                .light(RenderUtil.FULL_LIGHT)
                .render(poseStack, bufferSource);
        LyraAPI.light(context.visualNode.pos(), 8);
    }
}
