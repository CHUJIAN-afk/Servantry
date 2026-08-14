package first.summoner.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.ModelRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.entity.PathNode;
import first.summoner.common.projectile.SharkDragon;
import first.summoner.register.SummonerModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;

public class SharkDragonRenderer extends AbstractAttachmentEntityRenderer<SharkDragon> {

    @Override
    protected RenderContext<SharkDragon> createContext(SharkDragon laser) {
        return RenderContext.<SharkDragon>builder()
                .model(new ModelConfig<SharkDragon>()
                        .rotationOffset(180, 0, 0)
                        .translateOffset(-0.5f, -0.5f, -0.5f)
                )
                .build();
    }

    @Override
    protected void render(SharkDragon entity, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<SharkDragon> context, float partialTick) {
        if (entity.getTickCount() % 20 < 10) {
            ModelRenderer.renderModel(SummonerModelRegister.SHARK_OPEN, poseStack, bufferSource);
        } else {
            ModelRenderer.renderModel(SummonerModelRegister.SHARK_CLOSE, poseStack, bufferSource);
        }
    }
}
