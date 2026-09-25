package first.summoner.client.attachmentEntityRenderer.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.ModelContext;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.RenderUtil;
import first.lyra.client.render.model.LyraModelRenderer;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.common.minion.minion.VoidEater;
import first.summoner.register.SummonerModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.ModelResourceLocation;

/**
 * 虚空吞噬者渲染器：与星尘龙一样分头、身、尾三段模型。
 */
public class VoidEaterRenderer extends AbstractAttachmentEntityRenderer<VoidEater> {

    @Override
    protected RenderContext<VoidEater> createContext(VoidEater eater, PathNode visualNode, float partialTick, int packedLight) {
        return super.createContext(eater, visualNode, partialTick, packedLight)
                .model(new ModelContext()
                               .scale(0.5f)
                               .translateOffset(-0.5f, -0.5f, -0.5f)
                               .alphaDistanceFactor(1.5f));
    }

    @Override
    protected void render(PoseStack poseStack, MultiBufferSource bufferSource) {
        int total = Math.max(1, context.entity.getSameSize());
        int index = context.entity.getOrder();
        ModelResourceLocation model = index == 0
                ? SummonerModelRegister.VOID_EATER_HEAD
                : index == total - 1 ? SummonerModelRegister.VOID_EATER_TAIL : SummonerModelRegister.VOID_EATER_BODY;
        LyraModelRenderer.json(model)
                .color(context.color.argbInt())
                .light(RenderUtil.FULL_LIGHT)
                .render(poseStack, bufferSource);
    }
}
