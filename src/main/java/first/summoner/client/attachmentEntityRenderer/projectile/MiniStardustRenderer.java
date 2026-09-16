package first.summoner.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.dynamicLight.DynamicLightDispatcher;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.RenderUtil;
import first.lyra.client.render.model.LyraModelRenderer;
import first.lyra.client.render.trail.ConeTrailConfig;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.common.projectile.MiniStardustCell;
import first.summoner.register.SummonerModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FastColor;

/**
 * 星细胞射弹渲染器。
 * <p>
 * 使用圆锥拖尾，渲染缩小版星尘细胞模型（0.25倍）。
 * </p>
 */
public class MiniStardustRenderer extends AbstractAttachmentEntityRenderer<MiniStardustCell> {

    @Override
    protected RenderContext<MiniStardustCell> createContext(MiniStardustCell projectile, float partialTick) {
        return RenderContext.<MiniStardustCell>builder()
                .trail(new ConeTrailConfig<MiniStardustCell>()
                        .timer(projectile.getTrailTimer())
                        .colorRGB(0x8AE0FF)
                        .historyLength(5)
                        .segmentsPerNode(16)
                        .maxRadius(0.075f)
                        .resolution(4)
                        .fadeOut(progress -> (float) Math.pow(Math.max(0.0f, 1.0f - progress), 2.0)))
                .model(new ModelConfig<MiniStardustCell>()
                        .scale(0.2f)
                        .translateOffset(-0.5f, -0.5f, -0.5f)
                        .rotationOffset(0, 0, 45))
                .build();
    }

    @Override
    protected void render(MiniStardustCell projectile, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<MiniStardustCell> context, float partialTick, int packedLight, float alpha) {
        LyraModelRenderer.json(SummonerModelRegister.STARDUST_CELL)
                .color(FastColor.ARGB32.color((int) (alpha * 255), 255, 255, 255))
                .light(RenderUtil.FULL_LIGHT)
                .render(poseStack, bufferSource);
        DynamicLightDispatcher.addLightSources(visualNode.pos(), 8);
    }

}
