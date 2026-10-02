package first.summoner.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.api.LyraAPI;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.ModelContext;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.RenderUtil;
import first.lyra.client.render.model.LyraModelRenderer;
import first.lyra.client.render.trail.ConeTrailContext;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.common.projectile.MiniStardustCell;
import first.summoner.register.SummonerModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * 星细胞射弹渲染器。
 * <p>
 * 使用圆锥拖尾，渲染缩小版星尘细胞模型（0.25倍）。
 * </p>
 */
public class MiniStardustRenderer extends AbstractAttachmentEntityRenderer<MiniStardustCell> {

    @Override
    protected RenderContext<MiniStardustCell> createContext(MiniStardustCell projectile, PathNode visualNode, float partialTick, int packedLight) {
        return super.createContext(projectile, visualNode, partialTick, packedLight)
                .trail(new ConeTrailContext<MiniStardustCell>()
                        .maxRadius(0.075f)
                        .resolution(4)
                        .timer(projectile.getTrailTimer())
                        .colorRGB(0x8AE0FF)
                        .historyLength(5)
                        .segmentsPerNode(16)
                        .fadeOut(progress -> (float) Math.pow(Math.max(0.0f, 1.0f - progress), 2.0)))
                .model(new ModelContext()
                        .scale(0.2f)
                        .translateOffset(-0.5f, -0.5f, -0.5f)
                        .rotationOffset(0, 0, 45));
    }

    @Override
    protected void render(PoseStack poseStack, MultiBufferSource bufferSource) {
        LyraModelRenderer.json(SummonerModelRegister.STARDUST_CELL)
                .color(context.color.argbInt())
                .light(RenderUtil.FULL_LIGHT)
                .render(poseStack, bufferSource);
        LyraAPI.light(context.visualNode.pos(), 8);
    }

}
