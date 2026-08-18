package first.summoner.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.RenderUtil;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.client.render.trail.RibbonTrailConfig;
import first.lyra.common.entity.PathNode;
import first.summoner.common.projectile.Zenith;
import first.summoner.register.SummonerModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FastColor;

public class ZenithRenderer extends AbstractAttachmentEntityRenderer<Zenith> {

    @Override
    protected RenderContext<Zenith> createContext(Zenith zenith, float partialTick) {
        return RenderContext.<Zenith>builder()
                .trail(new RibbonTrailConfig<Zenith>()
                               .timer(0)
                               .colorRGB(0xffffff)
                               .historyLength(2)
                               .upOffset(1.32575f)
                )
                .model(new ModelConfig<Zenith>()
                               .scale(2)
                               .translateOffset(-0.5f, -0.5f, -0.5f)
                               .rotationOffset(0, 90, 45))
                .build();
    }

    @Override
    protected void render(Zenith zenith, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<Zenith> context, float partialTick, float alpha) {
        RenderUtil.renderStandalone(SummonerModelRegister.TERRAPRISM, poseStack, bufferSource, FastColor.ARGB32.color((int) (alpha * 255), 255, 255, 255), RenderUtil.FULL_LIGHT);
    }
}
