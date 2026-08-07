package first.servantry.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.dynamicLight.DynamicLightDispatcher;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.ModelRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.entity.PathNode;
import first.lyra.common.particle.genericParticle.GenericParticleBuilder;
import first.servantry.common.projectile.ImpFireball;
import first.servantry.register.ServantryModelRegister;
import first.servantry.utils.ParticleHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.player.Player;

/**
 * 小鬼火球渲染器 - 暂用占位模型。
 */
public class ImpFireballRenderer extends AbstractAttachmentEntityRenderer<ImpFireball> {

    @Override
    protected RenderContext<ImpFireball> createContext(ImpFireball entity) {
        return RenderContext.<ImpFireball>builder()
                .model(new ModelConfig<ImpFireball>()
                        .scale(0.3f)
                        .translateOffset(-0.5f, -0.5f, -0.5f)
                        .alphaDistanceFactor(1.0f)
                )
                .build();
    }

    @Override
    protected void render(ImpFireball entity, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<ImpFireball> context, float partialTick) {
        ModelRenderer.renderModel(ServantryModelRegister.TEST, poseStack, bufferSource);
        if (!Minecraft.getInstance().isPaused()) {
            Player owner = entity.getOwner();
            ParticleHelper.create(owner.level())
                    .generic(GenericParticleBuilder.create()
                                     .centerColor(0xe1c316)
                                     .edgeColor(0xff7506)
                                     .lifetime(5)
                                     .lifetimeRandom(5)
                                     .spin(0.25f)
                                     .spinRandom(0.5F)
                                     .friction(0.75F)
                                     .scale(0.035f)
                                     .scaleRandom(0.005f)
                    )
                    .pos(visualNode.pos())
                    .offset(0.1)
                    .emit();
        }
        DynamicLightDispatcher.addLightSources(visualNode.pos(), 8);
    }
}
