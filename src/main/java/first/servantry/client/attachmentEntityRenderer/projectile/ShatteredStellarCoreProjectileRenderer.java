package first.servantry.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.ModelRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.api.client.render.renderConfig.ConeTrailConfig;
import first.servantry.api.client.render.renderConfig.ModelConfig;
import first.servantry.api.entity.PathNode;
import first.servantry.common.projectile.ShatteredStellarCoreProjectile;
import first.servantry.register.ModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;

public class ShatteredStellarCoreProjectileRenderer extends AbstractAttachmentEntityRenderer<ShatteredStellarCoreProjectile> {

    @Override
    protected RenderContext<ShatteredStellarCoreProjectile> createContext(ShatteredStellarCoreProjectile projectile) {
        return RenderContext.<ShatteredStellarCoreProjectile>builder()
                .trail(new ConeTrailConfig<ShatteredStellarCoreProjectile>()
                               .timer(15)
                               .colorRGB(0x8AE0FF)
                               .historyLength(5)
                               .segmentsPerNode(16)
                               .maxRadius(0.075f)
                               .minRadiusRatio(0.5f)
                               .resolution(4)
                               .fadeOut(progress -> (float) Math.pow(Math.max(0.0f, 1.0f - progress), 2.0)))
                .model(new ModelConfig<ShatteredStellarCoreProjectile>()
                               .scale(0.15f)
                               .translateOffset(-0.5f, -0.5f, -0.2f)
                               .rotationOffset(0, 0, 45))
                .build();
    }

    @Override
    protected void renderEntity(ShatteredStellarCoreProjectile projectile, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<ShatteredStellarCoreProjectile> config) {
        ModelRenderer.renderModel(ModelRegister.STARDUST_CELL, poseStack, bufferSource);
    }
}
