package first.summoner.client.attachmentEntityRenderer.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.api.LyraAPI;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.ModelContext;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.model.LyraModelRenderer;
import first.lyra.client.render.trail.RibbonTrailContext;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.common.minion.minion.Terraprism;
import first.summoner.register.SummonerModelRegister;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import software.bernie.geckolib.util.Color;

public class TerraprismRenderer extends AbstractAttachmentEntityRenderer<Terraprism> {

    @Override
    protected RenderContext<Terraprism> createContext(Terraprism entity, PathNode visualNode, float partialTick, int packedLight) {
        return super.createContext(entity, visualNode, partialTick, packedLight)
                .packedLight(LightTexture.FULL_BRIGHT)
                .color(Color.ofOpaque(entity.getColor(partialTick)))
                .trail(new RibbonTrailContext<Terraprism>()
                        .upOffset(1.015f)
                        .timer(entity.trailTimer)
                        .colorRGB(entity.getColor(partialTick))
                        .segmentsPerNode(4)
                        .historyLength(6))
                .model(new ModelContext().scale(1.5f).translateOffset(-0.5f, -0.5f, -0.5f).rotationOffset(0, 90, 45));
    }

    @Override
    protected void render(PoseStack poseStack, MultiBufferSource bufferSource) {
        LyraModelRenderer.json(SummonerModelRegister.TERRAPRISM)
                .color(context.color.argbInt())
                .light(context.packedLight)
                .render(poseStack, bufferSource);
        LyraAPI.light(context.visualNode.pos(), 8);
    }
}
