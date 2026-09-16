package first.summoner.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.RenderUtil;
import first.lyra.client.render.model.LyraModelRenderer;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.attachmentEntity.PathNode;
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
        LyraModelRenderer.json(SummonerModelRegister.FROST_BOLT)
                .color(FastColor.ARGB32.color((int) (alpha * 255), 255, 255, 255))
                .light(RenderUtil.FULL_LIGHT)
                .render(poseStack, bufferSource);
    }
}
