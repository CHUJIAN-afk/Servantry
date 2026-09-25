package first.summoner.client.attachmentEntityRenderer.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.ModelContext;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.RenderUtil;
import first.lyra.client.render.model.LyraModelRenderer;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.common.minion.sentry.PulseTurret;
import first.summoner.register.SummonerModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * 脉冲塔哨兵渲染器。
 */
public class PulseTurretRenderer extends AbstractAttachmentEntityRenderer<PulseTurret> {

    @Override
    protected RenderContext<PulseTurret> createContext(PulseTurret turret, PathNode visualNode, float partialTick, int packedLight) {
        return super.createContext(turret, visualNode, partialTick, packedLight)
                .model(new ModelContext()
                               .scale(0.75f)
                               .translateOffset(-0.5f, -0.5f, -0.5f)
                               .alphaDistanceFactor(1.5f));
    }

    @Override
    protected void render(PoseStack poseStack, MultiBufferSource bufferSource) {
        LyraModelRenderer.json(SummonerModelRegister.PULSE_TURRET)
                .color(context.color.argbInt())
                .light(RenderUtil.FULL_LIGHT)
                .render(poseStack, bufferSource);
    }
}
