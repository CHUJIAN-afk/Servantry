package first.summoner.client.attachmentEntityRenderer.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.RenderUtil;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.entity.PathNode;
import first.summoner.common.minion.Sharknado;
import first.summoner.register.SummonerModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FastColor;

public class SharknadoRenderer extends AbstractAttachmentEntityRenderer<Sharknado> {

    @Override
    protected RenderContext<Sharknado> createContext(Sharknado entity, float partialTick) {
        return RenderContext.<Sharknado>builder()
                .model(new ModelConfig<Sharknado>()
                        .translateOffset(-0.5f, -0.5f, -0.5f)
                        .alphaDistanceFactor(1.5f)
                )
                .build();
    }

    @Override
    protected void render(Sharknado entity, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<Sharknado> context, float partialTick, int packedLight, float alpha) {
        RenderUtil.renderStandalone(SummonerModelRegister.SHARKNADO, poseStack, bufferSource, FastColor.ARGB32.color((int) (alpha * 255), 255, 255, 255), packedLight);
    }
}
