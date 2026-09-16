package first.summoner.client.attachmentEntityRenderer.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.RenderUtil;
import first.lyra.client.render.model.LyraModelRenderer;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.common.minion.sentry.Ballista;
import first.summoner.register.SummonerModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FastColor;

/**
 * 弩炮哨兵渲染器：使用独立 ballista 模型，按当前朝向旋转。
 */
public class BallistaRenderer extends AbstractAttachmentEntityRenderer<Ballista> {

    @Override
    protected RenderContext<Ballista> createContext(Ballista ballista, float partialTick) {
        return RenderContext.<Ballista>builder()
                .model(new ModelConfig<Ballista>()
                               .scale(0.8f)
                               .translateOffset(-0.5f, -0.5f, -0.5f)
                               .alphaDistanceFactor(1.5f))
                .build();
    }

    @Override
    protected void render(Ballista ballista, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<Ballista> context, float partialTick, int packedLight, float alpha) {
        LyraModelRenderer.json(SummonerModelRegister.BALLISTA)
                .color(FastColor.ARGB32.color((int) (alpha * 255), 255, 255, 255))
                .light(RenderUtil.FULL_LIGHT)
                .render(poseStack, bufferSource);
    }
}
