package first.servantry.client.attachmentEntityRenderer;

import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.common.projectile.LaserProjectile;

public class LaserProjectileRenderer extends AbstractAttachmentEntityRenderer<LaserProjectile> {

    @Override
    protected RenderContext<LaserProjectile> createContext(LaserProjectile laser) {
        return RenderContext.droplet(laser.getTrailTimer(), 0x8AE0FF, 0.025f);
    }

}
