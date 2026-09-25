package first.summoner.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.ModelContext;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.RenderUtil;
import first.lyra.client.render.model.LyraModelRenderer;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.common.projectile.FrostBolt;
import first.summoner.register.SummonerModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * 寒霜弹射弹渲染器。
 */
public class FrostBoltRenderer extends AbstractAttachmentEntityRenderer<FrostBolt> {

    @Override
    protected RenderContext<FrostBolt> createContext(FrostBolt bolt, PathNode visualNode, float partialTick, int packedLight) {
        return super.createContext(bolt, visualNode, partialTick, packedLight)
                .model(new ModelContext()
                               .scale(0.55f)
                               .translateOffset(-0.5f, -0.5f, -0.5f)
                               .alphaDistanceFactor(1.5f));
    }

    @Override
    protected void render(PoseStack poseStack, MultiBufferSource bufferSource) {
        LyraModelRenderer.json(SummonerModelRegister.FROST_BOLT)
                .color(context.color.argbInt())
                .light(RenderUtil.FULL_LIGHT)
                .render(poseStack, bufferSource);
    }
}
