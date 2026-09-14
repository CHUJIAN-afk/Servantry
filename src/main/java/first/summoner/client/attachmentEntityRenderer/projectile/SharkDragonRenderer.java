package first.summoner.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.RenderUtil;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.common.projectile.SharkDragon;
import first.summoner.register.SummonerModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.util.FastColor;

public class SharkDragonRenderer extends AbstractAttachmentEntityRenderer<SharkDragon> {

    @Override
    protected RenderContext<SharkDragon> createContext(SharkDragon laser, float partialTick) {
        return RenderContext.<SharkDragon>builder()
                .model(new ModelConfig<SharkDragon>()
                        .rotationOffset(180, 0, 0)
                        .translateOffset(-0.5f, -0.5f, -0.5f)
                )
                .build();
    }

    @Override
    protected void render(SharkDragon entity, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<SharkDragon> context, float partialTick, int packedLight, float alpha) {
        ModelResourceLocation modelKey = entity.getTickCount() % 20 < 10 ? SummonerModelRegister.SHARK_OPEN : SummonerModelRegister.SHARK_CLOSE;
        RenderUtil.renderStandalone(modelKey, poseStack, bufferSource, FastColor.ARGB32.color((int) (alpha * 255), 255, 255, 255), packedLight);
    }
}
