package first.summoner.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.ModelContext;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.RenderUtil;
import first.lyra.client.render.model.LyraModelRenderer;
import first.lyra.client.render.trail.RibbonTrailContext;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.common.projectile.Zenith;
import first.summoner.register.SummonerModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;

public class ZenithRenderer extends AbstractAttachmentEntityRenderer<Zenith> {

    @Override
    protected RenderContext<Zenith> createContext(Zenith zenith, PathNode visualNode, float partialTick, int packedLight) {
        return super.createContext(zenith, visualNode, partialTick, packedLight)
                .trail(new RibbonTrailContext<Zenith>()
                               .upOffset(1.32575f)
                               .timer(0)
                               .colorRGB(0xffffff)
                               .historyLength(2))
                .model(new ModelContext()
                               .scale(2)
                               .translateOffset(-0.5f, -0.5f, -0.5f)
                               .rotationOffset(0, 90, 45));
    }

    @Override
    protected void render(PoseStack poseStack, MultiBufferSource bufferSource) {
        LyraModelRenderer.json(SummonerModelRegister.TERRAPRISM)
                .color(context.color.argbInt())
                .light(RenderUtil.FULL_LIGHT)
                .render(poseStack, bufferSource);
    }
}
