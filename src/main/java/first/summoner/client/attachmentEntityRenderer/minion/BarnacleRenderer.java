package first.summoner.client.attachmentEntityRenderer.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.RenderUtil;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.entity.PathNode;
import first.summoner.common.minion.sentry.Barnacle;
import first.summoner.register.SummonerModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FastColor;

/**
 * 藤壶哨兵渲染器。
 */
public class BarnacleRenderer extends AbstractAttachmentEntityRenderer<Barnacle> {

    @Override
    protected RenderContext<Barnacle> createContext(Barnacle barnacle, float partialTick) {
        return RenderContext.<Barnacle>builder()
                .model(new ModelConfig<Barnacle>()
                               .scale(0.7f)
                               .translateOffset(-0.5f, -0.5f, -0.5f)
                               .alphaDistanceFactor(1.5f))
                .build();
    }

    @Override
    protected void render(Barnacle barnacle, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<Barnacle> context, float partialTick, int packedLight, float alpha) {
        RenderUtil.renderStandalone(SummonerModelRegister.BARNACLE, poseStack, bufferSource,
                FastColor.ARGB32.color((int) (alpha * 255), 255, 255, 255), RenderUtil.FULL_LIGHT);
    }
}
