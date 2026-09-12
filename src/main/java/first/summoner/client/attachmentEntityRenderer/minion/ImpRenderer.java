package first.summoner.client.attachmentEntityRenderer.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.RenderUtil;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.entity.PathNode;
import first.summoner.common.minion.minion.Imp;
import first.summoner.register.SummonerModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FastColor;

/**
 * 小鬼仆从渲染器。
 */
public class ImpRenderer extends AbstractAttachmentEntityRenderer<Imp> {

    @Override
    protected RenderContext<Imp> createContext(Imp imp, float partialTick) {
        return RenderContext.<Imp>builder()
                .model(new ModelConfig<Imp>()
                               .scale(0.5f)
                               .translateOffset(-0.5f, -0.5f, -0.5f)
                               .alphaDistanceFactor(1.5f))
                .build();
    }

    @Override
    protected void render(Imp imp, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<Imp> context, float partialTick, int packedLight, float alpha) {
        RenderUtil.renderStandalone(SummonerModelRegister.IMP, poseStack, bufferSource,
                FastColor.ARGB32.color((int) (alpha * 255), 255, 255, 255), RenderUtil.FULL_LIGHT);
    }
}
