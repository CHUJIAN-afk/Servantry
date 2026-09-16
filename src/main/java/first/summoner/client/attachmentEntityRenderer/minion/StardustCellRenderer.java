package first.summoner.client.attachmentEntityRenderer.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.dynamicLight.DynamicLightDispatcher;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.RenderUtil;
import first.lyra.client.render.model.LyraModelRenderer;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.attachmentEntity.PathNode;
import first.lyra.common.particle.genericParticle.GenericParticleBuilder;
import first.summoner.common.minion.minion.StardustCell;
import first.summoner.register.SummonerModelRegister;
import first.summoner.utils.ParticleHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FastColor;

/**
 * 星尘细胞渲染器。
 * <p>
 * 使用圆锥拖尾，渲染旋转的星尘细胞模型。
 * </p>
 */
public class StardustCellRenderer extends AbstractAttachmentEntityRenderer<StardustCell> {

    @Override
    protected RenderContext<StardustCell> createContext(StardustCell minion, float partialTick) {
        float rotation = minion.getTickCount() + partialTick;
        return RenderContext.<StardustCell>builder()
                .model(new ModelConfig<StardustCell>()
                               .scale(0.5f)
                               .translateOffset(-0.5f, -0.5f, -0.5f)
                               .rotationOffset(rotation, rotation, rotation)
                               .alphaDistanceFactor(2f))
                .build();
    }

    @Override
    protected void render(StardustCell minion, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<StardustCell> context, float partialTick, int packedLight, float alpha) {
        int trailTimer = minion.getTrailTimer();
        if (trailTimer > 0 && !Minecraft.getInstance().isPaused()) {
            ParticleHelper.create(minion.getLevel())
                    .generic(GenericParticleBuilder.create()
                                     .centerColor(0x2fb2e1)
                                     .edgeColor(0x33ccff)
                                     .lifetime(2)
                                     .lifetimeRandom(2)
                                     .spin(trailTimer * 0.25f)
                                     .spinRandom(0.5F)
                                     .friction(0.75F)
                                     .scale(0.01f)
                                     .scaleRandom(0.005f)
                    )
                    .pos(visualNode.pos())
                    .offset(0.15)
                    .count(10)
                    .emit();
        }
        LyraModelRenderer.json(SummonerModelRegister.STARDUST_CELL)
                .color(FastColor.ARGB32.color((int) (alpha * 255), 255, 255, 255))
                .light(RenderUtil.FULL_LIGHT)
                .render(poseStack, bufferSource);
        DynamicLightDispatcher.addLightSources(visualNode.pos(), 8);
    }
}
