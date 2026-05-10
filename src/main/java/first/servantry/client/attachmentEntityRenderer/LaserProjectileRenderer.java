package first.servantry.client.attachmentEntityRenderer;

import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.common.projectile.LaserProjectile;

public class LaserProjectileRenderer extends AbstractAttachmentEntityRenderer<LaserProjectile> {

    @Override
    protected RenderContext<LaserProjectile> createContext(LaserProjectile laser) {
        return RenderContext.<LaserProjectile>droplet(laser.getTrailDuration(), 0xFF3333, 0.05f)
                .trailHistoryLength(4)
                .trailSegmentsPerNode(2);
    }

}
