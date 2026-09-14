package first.summoner.client.attachmentEntityRenderer.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.RenderUtil;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.common.minion.sentry.EyeLaserTurret;
import first.summoner.register.SummonerModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FastColor;

/**
 * 眼球激光塔渲染器。
 */
public class EyeLaserTurretRenderer extends AbstractAttachmentEntityRenderer<EyeLaserTurret> {

    @Override
    protected RenderContext<EyeLaserTurret> createContext(EyeLaserTurret turret, float partialTick) {
        return RenderContext.<EyeLaserTurret>builder()
                .model(new ModelConfig<EyeLaserTurret>()
                               .scale(0.7f)
                               .translateOffset(-0.5f, -0.5f, -0.5f)
                               .alphaDistanceFactor(1.5f))
                .build();
    }

    @Override
    protected void render(EyeLaserTurret turret, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<EyeLaserTurret> context, float partialTick, int packedLight, float alpha) {
        RenderUtil.renderStandalone(SummonerModelRegister.EYE_LASER_TURRET, poseStack, bufferSource,
                FastColor.ARGB32.color((int) (alpha * 255), 255, 255, 255), RenderUtil.FULL_LIGHT);
    }
}
