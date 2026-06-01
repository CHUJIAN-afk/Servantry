package first.servantry.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.ModelRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.api.client.render.renderConfig.ModelConfig;
import first.servantry.api.client.render.renderConfig.RibbonTrailConfig;
import first.servantry.api.entity.PathNode;
import first.servantry.common.particle.GenericParticleBuilder;
import first.servantry.common.projectile.ZenithProjectile;
import first.servantry.register.ModelRegister;
import first.servantry.utils.ParticleHelper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;

public class ZenithProjectileRenderer extends AbstractAttachmentEntityRenderer<ZenithProjectile> {

    @Override
    protected RenderContext<ZenithProjectile> createContext(ZenithProjectile zenith) {
        return RenderContext.<ZenithProjectile>builder()
                .trail(new RibbonTrailConfig<ZenithProjectile>()
                        .timer(15)
                        .colorRGB(0xffffff)
                        .historyLength(4)
                        .width(0.7075f)
                        .diamondSize(0.15f)
                        .tipAlphaBoost((s, progress) -> progress < 0.3f ? Mth.lerp(progress / 0.3f, 2.5f, 1.0f) : 1.0f)
                        .tipBrightnessBoost((s, progress) -> progress < 0.25f ? Mth.lerp(progress / 0.25f, 1.5f, 1.0f) : 1.0f)
                )
                .model(new ModelConfig<ZenithProjectile>()
                        .translateOffset(-0.5f, -0.5f, -0.5f)
                        .rotationOffset(0, 90, 45)
                )
                .build();
    }

    @Override
    protected void renderEntity(ZenithProjectile zenithProjectile, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<ZenithProjectile> config) {
        ArrayList<PathNode> pathNodes = zenithProjectile.getHistoryNodes();
        Player owner = zenithProjectile.getOwner();
        if (pathNodes.size() > 3) {
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
                        .offset(0.015)
                        .velocity(velocity)
                        .count(1)
                        .speed(2)
                        .spread(0.05)
                        .emit();
            }
        }
        ModelRenderer.renderModel(ModelRegister.TERRAPRISM, poseStack, bufferSource);
    }
}
