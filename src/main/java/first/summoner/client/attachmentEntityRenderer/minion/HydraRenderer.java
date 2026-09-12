package first.summoner.client.attachmentEntityRenderer.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.RenderUtil;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.entity.PathNode;
import first.summoner.common.minion.sentry.Hydra;
import first.summoner.register.SummonerModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FastColor;

/**
 * 九头蛇哨兵渲染器。
 */
public class HydraRenderer extends AbstractAttachmentEntityRenderer<Hydra> {

    @Override
    protected RenderContext<Hydra> createContext(Hydra hydra, float partialTick) {
        return RenderContext.<Hydra>builder()
                .model(new ModelConfig<Hydra>()
                               .scale(0.8f)
                               .translateOffset(-0.5f, -0.5f, -0.5f)
                               .alphaDistanceFactor(1.5f))
                .build();
    }

    @Override
    protected void render(Hydra hydra, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<Hydra> context, float partialTick, int packedLight, float alpha) {
        RenderUtil.renderStandalone(SummonerModelRegister.HYDRA, poseStack, bufferSource,
                FastColor.ARGB32.color((int) (alpha * 255), 255, 255, 255), RenderUtil.FULL_LIGHT);
    }
}
