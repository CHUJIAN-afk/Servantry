package first.summoner.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.RenderUtil;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.common.projectile.ExplosionFlameBall;
import first.summoner.register.SummonerModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FastColor;

/**
 * 爆炸烈焰火球射弹渲染器。
 */
public class ExplosionFlameBallRenderer extends AbstractAttachmentEntityRenderer<ExplosionFlameBall> {

    @Override
    protected RenderContext<ExplosionFlameBall> createContext(ExplosionFlameBall ball, float partialTick) {
        return RenderContext.<ExplosionFlameBall>builder()
                .model(new ModelConfig<ExplosionFlameBall>()
                               .scale(0.6f)
                               .translateOffset(-0.5f, -0.5f, -0.5f)
                               .alphaDistanceFactor(1.5f))
                .build();
    }

    @Override
    protected void render(ExplosionFlameBall ball, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<ExplosionFlameBall> context, float partialTick, int packedLight, float alpha) {
        RenderUtil.renderStandalone(SummonerModelRegister.EXPLOSION_FLAME_BALL, poseStack, bufferSource,
                FastColor.ARGB32.color((int) (alpha * 255), 255, 255, 255), RenderUtil.FULL_LIGHT);
    }
}
