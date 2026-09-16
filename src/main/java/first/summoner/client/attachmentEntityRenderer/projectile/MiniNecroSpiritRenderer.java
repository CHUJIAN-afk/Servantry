package first.summoner.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.RenderUtil;
import first.lyra.client.render.model.LyraModelRenderer;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.common.projectile.MiniNecroSpirit;
import first.summoner.register.SummonerModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FastColor;

/**
 * 迷你死魂灵射弹渲染器。
 */
public class MiniNecroSpiritRenderer extends AbstractAttachmentEntityRenderer<MiniNecroSpirit> {

    @Override
    protected RenderContext<MiniNecroSpirit> createContext(MiniNecroSpirit spirit, float partialTick) {
        return RenderContext.<MiniNecroSpirit>builder()
                .model(new ModelConfig<MiniNecroSpirit>()
                               .scale(0.5f)
                               .translateOffset(-0.5f, -0.5f, -0.5f)
                               .alphaDistanceFactor(1.5f))
                .build();
    }

    @Override
    protected void render(MiniNecroSpirit spirit, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<MiniNecroSpirit> context, float partialTick, int packedLight, float alpha) {
        LyraModelRenderer.json(SummonerModelRegister.MINI_NECRO_SPIRIT)
                .color(FastColor.ARGB32.color((int) (alpha * 255), 255, 255, 255))
                .light(RenderUtil.FULL_LIGHT)
                .render(poseStack, bufferSource);
    }
}
