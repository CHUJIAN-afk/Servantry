package first.servantry.client.attachmentEntityRenderer.servant;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.ModelRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.api.client.render.renderConfig.ModelConfig;
import first.servantry.api.common.particle.genericParticle.GenericParticleBuilder;
import first.servantry.api.entity.PathNode;
import first.servantry.common.servant.NecroSpirit;
import first.servantry.register.ServantryModelRegister;
import first.servantry.utils.ParticleHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * 死魂灵巫术单元渲染器 - 占位模型+紫色粒子拖尾。
 */
public class NecroSpiritRenderer extends AbstractAttachmentEntityRenderer<NecroSpirit> {

    @Override
    protected RenderContext<NecroSpirit> createContext(NecroSpirit entity) {
        return RenderContext.<NecroSpirit>builder()
                .model(new ModelConfig<NecroSpirit>()
                        .scale(0.5f)
                        .translateOffset(-0.5f, -0.5f, -0.5f)
                        .alphaDistanceFactor(1.5f)
                )
                .build();
    }

    @Override
    protected void render(NecroSpirit servant, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<NecroSpirit> context, float partialTick) {
        if (!Minecraft.getInstance().isPaused()) {
            ParticleHelper.create(servant.getOwner().level())
                    .generic(GenericParticleBuilder.create()
                                     .centerColor(0xff1200)
                                     .edgeColor(0xd40f00)
                                     .lifetime(5)
                                     .lifetimeRandom(4)
                                     .spin(0.3f)
                                     .spinRandom(0.5F)
                                     .friction(0.75F)
                                     .scale(0.03f)
                                     .scaleRandom(0.005f)
                    )
                    .pos(visualNode.pos())
                    .offset(0.12)
                    .emit();
        }
        ModelRenderer.renderModel(ServantryModelRegister.TEST, poseStack, bufferSource);
    }
}
