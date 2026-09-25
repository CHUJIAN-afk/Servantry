package first.summoner.client.attachmentEntityRenderer.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.ModelContext;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.RenderUtil;
import first.lyra.client.render.model.LyraModelRenderer;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.common.minion.sentry.MoonPortal;
import first.summoner.register.SummonerModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * 月亮传送门渲染器：圆形传送门模型。
 */
public class MoonPortalRenderer extends AbstractAttachmentEntityRenderer<MoonPortal> {

    @Override
    protected RenderContext<MoonPortal> createContext(MoonPortal portal, PathNode visualNode, float partialTick, int packedLight) {
        return super.createContext(portal, visualNode, partialTick, packedLight)
                .model(new ModelContext()
                               .scale(0.75f)
                               .translateOffset(-0.5f, -0.5f, -0.5f)
                               .alphaDistanceFactor(1.5f));
    }

    @Override
    protected void render(PoseStack poseStack, MultiBufferSource bufferSource) {
        LyraModelRenderer.json(SummonerModelRegister.MOON_PORTAL)
                .color(context.color.argbInt())
                .light(RenderUtil.FULL_LIGHT)
                .render(poseStack, bufferSource);
    }
}
