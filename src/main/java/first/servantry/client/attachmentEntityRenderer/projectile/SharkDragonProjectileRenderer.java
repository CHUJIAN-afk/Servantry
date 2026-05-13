package first.servantry.client.attachmentEntityRenderer.projectile;

import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.api.client.render.renderConfig.DropletTrailConfig;
import first.servantry.common.projectile.SharkDragonProjectile;

public class SharkDragonProjectileRenderer extends AbstractAttachmentEntityRenderer<SharkDragonProjectile> {

    @Override
    protected RenderContext<SharkDragonProjectile> createContext(SharkDragonProjectile laser) {
        return RenderContext.<SharkDragonProjectile>builder()
                .trail(new DropletTrailConfig<SharkDragonProjectile>()
                        .timer(laser.getTrailDuration())
                        .colorRGB(0x4282ff)
                        .historyLength(4)
                        .segmentsPerNode(2)
                        .maxRadius(0.04f)
                        .minRadiusRatio(0.5f)
                        .resolution(16))
                .build();
    }

}
