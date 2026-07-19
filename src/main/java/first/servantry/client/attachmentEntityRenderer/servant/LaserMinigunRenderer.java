package first.servantry.client.attachmentEntityRenderer.servant;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.ModelRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.api.client.render.renderConfig.ModelConfig;
import first.servantry.api.entity.PathNode;
import first.servantry.common.servant.LaserMinigun;
import first.servantry.register.ServantryModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * 激光机枪渲染器 - 占位模型+蓝色粒子拖尾。
 */
public class LaserMinigunRenderer extends AbstractAttachmentEntityRenderer<LaserMinigun> {

    @Override
    protected RenderContext<LaserMinigun> createContext(LaserMinigun entity) {
        return RenderContext.<LaserMinigun>builder()
                .model(new ModelConfig<LaserMinigun>()
                        .scale(0.5f)
                        .translateOffset(-0.5f, -0.5f, -0.5f)
                        .alphaDistanceFactor(1.5f)
                )
                .build();
    }

    @Override
    protected void render(LaserMinigun servant, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<LaserMinigun> context, float partialTick) {
        ModelRenderer.renderModel(ServantryModelRegister.TEST, poseStack, bufferSource);
    }
}
