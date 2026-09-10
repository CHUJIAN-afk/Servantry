package first.summoner.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.RenderUtil;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.entity.PathNode;
import first.summoner.common.projectile.FrostBolt;
import first.summoner.register.SummonerModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FastColor;

/**
 * 寒霜弹射弹渲染器。
 */
public class FrostBoltRenderer extends AbstractAttachmentEntityRenderer<FrostBolt> {

    @Override
    protected RenderContext<FrostBolt> createContext(FrostBolt bolt, float partialTick) {
        return RenderContext.<FrostBolt>builder()
                .model(new ModelConfig<FrostBolt>()
                               .scale(0.55f)
                               .translateOffset(-0.5f, -0.5f, -0.5f)
                               .alphaDistanceFactor(1.5f))
                .build();
    }

    @Override
    protected void render(FrostBolt bolt, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<FrostBolt> context, float partialTick, int packedLight, float alpha) {
        RenderUtil.renderStandalone(SummonerModelRegister.FROST_BOLT, poseStack, bufferSource,
                FastColor.ARGB32.color((int) (alpha * 255), 255, 255, 255), RenderUtil.FULL_LIGHT);
    }
}
