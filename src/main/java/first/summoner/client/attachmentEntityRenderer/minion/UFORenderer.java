package first.summoner.client.attachmentEntityRenderer.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.entity.PathNode;
import first.lyra.common.particle.genericParticle.GenericParticleBuilder;
import first.summoner.common.minion.UFO;
import first.summoner.utils.ParticleHelper;
import first.summoner.utils.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.player.Player;

/**
 * UFO渲染器 - 占位模型+锥体拖尾。
 * 激光效果由粒子系统模拟，无需渲染器处理。
 */
public class UFORenderer extends AbstractAttachmentEntityRenderer<UFO> {

    @Override
    protected RenderContext<UFO> createContext(UFO entity) {
        return RenderContext.<UFO>builder()
                .model(new ModelConfig<UFO>()
                               .translateOffset(0, -0.1f, 0)
                               .alphaDistanceFactor(1.5f))
                .build();
    }

    @Override
    protected void render(UFO minion, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<UFO> context, float partialTick) {
        RenderUtil.renderDebugLabel(poseStack, bufferSource, minion);
        Minecraft minecraft = Minecraft.getInstance();
        int trailTimer = minion.getTrailTimer();
        if (trailTimer > 0 && !minecraft.isPaused()) {
            Player owner = minion.getOwner();
            ParticleHelper.create(owner.level())
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
    }
}
