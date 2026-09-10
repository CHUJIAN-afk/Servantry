package first.summoner.client.attachmentEntityRenderer.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.RenderUtil;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.entity.PathNode;
import first.summoner.common.minion.DeadlySphere;
import first.summoner.register.SummonerModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FastColor;

/**
 * 致命球渲染器：独立 JSON 模型，滚动时随实体 yaw/pitch/roll 旋转。
 */
public class DeadlySphereRenderer extends AbstractAttachmentEntityRenderer<DeadlySphere> {

    @Override
    protected RenderContext<DeadlySphere> createContext(DeadlySphere sphere, float partialTick) {
        return RenderContext.<DeadlySphere>builder()
                .model(new ModelConfig<DeadlySphere>()
                               .scale(0.55f)
                               .translateOffset(-0.5f, -0.5f, -0.5f)
                               .alphaDistanceFactor(1.5f))
                .build();
    }

    @Override
    protected void render(DeadlySphere sphere, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<DeadlySphere> context, float partialTick, int packedLight, float alpha) {
        RenderUtil.renderStandalone(SummonerModelRegister.DEADLY_SPHERE, poseStack, bufferSource,
                FastColor.ARGB32.color((int) (alpha * 255), 255, 255, 255), RenderUtil.FULL_LIGHT);
    }
}
