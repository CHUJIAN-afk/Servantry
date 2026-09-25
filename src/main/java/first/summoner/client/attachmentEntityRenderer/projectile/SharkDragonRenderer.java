package first.summoner.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.ModelContext;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.model.LyraModelRenderer;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.common.projectile.SharkDragon;
import first.summoner.register.SummonerModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.ModelResourceLocation;

public class SharkDragonRenderer extends AbstractAttachmentEntityRenderer<SharkDragon> {

    @Override
    protected RenderContext<SharkDragon> createContext(SharkDragon laser, PathNode visualNode, float partialTick, int packedLight) {
        return super.createContext(laser, visualNode, partialTick, packedLight)
                .model(new ModelContext()
                        .rotationOffset(180, 0, 0)
                        .translateOffset(-0.5f, -0.5f, -0.5f)
                );
    }

    @Override
    protected void render(PoseStack poseStack, MultiBufferSource bufferSource) {
        ModelResourceLocation modelKey = context.entity.getTickCount() % 20 < 10 ? SummonerModelRegister.SHARK_OPEN : SummonerModelRegister.SHARK_CLOSE;
        LyraModelRenderer.json(modelKey)
                .color(context.color.argbInt())
                .light(context.packedLight)
                .render(poseStack, bufferSource);
    }
}
