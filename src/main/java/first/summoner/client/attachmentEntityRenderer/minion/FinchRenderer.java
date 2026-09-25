package first.summoner.client.attachmentEntityRenderer.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityGeoRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.Summoner;
import first.summoner.common.minion.minion.Finch;
import net.minecraft.client.renderer.MultiBufferSource;
import software.bernie.geckolib.util.Color;

/**
 * 小鸟渲染器 - geo 模型 {@code entity/summon/finch_baby}，使用世界光照。
 */
public class FinchRenderer extends AbstractAttachmentEntityGeoRenderer<Finch> {

    public FinchRenderer() {
        super(Summoner.rl("entity/summon/finch_baby"));
    }

    @Override
    protected GeoRenderContext createContext(Finch finch, PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, int packedLight, PathNode visualNode) {
        RenderContext<Finch> renderContext = RenderContext.<Finch>builder()
                .model(new ModelConfig<Finch>()
                               .translateOffset(0, -0.1f, 0.05f)
                               .rotationOffset(180, 0, 0)
                               .alphaDistanceFactor(1.5F))
                .build();
        return new GeoRenderContext(finch, visualNode, Color.WHITE, partialTick, packedLight, renderContext);
    }
}
