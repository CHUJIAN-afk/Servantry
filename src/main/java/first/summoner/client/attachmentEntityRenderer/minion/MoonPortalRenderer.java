package first.summoner.client.attachmentEntityRenderer.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.RenderUtil;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.entity.PathNode;
import first.summoner.common.sentry.MoonPortal;
import first.summoner.register.SummonerModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FastColor;

/**
 * 月亮传送门渲染器：圆形传送门模型。
 */
public class MoonPortalRenderer extends AbstractAttachmentEntityRenderer<MoonPortal> {

    @Override
    protected RenderContext<MoonPortal> createContext(MoonPortal portal, float partialTick) {
        return RenderContext.<MoonPortal>builder()
                .model(new ModelConfig<MoonPortal>()
                               .scale(0.75f)
                               .translateOffset(-0.5f, -0.5f, -0.5f)
                               .alphaDistanceFactor(1.5f))
                .build();
    }

    @Override
    protected void render(MoonPortal portal, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<MoonPortal> context, float partialTick, int packedLight, float alpha) {
        RenderUtil.renderStandalone(SummonerModelRegister.MOON_PORTAL, poseStack, bufferSource,
                FastColor.ARGB32.color((int) (alpha * 255), 255, 255, 255), RenderUtil.FULL_LIGHT);
    }
}
