package first.summoner.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.trail.ConeTrailContext;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.common.projectile.PoisonSaliva;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * 毒唾液射弹渲染器 - 绿色圆锥拖尾。
 */
public class PoisonSalivaRenderer extends AbstractAttachmentEntityRenderer<PoisonSaliva> {

    @Override
    protected RenderContext<PoisonSaliva> createContext(PoisonSaliva projectile, PathNode visualNode, float partialTick, int packedLight) {
        return super.createContext(projectile, visualNode, partialTick, packedLight)
                .trail(new ConeTrailContext<PoisonSaliva>()
                        .maxRadius(0.18f)
                        .minRadiusRatio(0.45f)
                        .resolution(4)
                        .timer(5)
                        .colorRGB(0x59ff3a)
                        .fadeOut(progress -> (float) Math.pow(Math.max(0.0f, 1.0f - progress), 0.5)));
    }

    @Override
    protected void render(PoseStack poseStack, MultiBufferSource bufferSource) {
    }
}
