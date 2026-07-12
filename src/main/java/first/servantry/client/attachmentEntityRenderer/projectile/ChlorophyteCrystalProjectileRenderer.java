package first.servantry.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.ModelRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.api.client.render.renderConfig.ConeTrailConfig;
import first.servantry.api.client.render.renderConfig.ModelConfig;
import first.servantry.api.entity.PathNode;
import first.servantry.common.projectile.ChlorophyteCrystalProjectile;
import first.servantry.register.ServantryModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;

public class ChlorophyteCrystalProjectileRenderer extends AbstractAttachmentEntityRenderer<ChlorophyteCrystalProjectile> {

    @Override
    protected RenderContext<ChlorophyteCrystalProjectile> createContext(ChlorophyteCrystalProjectile crystal) {
        return RenderContext.<ChlorophyteCrystalProjectile>builder()
                .model(new ModelConfig<ChlorophyteCrystalProjectile>()
                        .rotationOffset(0, 90, 0)
                        .scale(0.5f)
                        .translateOffset(-0.5f, -0.5f, -0.5f)
                )
                .trail(new ConeTrailConfig<ChlorophyteCrystalProjectile>()
                        .timer(crystal.getTrailDuration())
                        .colorRGB(0x1bff10)
                        .historyLength(4)
                        .segmentsPerNode(2)
                        .maxRadius(0.1f)
                        .minRadiusRatio(0.75f)
                        .resolution(4))
                .build();
    }

    @Override
    protected void renderEntity(ChlorophyteCrystalProjectile entity, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<ChlorophyteCrystalProjectile> config) {
        ModelRenderer.renderModel(ServantryModelRegister.CHLOROPHYTE_CRYSTAL, poseStack, bufferSource);
    }
}
