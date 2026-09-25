package first.summoner.client.attachmentEntityRenderer.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityGeoRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.Summoner;
import first.summoner.common.minion.minion.Hornet;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import software.bernie.geckolib.util.Color;

/**
 * 黄蜂渲染器 - geo 模型 {@code entity/summon/hornet_baby}，常亮渲染。
 */
public class HornetRenderer extends AbstractAttachmentEntityGeoRenderer<Hornet> {

    public HornetRenderer() {
        super(Summoner.rl("entity/summon/hornet_baby"));
    }

    @Override
    protected GeoRenderContext createContext(Hornet hornet, PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, int packedLight, PathNode visualNode) {
        RenderContext<Hornet> renderContext = RenderContext.<Hornet>builder()
                .model(new ModelConfig<Hornet>()
                               .scale(0.6F)
                               .translateOffset(0, -0.2F, 0.3F)
                               .rotationOffset(180, -35, 0)
                               .alphaDistanceFactor(1.5F))
                .build();
        return new GeoRenderContext(hornet, visualNode, Color.WHITE, partialTick, LightTexture.FULL_BRIGHT, renderContext);
    }
}
