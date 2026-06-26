package first.servantry.client.attachmentEntityRenderer.projectile;

import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.api.client.render.renderConfig.DropletTrailConfig;
import first.servantry.common.projectile.GodFlameProjectile;

public class GodFlameProjectileRenderer extends AbstractAttachmentEntityRenderer<GodFlameProjectile> {

    @Override
    protected RenderContext<GodFlameProjectile> createContext(GodFlameProjectile projectile) {
        return RenderContext.<GodFlameProjectile>builder()
                .trail(new DropletTrailConfig<GodFlameProjectile>()
                               .timer(projectile.getTrailDuration())
                               .colorRGB(0x6f19d4)
                               .historyLength(5)
                               .segmentsPerNode(2)
                               .maxRadius(0.1f)
                               .minRadiusRatio(0.75f)
                               .resolution(4))
                .build();
    }
}
