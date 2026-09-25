package first.summoner.client.attachmentEntityRenderer.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityGeoRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.Summoner;
import first.summoner.common.minion.minion.SnowFlinx;
import net.minecraft.client.renderer.MultiBufferSource;
import software.bernie.geckolib.util.Color;

/**
 * 小雪怪渲染器 - geo 模型 {@code entity/summon/summon_snow_flinx}。
 */
public class SnowFlinxRenderer extends AbstractAttachmentEntityGeoRenderer<SnowFlinx> {

    public SnowFlinxRenderer() {
        super(Summoner.rl("entity/summon/summon_snow_flinx"));
    }

    @Override
    protected GeoRenderContext createContext(SnowFlinx flinx, PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, int packedLight, PathNode visualNode) {
        RenderContext<SnowFlinx> renderContext = RenderContext.<SnowFlinx>builder()
                .model(new ModelConfig<SnowFlinx>()
                               .rotationOffset(90, 0, 0)
                               .alphaDistanceFactor(1.5F))
                .build();
        return new GeoRenderContext(flinx, visualNode, Color.WHITE, partialTick, packedLight, renderContext);
    }
}
