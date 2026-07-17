package first.servantry.client.attachmentEntityRenderer.servant;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.ModelRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.api.client.render.renderConfig.ModelConfig;
import first.servantry.api.common.particle.genericParticle.GenericParticleBuilder;
import first.servantry.api.entity.PathNode;
import first.servantry.common.servant.UFO;
import first.servantry.register.ServantryModelRegister;
import first.servantry.utils.ParticleHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * UFO渲染器 - 占位模型+锥体拖尾。
 * 激光效果由粒子系统模拟，无需渲染器处理。
 */
public class UFORenderer extends AbstractAttachmentEntityRenderer<UFO> {

    @Override
    protected RenderContext<UFO> createContext(UFO entity) {
        return RenderContext.<UFO>builder()
                .model(new ModelConfig<UFO>()
                               .scale(0.5f)
                               .translateOffset(-0.5f, -0.5f, -0.5f)
                               .alphaDistanceFactor(1.5f))
                .build();
    }

    @Override
    protected void render(UFO servant, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<UFO> context, float partialTick) {
        if (servant.getTrailTimer() > 0 && !Minecraft.getInstance().isPaused()) {
            int trailTimer = servant.getTrailTimer();
            ParticleHelper.create(servant.getOwner().level())
                    .generic(GenericParticleBuilder.create()
                                     .centerColor(0x46f7ff)
                                     .edgeColor(0x3dd6dd)
                                     .lifetime(7 - trailTimer)
                                     .lifetimeRandom(4)
                                     .spin(trailTimer * 0.25f)
                                     .spinRandom(0.5F)
                                     .friction(0.75F)
                                     .scale(0.035f)
                                     .scaleRandom(0.005f)
                    )
                    .pos(visualNode.pos())
                    .offset(0.15)
                    .count(5)
                    .emit();
        }
        ModelRenderer.renderModel(ServantryModelRegister.TEST, poseStack, bufferSource);
    }
}
