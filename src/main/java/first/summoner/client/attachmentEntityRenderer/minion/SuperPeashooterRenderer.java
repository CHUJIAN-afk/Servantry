package first.summoner.client.attachmentEntityRenderer.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.RenderUtil;
import first.lyra.client.render.model.LyraModelRenderer;
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
        LyraModelRenderer.json(SummonerModelRegister.SUPER_PEASHOOTER)
                .color(FastColor.ARGB32.color((int) (alpha * 255), 255, 255, 255))
                .light(RenderUtil.FULL_LIGHT)
                .render(poseStack, bufferSource);
    }
}
