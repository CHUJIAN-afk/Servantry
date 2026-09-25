package first.summoner.client.attachmentEntityRenderer.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.ModelContext;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.RenderUtil;
import first.lyra.client.render.model.LyraModelRenderer;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.common.minion.sentry.Ballista;
import first.summoner.register.SummonerModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * 弩炮哨兵渲染器：使用独立 ballista 模型，按当前朝向旋转。
 */
public class BallistaRenderer extends AbstractAttachmentEntityRenderer<Ballista> {

    @Override
    protected RenderContext<Ballista> createContext(Ballista ballista, PathNode visualNode, float partialTick, int packedLight) {
        return super.createContext(ballista, visualNode, partialTick, packedLight)
                .model(new ModelContext()
                               .scale(0.8f)
                               .translateOffset(-0.5f, -0.5f, -0.5f)
                               .alphaDistanceFactor(1.5f));
    }

    @Override
    protected void render(PoseStack poseStack, MultiBufferSource bufferSource) {
        LyraModelRenderer.json(SummonerModelRegister.BALLISTA)
                .color(context.color.argbInt())
                .light(RenderUtil.FULL_LIGHT)
                .render(poseStack, bufferSource);
    }
}
