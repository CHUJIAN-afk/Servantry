package first.summoner.client.attachmentEntityRenderer.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.AfterimageContext;
import first.lyra.client.render.ModelContext;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.model.LyraModelRenderer;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.common.minion.minion.Twins;
import first.summoner.register.SummonerModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.ModelResourceLocation;
import software.bernie.geckolib.util.Color;

public class TwinsRenderer extends AbstractAttachmentEntityRenderer<Twins> {

    @Override
    protected RenderContext<Twins> createContext(Twins twins, PathNode visualNode, float partialTick, int packedLight) {
        int trailTimer = twins.getTrailTimer();
        RenderContext<Twins> renderContext = super.createContext(twins, visualNode, partialTick, packedLight)
                .model(new ModelContext()
                               .scale(0.5f)
                               .translateOffset(-0.5f, -0.25f, -0.5f)
                               .rotationOffset(180, 0, 0));
        if (trailTimer > 0 && twins.updateLastRenderPos(visualNode.pos())) {
            renderContext = renderContext.afterimage(new AfterimageContext().color(Color.ofOpaque(0xce4949)));
        }
        return renderContext;
    }

    @Override
    protected void render(PoseStack poseStack, MultiBufferSource bufferSource) {
        ModelResourceLocation modelKey = context.entity.isLaserEye() ? SummonerModelRegister.TWINS_LASER : SummonerModelRegister.TWINS_CURSED_FLAME;
        LyraModelRenderer.json(modelKey)
                .color(context.color.argbInt())
                .light(context.packedLight)
                .render(poseStack, bufferSource);
    }
}
