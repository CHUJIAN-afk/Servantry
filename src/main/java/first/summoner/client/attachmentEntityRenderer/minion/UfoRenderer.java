package first.summoner.client.attachmentEntityRenderer.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.ModelContext;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.geo.AbstractAttachmentEntityGeoRenderer;
import first.lyra.common.attachmentEntity.PathNode;
import first.lyra.common.particle.genericParticle.GenericParticleBuilder;
import first.summoner.Summoner;
import first.summoner.common.minion.minion.UFO;
import first.summoner.utils.ParticleHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * UFO 渲染器：geo 模型 {@code entity/summon/ufo}，常亮渲染。
 */
public class UfoRenderer extends AbstractAttachmentEntityGeoRenderer<UFO> {

    public UfoRenderer() {
        super(Summoner.rl("entity/summon/ufo"));
    }

    @Override
    protected RenderContext<UFO> createContext(UFO ufo, PathNode visualNode, float partialTick, int packedLight) {
        return super.createContext(ufo, visualNode, partialTick, packedLight)
                .packedLight(LightTexture.FULL_BRIGHT)
                .model(new ModelContext()
                               .scale(0.8f)
                               .translateOffset(0, -0.125f, 0)
                               .alphaDistanceFactor(1.5f));
    }

    @Override
    protected void render(PoseStack poseStack, MultiBufferSource bufferSource) {
        if (context.entity.getTrailTimer() > 0 && !Minecraft.getInstance().isPaused()) {
            ParticleHelper.create(context.entity.getLevel())
                    .generic(GenericParticleBuilder.create()
                                     .centerColor(0x46f7ff)
                                     .edgeColor(0x3dd6dd)
                                     .lifetime(3)
                                     .lifetimeRandom(2)
                                     .scale(0.015f)
                                     .scaleRandom(0.005f)
                    )
                    .pos(context.visualNode.pos())
                    .offset(0.2)
                    .count(10)
                    .emit();
        }
    }
}
