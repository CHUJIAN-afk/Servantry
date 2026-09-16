package first.summoner.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.RenderUtil;
import first.lyra.client.render.model.LyraModelRenderer;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.common.projectile.ForbiddenOrb;
import first.summoner.register.SummonerModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FastColor;

/**
 * 禁戒弹射弹渲染器。
 */
public class ForbiddenOrbRenderer extends AbstractAttachmentEntityRenderer<ForbiddenOrb> {

    @Override
    protected RenderContext<ForbiddenOrb> createContext(ForbiddenOrb orb, float partialTick) {
        return RenderContext.<ForbiddenOrb>builder()
                .model(new ModelConfig<ForbiddenOrb>()
                               .scale(0.6f)
                               .translateOffset(-0.5f, -0.5f, -0.5f)
                               .alphaDistanceFactor(1.5f))
                .build();
    }

    @Override
    protected void render(ForbiddenOrb orb, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<ForbiddenOrb> context, float partialTick, int packedLight, float alpha) {
        LyraModelRenderer.json(SummonerModelRegister.FORBIDDEN_ORB)
                .color(FastColor.ARGB32.color((int) (alpha * 255), 255, 255, 255))
                .light(RenderUtil.FULL_LIGHT)
                .render(poseStack, bufferSource);
    }
}
