package first.summoner.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.RenderUtil;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.common.projectile.HornetStinger;
import first.summoner.register.SummonerModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FastColor;

/**
 * 黄蜂刺射弹渲染器 - 独立的静态 JSON 模型。
 */
public class HornetStingerRenderer extends AbstractAttachmentEntityRenderer<HornetStinger> {

    @Override
    protected RenderContext<HornetStinger> createContext(HornetStinger stinger, float partialTick) {
        return RenderContext.<HornetStinger>builder()
                .model(new ModelConfig<HornetStinger>()
                               .scale(0.6f)
                               .translateOffset(-0.5f, -0.5f, -0.5f)
                               .alphaDistanceFactor(1.5f))
                .build();
    }

    @Override
    protected void render(HornetStinger stinger, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<HornetStinger> context, float partialTick, int packedLight, float alpha) {
        RenderUtil.renderStandalone(SummonerModelRegister.HORNET_STINGER, poseStack, bufferSource,
                FastColor.ARGB32.color((int) (alpha * 255), 255, 255, 255), RenderUtil.FULL_LIGHT);
    }
}
