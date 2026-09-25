package first.summoner.client.attachmentEntityRenderer.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityGeoRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.Summoner;
import first.summoner.common.minion.minion.IronGolem;
import net.minecraft.client.renderer.MultiBufferSource;
import software.bernie.geckolib.util.Color;

/**
 * 铁傀儡渲染器 - geo 模型 {@code entity/summon/iron_golem_32}，使用世界光照。
 */
public class IronGolemRenderer extends AbstractAttachmentEntityGeoRenderer<IronGolem> {

    public IronGolemRenderer() {
        super(Summoner.rl("entity/summon/iron_golem_32"));
    }

    @Override
    protected GeoRenderContext createContext(IronGolem golem, PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, int packedLight, PathNode visualNode) {
        RenderContext<IronGolem> renderContext = RenderContext.<IronGolem>builder()
                .model(new ModelConfig<IronGolem>()
                               .rotationOffset(180, 0, 0))
                .build();
        return new GeoRenderContext(golem, visualNode, Color.WHITE, partialTick, packedLight, renderContext);
    }
}
