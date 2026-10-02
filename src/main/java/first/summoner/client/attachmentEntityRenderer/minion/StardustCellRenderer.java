package first.summoner.client.attachmentEntityRenderer.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.ModelContext;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.RenderUtil;
import first.lyra.client.render.model.LyraModelRenderer;
import first.lyra.common.attachmentEntity.PathNode;
import first.lyra.common.particle.genericParticle.GenericParticleBuilder;
import first.summoner.common.minion.minion.StardustCell;
import first.summoner.register.SummonerModelRegister;
import first.summoner.utils.DynamicLightUtil;
import first.summoner.utils.ParticleHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * 星尘细胞渲染器。
 * <p>
 * 使用圆锥拖尾，渲染旋转的星尘细胞模型。
 * </p>
 */
public class StardustCellRenderer extends AbstractAttachmentEntityRenderer<StardustCell> {

    @Override
    protected RenderContext<StardustCell> createContext(StardustCell minion, PathNode visualNode, float partialTick, int packedLight) {
        float rotation = minion.getTickCount() + partialTick;
        return super.createContext(minion, visualNode, partialTick, packedLight)
                .model(new ModelContext()
                               .scale(0.5f)
                               .translateOffset(-0.5f, -0.5f, -0.5f)
                               .rotationOffset(rotation, rotation, rotation)
                               .alphaDistanceFactor(2f));
    }

    @Override
    protected void render(PoseStack poseStack, MultiBufferSource bufferSource) {
        int trailTimer = context.entity.getTrailTimer();
        if (trailTimer > 0 && !Minecraft.getInstance().isPaused()) {
            ParticleHelper.create(context.entity.getLevel())
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
                    .pos(context.visualNode.pos())
                    .offset(0.15)
                    .count(10)
                    .emit();
        }
        LyraModelRenderer.json(SummonerModelRegister.STARDUST_CELL)
                .color(context.color.argbInt())
                .light(RenderUtil.FULL_LIGHT)
                .render(poseStack, bufferSource);
        DynamicLightUtil.addLight(context.visualNode.pos(), 8);
    }
}
