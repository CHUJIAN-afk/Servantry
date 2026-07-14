package first.servantry.client.attachmentEntityRenderer.projectile;

import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.api.client.render.renderConfig.ConeTrailConfig;
import first.servantry.common.projectile.RainbowCrystal;
import net.minecraft.client.Minecraft;

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
}
