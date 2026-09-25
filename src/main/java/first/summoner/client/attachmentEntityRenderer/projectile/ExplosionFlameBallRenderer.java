package first.summoner.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.ModelContext;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.RenderUtil;
import first.lyra.client.render.model.LyraModelRenderer;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.common.projectile.ExplosionFlameBall;
import first.summoner.register.SummonerModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * 爆炸烈焰火球射弹渲染器。
 */
public class ExplosionFlameBallRenderer extends AbstractAttachmentEntityRenderer<ExplosionFlameBall> {

    @Override
    protected RenderContext<ExplosionFlameBall> createContext(ExplosionFlameBall ball, PathNode visualNode, float partialTick, int packedLight) {
        return super.createContext(ball, visualNode, partialTick, packedLight)
                .model(new ModelContext()
                               .scale(0.6f)
                               .translateOffset(-0.5f, -0.5f, -0.5f)
                               .alphaDistanceFactor(1.5f));
    }

    @Override
    protected void render(PoseStack poseStack, MultiBufferSource bufferSource) {
        LyraModelRenderer.json(SummonerModelRegister.EXPLOSION_FLAME_BALL)
                .color(context.color.argbInt())
                .light(RenderUtil.FULL_LIGHT)
                .render(poseStack, bufferSource);
    }
}
