package first.servantry.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.ModelRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.entity.PathNode;
import first.lyra.common.particle.genericParticle.GenericParticleBuilder;
import first.servantry.common.projectile.MiniNecroSpirit;
import first.servantry.register.ServantryModelRegister;
import first.servantry.utils.ParticleHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * 死魂灵射弹渲染器 - 占位模型+紫色粒子拖尾。
 */
public class NecroSpiritProjectileRenderer extends AbstractAttachmentEntityRenderer<MiniNecroSpirit> {

    @Override
    protected RenderContext<MiniNecroSpirit> createContext(MiniNecroSpirit entity) {
        return RenderContext.<MiniNecroSpirit>builder()
                .model(new ModelConfig<MiniNecroSpirit>()
                        .scale(0.3f)
                        .translateOffset(-0.5f, -0.5f, -0.5f)
                        .alphaDistanceFactor(1.0f)
                )
                .build();
    }

    @Override
    protected void render(MiniNecroSpirit entity, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<MiniNecroSpirit> context, float partialTick) {
        ModelRenderer.renderModel(ServantryModelRegister.TEST, poseStack, bufferSource);
        if (!Minecraft.getInstance().isPaused()) {
            ParticleHelper.create(entity.getOwner().level())
                    .generic(GenericParticleBuilder.create()
                                     .centerColor(0xff1200)
                                     .edgeColor(0xd40f00)
                                     .lifetime(4)
                                     .lifetimeRandom(3)
                                     .spin(0.25f)
                                     .spinRandom(0.3F)
                                     .friction(0.75F)
                                     .scale(0.025f)
                                     .scaleRandom(0.008f)
                    )
                    .pos(visualNode.pos())
                    .offset(0.08)
                    .emit();
        }
    }
}
