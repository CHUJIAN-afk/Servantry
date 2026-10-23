package first.summoner.client.attachmentEntityRenderer.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.ModelContext;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.model.LyraModelRenderer;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.common.minion.minion.Terraprism;
import first.summoner.register.SummonerModelRegister;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import software.bernie.geckolib.util.Color;

public class TerraprismRenderer extends AbstractAttachmentEntityRenderer<Terraprism> {

    private final TerraprismTrailContext trail = new TerraprismTrailContext();

    @Override
    protected RenderContext<Terraprism> createContext(Terraprism entity, PathNode visualNode, float partialTick, int packedLight) {
        return super.createContext(entity, visualNode, partialTick, packedLight)
                .packedLight(LightTexture.FULL_BRIGHT)
                .color(Color.ofOpaque(entity.getColor(partialTick)))
                .trail(trail.timer(entity.trailTimer))
                .model(new ModelContext().scale(1.5f).translateOffset(-1f, -1f, -0.5f).rotationOffset(0, 90, 45));
    }

    @Override
    protected void render(PoseStack poseStack, MultiBufferSource bufferSource) {
        Color color = context.color;
        LyraModelRenderer.json(SummonerModelRegister.TERRAPRISM)
                .color(color.argbInt())
                .light(context.packedLight)
                .render(poseStack, bufferSource);
    }
}
