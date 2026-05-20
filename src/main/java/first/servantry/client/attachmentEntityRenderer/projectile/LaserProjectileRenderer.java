package first.servantry.client.attachmentEntityRenderer.projectile;

import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.api.client.render.renderConfig.DropletTrailConfig;
import first.servantry.common.projectile.LaserProjectile;

public class LaserProjectileRenderer extends AbstractAttachmentEntityRenderer<LaserProjectile> {

    @Override
    protected RenderContext<LaserProjectile> createContext(LaserProjectile laser) {
        return RenderContext.<LaserProjectile>builder()
                .trail(new DropletTrailConfig<LaserProjectile>()
                        .timer(laser.getTrailDuration())
                        .colorRGB(0xFF3333)
                        .historyLength(4)
                        .segmentsPerNode(2)
                        .maxRadius(0.04f)
                        .minRadiusRatio(0.5f)
                        .resolution(16))
                .build();
    }
}
