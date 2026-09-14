package first.summoner.client.attachmentEntityRenderer.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.RenderUtil;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.common.minion.sentry.PulseTurret;
import first.summoner.register.SummonerModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FastColor;

/**
 * 脉冲塔哨兵渲染器。
 */
public class PulseTurretRenderer extends AbstractAttachmentEntityRenderer<PulseTurret> {

    @Override
    protected RenderContext<PulseTurret> createContext(PulseTurret turret, float partialTick) {
        return RenderContext.<PulseTurret>builder()
                .model(new ModelConfig<PulseTurret>()
                               .scale(0.75f)
                               .translateOffset(-0.5f, -0.5f, -0.5f)
                               .alphaDistanceFactor(1.5f))
                .build();
    }

    @Override
    protected void render(PulseTurret turret, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<PulseTurret> context, float partialTick, int packedLight, float alpha) {
        RenderUtil.renderStandalone(SummonerModelRegister.PULSE_TURRET, poseStack, bufferSource,
                FastColor.ARGB32.color((int) (alpha * 255), 255, 255, 255), RenderUtil.FULL_LIGHT);
    }
}
