package first.servantry.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.PathNode;
import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.ModelRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.api.client.render.renderConfig.ModelConfig;
import first.servantry.common.projectile.SharkDragonProjectile;
import first.servantry.register.ModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;

public class SharkDragonProjectileRenderer extends AbstractAttachmentEntityRenderer<SharkDragonProjectile> {

    @Override
    protected RenderContext<SharkDragonProjectile> createContext(SharkDragonProjectile laser) {
        return RenderContext.<SharkDragonProjectile>builder()
                .model(new ModelConfig<SharkDragonProjectile>()
                        .rotationOffset(180, 0, 0)
                        .translateOffset(-0.5f, -0.5f, -0.5f)
                )
                .build();
    }

    @Override
    protected void renderEntity(SharkDragonProjectile entity, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<SharkDragonProjectile> config) {
        if (entity.getOwner().tickCount % 20 < 10) {
            ModelRenderer.renderModel(ModelRegister.SHARK_OPEN, poseStack, bufferSource);
        } else {
            ModelRenderer.renderModel(ModelRegister.SHARK_CLOSE, poseStack, bufferSource);
        }
    }
}
