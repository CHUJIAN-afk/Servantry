package first.summoner.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.RenderUtil;
import first.lyra.client.render.model.LyraModelRenderer;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.Summoner;
import first.summoner.common.projectile.Corn;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FastColor;

/**
 * 玉米炮射弹渲染器：渲染 geo/corn 静态模型。
 */
public class CornRenderer extends AbstractAttachmentEntityRenderer<Corn> {

    @Override
    protected RenderContext<Corn> createContext(Corn corn, float partialTick) {
        return RenderContext.<Corn>builder()
                .model(new ModelConfig<Corn>()
                               .scale(0.45f)
                               .translateOffset(0, -0.8125f, -0.3125f)
                               .alphaDistanceFactor(1.5f))
                .build();
    }

    @Override
    protected void render(Corn corn, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<Corn> context, float partialTick, int packedLight, float alpha) {
        LyraModelRenderer.geo(Summoner.rl("corn"))
                .light(RenderUtil.FULL_LIGHT)
                .color(FastColor.ARGB32.color((int) (alpha * 255), 255, 255, 255))
                .render(poseStack, bufferSource);
    }
}
