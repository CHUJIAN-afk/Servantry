package first.servantry.client.attachmentEntityRenderer.projectile;

import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.api.client.render.renderConfig.DropletTrailConfig;
import first.servantry.common.projectile.ShatteredStellarCoreProjectile;

public class ShatteredStellarCoreProjectileRenderer extends AbstractAttachmentEntityRenderer<ShatteredStellarCoreProjectile> {

    @Override
    protected RenderContext<ShatteredStellarCoreProjectile> createContext(ShatteredStellarCoreProjectile laser) {
        return RenderContext.<ShatteredStellarCoreProjectile>builder()
                .trail(new DropletTrailConfig<ShatteredStellarCoreProjectile>()
                        .timer(laser.getTrailDuration())
                        .colorRGB(0x7926ff)
                        .historyLength(2)
                        .segmentsPerNode(2)
                        .maxRadius(0.04f)
                        .minRadiusRatio(0.5f)
                        .resolution(8))
                .build();
    }
}
