package first.servantry.client.attachmentEntityRenderer.servant;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.ModelRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.api.client.render.renderConfig.ModelConfig;
import first.servantry.api.entity.PathNode;
import first.servantry.common.servant.EtherealStellarCore;
import first.servantry.register.ModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * 永夜之眼渲染器 - 环绕仆从本体 + 激光射线渲染。
 */
public class EtherealStellarCoreRenderer extends AbstractAttachmentEntityRenderer<EtherealStellarCore> {

    @Override
    protected RenderContext<EtherealStellarCore> createContext(EtherealStellarCore servant) {
        return RenderContext.<EtherealStellarCore>builder()
                .model(new ModelConfig<EtherealStellarCore>()
                               .scale(0.5f)
                               .translateOffset(-0.5f, -0.5f, -0.5f)
                               .alphaDistanceFactor(1.5f)
                               .visualNodeFunction((entity, partialTick, rawNode) -> {
                                   entity.getOwner().tickCount += 10;
                                   PathNode idleState = entity.getInterpolatedIdleState(partialTick);
                                   entity.getOwner().tickCount -= 10;
                                   return idleState;
                               })
                )
                .build();
    }

    @Override
    protected void renderEntity(EtherealStellarCore servant, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<EtherealStellarCore> config) {
        ModelRenderer.renderModel(ModelRegister.STARDUST_CELL, poseStack, bufferSource);
    }
}
