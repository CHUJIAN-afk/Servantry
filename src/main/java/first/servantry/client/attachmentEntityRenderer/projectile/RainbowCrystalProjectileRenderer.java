package first.servantry.client.attachmentEntityRenderer.projectile;

import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.api.client.render.renderConfig.ConeTrailConfig;
import first.servantry.common.projectile.RainbowCrystalProjectile;
import net.minecraft.client.Minecraft;

public class RainbowCrystalProjectileRenderer extends AbstractAttachmentEntityRenderer<RainbowCrystalProjectile> {

    @Override
    protected RenderContext<RainbowCrystalProjectile> createContext(RainbowCrystalProjectile crystal) {
        float partialTick = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
        return RenderContext.<RainbowCrystalProjectile>builder()
                .trail(new ConeTrailConfig<RainbowCrystalProjectile>()
                               .timer(15)
                               .colorRGB(crystal.getColor(partialTick))
                               .historyLength(16)
                               .segmentsPerNode(2)
                               .maxRadius(0.25f)
                               .minRadiusRatio(0.2f)
                               .resolution(4))
                .build();
    }
}
