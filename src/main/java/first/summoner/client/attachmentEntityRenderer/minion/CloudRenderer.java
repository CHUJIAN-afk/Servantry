package first.summoner.client.attachmentEntityRenderer.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.RenderUtil;
import first.lyra.client.render.model.LyraModelRenderer;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.common.minion.sentry.Cloud;
import first.summoner.register.SummonerModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FastColor;

/**
 * 雨云哨兵渲染器。
 */
public class CloudRenderer extends AbstractAttachmentEntityRenderer<Cloud> {

    @Override
    protected RenderContext<Cloud> createContext(Cloud cloud, float partialTick) {
        return RenderContext.<Cloud>builder()
                .model(new ModelConfig<Cloud>()
                               .scale(0.8f)
                               .translateOffset(-0.5f, -0.35f, -0.5f)
                               .alphaDistanceFactor(1.5f))
                .build();
    }

    @Override
    protected void render(Cloud cloud, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<Cloud> context, float partialTick, int packedLight, float alpha) {
        LyraModelRenderer.json(SummonerModelRegister.CLOUD)
                .color(FastColor.ARGB32.color((int) (alpha * 255), 255, 255, 255))
                .light(RenderUtil.FULL_LIGHT)
                .render(poseStack, bufferSource);
    }
}
