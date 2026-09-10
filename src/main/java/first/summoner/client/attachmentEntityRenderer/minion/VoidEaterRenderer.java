package first.summoner.client.attachmentEntityRenderer.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.RenderUtil;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.entity.PathNode;
import first.summoner.common.minion.VoidEater;
import first.summoner.register.SummonerModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.util.FastColor;

/**
 * 虚空吞噬者渲染器：与星尘龙一样分头、身、尾三段模型。
 */
public class VoidEaterRenderer extends AbstractAttachmentEntityRenderer<VoidEater> {

    @Override
    protected RenderContext<VoidEater> createContext(VoidEater eater, float partialTick) {
        return RenderContext.<VoidEater>builder()
                .model(new ModelConfig<VoidEater>()
                               .scale(0.5f)
                               .translateOffset(-0.5f, -0.5f, -0.5f)
                               .alphaDistanceFactor(1.5f))
                .build();
    }

    @Override
    protected void render(VoidEater eater, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<VoidEater> context, float partialTick, int packedLight, float alpha) {
        int total = eater.getTotalSegments();
        int index = eater.getSegmentIndex();
        ModelResourceLocation model = index == 0
                ? SummonerModelRegister.VOID_EATER_HEAD
                : index == total - 1 ? SummonerModelRegister.VOID_EATER_TAIL : SummonerModelRegister.VOID_EATER_BODY;
        RenderUtil.renderStandalone(model, poseStack, bufferSource,
                FastColor.ARGB32.color((int) (alpha * 255), 255, 255, 255), RenderUtil.FULL_LIGHT);
    }
}
