package first.summoner.client.attachmentEntityRenderer.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityGeoRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.Summoner;
import first.summoner.common.minion.minion.RuinRelic;
import net.minecraft.client.renderer.MultiBufferSource;
import software.bernie.geckolib.util.Color;

/**
 * 禁戒遗迹仆从渲染器 - geo 模型 {@code entity/summon/ruin_relic}，附带呼吸缩放脉动。
 */
public class RuinRelicRenderer extends AbstractAttachmentEntityGeoRenderer<RuinRelic> {

    public RuinRelicRenderer() {
        super(Summoner.rl("entity/summon/ruin_relic"));
    }

    @Override
    protected GeoRenderContext createContext(RuinRelic relic, PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, int packedLight, PathNode visualNode) {
        RenderContext<RuinRelic> renderContext = RenderContext.<RuinRelic>builder()
                .model(new ModelConfig<RuinRelic>()
                               .translateOffset(0, -0.35f, 0.015f)
                               .rotationOffset(90, 0, 0)
                               .scale((float) (0.95f + Math.sin((relic.getTickCount() + partialTick) * 0.1) * 0.1f))
                               .alphaDistanceFactor(1.5F))
                .build();
        return new GeoRenderContext(relic, visualNode, Color.WHITE, partialTick, packedLight, renderContext);
    }
}
