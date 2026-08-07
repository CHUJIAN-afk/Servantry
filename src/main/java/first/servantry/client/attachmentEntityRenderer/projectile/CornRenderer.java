package first.servantry.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.geo.GeoSideloader;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.entity.PathNode;
import first.lyra.common.particle.genericParticle.GenericParticleBuilder;
import first.servantry.Servantry;
import first.servantry.common.projectile.Corn;
import first.servantry.utils.ParticleHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
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
                               .scale(2)
                               .translateOffset(0, -0.8f, 0.5f)
                               .rotationOffset(180, 0, 0)
                )
                .build();
    }

    @Override
    protected void render(Corn corn, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<Corn> context, float partialTick) {
        GeoSideloader.create(Servantry.rl("corn"))
                .render(poseStack, bufferSource, partialTick, LightTexture.FULL_BRIGHT);
        Minecraft minecraft = Minecraft.getInstance();
        if (!minecraft.isPaused()) {
            Player owner = corn.getOwner();
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
