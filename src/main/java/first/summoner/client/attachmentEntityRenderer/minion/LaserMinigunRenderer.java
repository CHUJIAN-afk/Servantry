package first.summoner.client.attachmentEntityRenderer.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.animated.AnimatedModelRenderer;
import first.lyra.client.render.animated.AnimatedRenderOptions;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.entity.PathNode;
import first.summoner.Summoner;
import first.summoner.common.minion.minion.LaserMinigun;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FastColor;

/**
 * 激光机枪渲染器 - 使用原生 Geo 模型渲染 barrel 旋转动画；
 * 平时按 1x 播放，射击时按 2x 播放。
 */
public class LaserMinigunRenderer extends AbstractAttachmentEntityRenderer<LaserMinigun> {

    @Override
    protected RenderContext<LaserMinigun> createContext(LaserMinigun entity, float partialTick) {
        return RenderContext.<LaserMinigun>builder()
                .model(new ModelConfig<LaserMinigun>()
                               .translateOffset(0.0375f, -0.35f, 0)
                               .rotationOffset(180, 0, 0)
                               .alphaDistanceFactor(1.5f))
                .build();
    }

    @Override
    protected void render(LaserMinigun minion, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<LaserMinigun> context, float partialTick, int packedLight, float alpha) {
        AnimatedRenderOptions request = AnimatedModelRenderer.request(Summoner.rl("laser_minigun"))
                .light(packedLight)
                .color(FastColor.ARGB32.color((int) (alpha * 255), 255, 255, 255))
                .animation("shooting", minion.getRenderTick(partialTick));
        request.render(poseStack, bufferSource);
    }
}
