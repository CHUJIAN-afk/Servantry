package first.summoner.client.attachmentEntityRenderer.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.RenderUtil;
import first.lyra.client.render.animated.AnimatedModelRenderer;
import first.lyra.client.render.animated.AnimatedRenderOptions;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.entity.PathNode;
import first.summoner.Summoner;
import first.summoner.common.minion.sentry.Cannon;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FastColor;

/**
 * 玉米炮哨兵渲染器：渲染 geo/cannon 静态模型。
 */
public class CannonRenderer extends AbstractAttachmentEntityRenderer<Cannon> {

    @Override
    protected RenderContext<Cannon> createContext(Cannon cannon, float partialTick) {
        return RenderContext.<Cannon>builder()
                .model(new ModelConfig<Cannon>()
                               .scale(0.6f)
                               .translateOffset(0, -0.625f, -0.375f)
                               .alphaDistanceFactor(1.5f))
                .build();
    }

    @Override
    protected void render(Cannon cannon, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<Cannon> context, float partialTick, int packedLight, float alpha) {
        AnimatedRenderOptions request = AnimatedModelRenderer.request(Summoner.rl("cannon"))
                .light(RenderUtil.FULL_LIGHT)
                .color(FastColor.ARGB32.color((int) (alpha * 255), 255, 255, 255));
        request.render(poseStack, bufferSource);
    }
}
