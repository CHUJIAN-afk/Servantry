package first.summoner.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.ModelContext;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.RenderUtil;
import first.lyra.client.render.model.LyraModelRenderer;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.common.projectile.MiniNecroSpirit;
import first.summoner.register.SummonerModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * 迷你死魂灵射弹渲染器。
 */
public class MiniNecroSpiritRenderer extends AbstractAttachmentEntityRenderer<MiniNecroSpirit> {

    @Override
    protected RenderContext<MiniNecroSpirit> createContext(MiniNecroSpirit spirit, PathNode visualNode, float partialTick, int packedLight) {
        return super.createContext(spirit, visualNode, partialTick, packedLight)
                .model(new ModelContext()
                               .scale(0.5f)
                               .translateOffset(-0.5f, -0.5f, -0.5f)
                               .alphaDistanceFactor(1.5f));
    }

    @Override
    protected void render(PoseStack poseStack, MultiBufferSource bufferSource) {
        LyraModelRenderer.json(SummonerModelRegister.MINI_NECRO_SPIRIT)
                .color(context.color.argbInt())
                .light(RenderUtil.FULL_LIGHT)
                .render(poseStack, bufferSource);
    }
}
