package first.servantry.client.attachmentEntityRenderer.projectile;

import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.api.client.render.renderConfig.DropletTrailConfig;
import first.servantry.common.projectile.EternalNightLaserProjectile;

public class EternalNightLaserProjectileRenderer extends AbstractAttachmentEntityRenderer<EternalNightLaserProjectile> {

    @Override
    protected RenderContext<EternalNightLaserProjectile> createContext(EternalNightLaserProjectile laser) {
        return RenderContext.<EternalNightLaserProjectile>builder()
                .trail(new DropletTrailConfig<EternalNightLaserProjectile>()
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
