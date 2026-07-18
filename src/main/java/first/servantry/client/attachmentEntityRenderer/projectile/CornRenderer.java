package first.servantry.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.ModelRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.api.client.render.renderConfig.ModelConfig;
import first.servantry.api.common.particle.genericParticle.GenericParticleBuilder;
import first.servantry.api.entity.PathNode;
import first.servantry.common.projectile.Corn;
import first.servantry.register.ServantryModelRegister;
import first.servantry.utils.ParticleHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.player.Player;

/**
 * 玉米炮射弹渲染器 - 占位模型+粒子拖尾。
 */
public class CornRenderer extends AbstractAttachmentEntityRenderer<Corn> {

    @Override
    protected RenderContext<Corn> createContext(Corn entity) {
        return RenderContext.<Corn>builder()
                .model(new ModelConfig<Corn>()
                        .scale(0.3f)
                        .translateOffset(-0.5f, -0.5f, -0.5f)
                        .alphaDistanceFactor(1.0f)
                )
                .build();
    }

    @Override
    protected void render(Corn entity, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<Corn> context, float partialTick) {
        ModelRenderer.renderModel(ServantryModelRegister.TEST, poseStack, bufferSource);
        if (!Minecraft.getInstance().isPaused()) {
            Player owner = entity.getOwner();
            ParticleHelper.create(owner.level())
                    .generic(GenericParticleBuilder.create()
                            .centerColor(0xFFDD00)
                            .edgeColor(0xFF8800)
                            .lifetime(5)
                            .lifetimeRandom(5)
                            .spin(0.2f)
                            .spinRandom(0.3F)
                            .friction(0.75F)
                            .scale(0.03f)
                            .scaleRandom(0.01f)
                    )
                    .pos(visualNode.pos())
                    .offset(0.1)
                    .emit();
        }
    }
}
