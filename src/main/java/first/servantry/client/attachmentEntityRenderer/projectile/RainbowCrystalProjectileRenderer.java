package first.servantry.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.client.dynamicLight.DynamicLightDispatcher;
import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.api.client.render.renderConfig.ConeTrailConfig;
import first.servantry.api.entity.PathNode;
import first.servantry.common.projectile.RainbowCrystal;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;

public class RainbowCrystalProjectileRenderer extends AbstractAttachmentEntityRenderer<RainbowCrystal> {

    @Override
    protected RenderContext<RainbowCrystal> createContext(RainbowCrystal crystal) {
        float partialTick = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
        return RenderContext.<RainbowCrystal>builder()
                .trail(new ConeTrailConfig<RainbowCrystal>()
                               .timer(15)
                               .colorRGB(crystal.getColor(partialTick))
                               .historyLength(16)
                               .segmentsPerNode(2)
                               .maxRadius((crystal.getLife() + partialTick) * 0.025f)
                               .resolution(4))
                .build();
    }

    @Override
    protected void render(RainbowCrystal entity, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<RainbowCrystal> context, float partialTick) {
        DynamicLightDispatcher.addLightSources(visualNode.pos(), 8);
    }
}
