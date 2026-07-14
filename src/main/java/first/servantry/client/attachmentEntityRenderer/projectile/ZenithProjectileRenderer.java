package first.servantry.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.ModelRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.api.client.render.renderConfig.ModelConfig;
import first.servantry.api.client.render.renderConfig.RibbonTrailConfig;
import first.servantry.api.common.particle.genericParticle.GenericParticleBuilder;
import first.servantry.api.entity.PathNode;
import first.servantry.common.projectile.Zenith;
import first.servantry.register.ServantryModelRegister;
import first.servantry.utils.ParticleHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;

public class ZenithProjectileRenderer extends AbstractAttachmentEntityRenderer<Zenith> {

    @Override
    protected RenderContext<Zenith> createContext(Zenith zenith) {
        return RenderContext.<Zenith>builder()
                .trail(new RibbonTrailConfig<Zenith>()
                               .timer(0)
                               .colorRGB(0xffffff)
                               .historyLength(2)
                               .upOffset(1.32575f)
                )
                .model(new ModelConfig<Zenith>()
                               .scale(2)
                               .translateOffset(-0.5f, -0.5f, -0.5f)
                               .rotationOffset(0, 90, 45))
                .build();
    }

    @Override
    protected void render(Zenith zenith, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<Zenith> context) {
        ArrayList<PathNode> pathNodes = zenith.getHistoryNodes();
        Player owner = zenith.getOwner();
        if (pathNodes.size() > 3 && !Minecraft.getInstance().isPaused()) {
            Vec3 velocity = visualNode.pos().subtract(pathNodes.get(2).pos());
            if (velocity.length() > 1) {
                ParticleHelper.create(owner.level())
                        .generic(GenericParticleBuilder.create()
                                         .color(0xffffff)
                                         .edgeColor(0xb7b7b7)
                                         .colorRandom(0.2F, 0.2F, 0.0F)
                                         .lifetime(5)
                                         .lifetimeRandom(20)
                                         .spin(0.5f)
                                         .spinRandom(0.5F)
                                         .friction(0.75F)
                                         .scale(0.01f)
                                         .scaleRandom(0.0015f)
                        )
                        .pos(visualNode.pos())
                        .offset(0.15)
                        .velocity(velocity.normalize())
                        .count(1)
                        .speed(0.25)
                        .spread(0.05)
                        .emit();
            }
        }
        ModelRenderer.renderModel(ServantryModelRegister.TERRAPRISM, poseStack, bufferSource);
    }
}
