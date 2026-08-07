package first.servantry.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.dynamicLight.DynamicLightDispatcher;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.trail.ConeTrailConfig;
import first.lyra.common.entity.PathNode;
import first.servantry.common.projectile.MiniRainbowCrystal;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;

public class RainbowCrystalRenderer extends AbstractAttachmentEntityRenderer<MiniRainbowCrystal> {

    @Override
    protected RenderContext<MiniRainbowCrystal> createContext(MiniRainbowCrystal crystal) {
        float partialTick = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
        return RenderContext.<MiniRainbowCrystal>builder()
                .trail(new ConeTrailConfig<MiniRainbowCrystal>()
                               .timer(15)
                               .colorRGB(crystal.getColor(partialTick))
                               .historyLength(16)
                               .segmentsPerNode(2)
                               .maxRadius((crystal.getLife() + partialTick) * 0.025f)
                               .resolution(4))
                .build();
    }

    @Override
    protected void render(MiniRainbowCrystal entity, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<MiniRainbowCrystal> context, float partialTick) {
        DynamicLightDispatcher.addLightSources(visualNode.pos(), 8);
    }
}
