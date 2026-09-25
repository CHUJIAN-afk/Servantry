package first.summoner.client.attachmentEntityRenderer.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.ModelContext;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.model.LyraModelRenderer;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.common.minion.minion.Sharknado;
import first.summoner.register.SummonerModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;

public class SharknadoRenderer extends AbstractAttachmentEntityRenderer<Sharknado> {

    @Override
    protected RenderContext<Sharknado> createContext(Sharknado entity, PathNode visualNode, float partialTick, int packedLight) {
        return super.createContext(entity, visualNode, partialTick, packedLight)
                .model(new ModelContext()
                        .translateOffset(-0.5f, -0.5f, -0.5f)
                        .alphaDistanceFactor(1.5f)
                );
    }

    @Override
    protected void render(PoseStack poseStack, MultiBufferSource bufferSource) {
        LyraModelRenderer.json(SummonerModelRegister.SHARKNADO)
                .color(context.color.argbInt())
                .light(context.packedLight)
                .render(poseStack, bufferSource);
    }
}
