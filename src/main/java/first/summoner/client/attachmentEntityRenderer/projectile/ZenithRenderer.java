package first.summoner.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.api.LyraAPI;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.ModelContext;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.RenderUtil;
import first.lyra.client.render.model.LyraModelRenderer;
import first.lyra.client.render.trail.RibbonTrailContext;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.common.projectile.Zenith;
import first.summoner.register.SummonerModelRegister;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;

public class ZenithRenderer extends AbstractAttachmentEntityRenderer<Zenith> {

    @Override
    protected RenderContext<Zenith> createContext(Zenith zenith, PathNode visualNode, float partialTick, int packedLight) {
        int historyLength = 6;
        if (zenith.getTickCount() <= 6) {
            historyLength = zenith.getTickCount();
        } else {
            if (zenith.getTickCount() > 8) {
                historyLength = 12 - zenith.getTickCount();
            }
        }
        return super.createContext(zenith, visualNode, partialTick, packedLight)
                .trail(new RibbonTrailContext<Zenith>()
                               .upOffset(1.32575f)
                               .timer(12)
                               .colorRGB(zenith.renderType.getColorRBG(getAlphaModify()))
                               .segmentsPerNode(6)
                               .historyLength(historyLength))
                .model(new ModelContext()
                               .scale(2)
                               .alphaDistanceFactor(2f)
                               .translateOffset(-0.5f, -0.5f, -0.5f)
                               .rotationOffset(0, 90, 45));
    }

    @Override
    protected void render(PoseStack poseStack, MultiBufferSource bufferSource) {
        LyraAPI.light(context.visualNode.pos(), 8);
        LyraModelRenderer.json(SummonerModelRegister.TERRAPRISM)
                .color(context.color.argbInt())
                .light(RenderUtil.FULL_LIGHT)
                .render(poseStack, bufferSource);
    }

    @Override
    protected float getAlphaModify() {
        float alpha = 1;
        if (Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
            alpha = super.getAlphaModify();
        } else {
            float tick = context.entity.getTickCount() + context.partialTick;
            if (tick < 4) {
                alpha = Math.clamp(tick / 4f, 0.21f, 1f);
            }
            if (tick > 9) {
                alpha = Math.clamp((12 - tick) / 3f, 0.21f, 1f);
            }
        }
        if (context.entity.alpha < alpha) {
            alpha = context.entity.alpha;
        }
        return alpha;
    }
}
