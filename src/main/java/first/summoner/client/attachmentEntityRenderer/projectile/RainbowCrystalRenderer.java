package first.summoner.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.dynamicLight.DynamicLightDispatcher;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.trail.ConeTrailContext;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.common.projectile.MiniRainbowCrystal;
import net.minecraft.client.renderer.MultiBufferSource;

public class RainbowCrystalRenderer extends AbstractAttachmentEntityRenderer<MiniRainbowCrystal> {

    @Override
    protected RenderContext<MiniRainbowCrystal> createContext(MiniRainbowCrystal crystal, PathNode visualNode, float partialTick, int packedLight) {
        return super.createContext(crystal, visualNode, partialTick, packedLight)
                .trail(new ConeTrailContext<MiniRainbowCrystal>()
                               .maxRadius((crystal.getLife() + partialTick) * 0.025f)
                               .resolution(4)
                               .timer(15)
                               .colorRGB(crystal.getColor(partialTick))
                               .historyLength(16)
                               .segmentsPerNode(2));
    }

    @Override
    protected void render(PoseStack poseStack, MultiBufferSource bufferSource) {
        DynamicLightDispatcher.addLightSources(context.visualNode.pos(), 8);
    }
}
