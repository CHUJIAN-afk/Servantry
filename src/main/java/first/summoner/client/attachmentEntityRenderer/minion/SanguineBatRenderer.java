package first.summoner.client.attachmentEntityRenderer.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityGeoRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.Summoner;
import first.summoner.common.minion.minion.SanguineBat;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import software.bernie.geckolib.util.Color;

/**
 * 血蝙蝠渲染器 - geo 模型 {@code entity/summon/sanguine_bat}，常亮渲染。
 */
public class SanguineBatRenderer extends AbstractAttachmentEntityGeoRenderer<SanguineBat> {

    public SanguineBatRenderer() {
        super(Summoner.rl("entity/summon/sanguine_bat"));
    }

    @Override
    protected GeoRenderContext createContext(SanguineBat bat, PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, int packedLight, PathNode visualNode) {
        RenderContext<SanguineBat> renderContext = RenderContext.<SanguineBat>builder()
                .model(new ModelConfig<SanguineBat>()
                               .scale(0.5f)
                               .rotationOffset(180, 0, 0)
                               .translateOffset(0, -0.5f, 0)
                               .alphaDistanceFactor(1.5F))
                .build();
        Color color = Color.ofARGB(getAlpha(visualNode.pos(), bat.getOwner().getEyePosition(partialTick)), 1, 1, 1);
        return new GeoRenderContext(bat, visualNode, color, partialTick, LightTexture.FULL_BRIGHT, renderContext);
    }
}
