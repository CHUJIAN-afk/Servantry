package first.summoner.client.attachmentEntityRenderer.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.RenderUtil;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.common.minion.sentry.SuperPeashooter;
import first.summoner.register.SummonerModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FastColor;

/**
 * 超级电能豌豆射手渲染器。
 */
public class SuperPeashooterRenderer extends AbstractAttachmentEntityRenderer<SuperPeashooter> {

    @Override
    protected RenderContext<SuperPeashooter> createContext(SuperPeashooter shooter, float partialTick) {
        return RenderContext.<SuperPeashooter>builder()
                .model(new ModelConfig<SuperPeashooter>()
                               .scale(0.75f)
                               .translateOffset(-0.5f, -0.5f, -0.5f)
                               .alphaDistanceFactor(1.5f))
                .build();
    }

    @Override
    protected void render(SuperPeashooter shooter, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<SuperPeashooter> context, float partialTick, int packedLight, float alpha) {
        RenderUtil.renderStandalone(SummonerModelRegister.SUPER_PEASHOOTER, poseStack, bufferSource,
                FastColor.ARGB32.color((int) (alpha * 255), 255, 255, 255), RenderUtil.FULL_LIGHT);
    }
}
