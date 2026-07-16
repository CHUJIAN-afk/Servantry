package first.servantry.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.client.dynamicLight.DynamicLightDispatcher;
import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.ModelRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.api.client.render.renderConfig.ConeTrailConfig;
import first.servantry.api.client.render.renderConfig.ModelConfig;
import first.servantry.api.entity.PathNode;
import first.servantry.common.projectile.ShatteredStellarCore;
import first.servantry.register.ServantryModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;

public class ShatteredStellarCoreRenderer extends AbstractAttachmentEntityRenderer<ShatteredStellarCore> {

    @Override
    protected RenderContext<ShatteredStellarCore> createContext(ShatteredStellarCore projectile) {
        return RenderContext.<ShatteredStellarCore>builder()
                .trail(new ConeTrailConfig<ShatteredStellarCore>()
                               .timer(15)
                               .colorRGB(0x8AE0FF)
                               .historyLength(4)
                               .maxRadius(0.075f)
                               .minRadiusRatio(0.025f)
                               .resolution(4)
                               .fadeOut(progress -> (float) Math.pow(Math.max(0.0f, 1.0f - progress), 2.0)))
                .model(new ModelConfig<ShatteredStellarCore>()
                               .scale(0.15f)
                               .translateOffset(-0.5f, -0.5f, -0.2f)
                               .rotationOffset(0, 0, 45))
                .build();
    }

    @Override
    protected void render(ShatteredStellarCore projectile, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<ShatteredStellarCore> context, float partialTick) {
        ModelRenderer.renderModel(ServantryModelRegister.STARDUST_CELL, poseStack, bufferSource);
        DynamicLightDispatcher.addLightSources(visualNode.pos(), 8);
    }
}
