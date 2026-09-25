package first.summoner.client.attachmentEntityRenderer.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityGeoRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.Summoner;
import first.summoner.common.minion.minion.Slime;
import net.minecraft.client.renderer.MultiBufferSource;
import software.bernie.geckolib.util.Color;

/**
 * 史莱姆渲染器 - geo 模型 {@code entity/summon/slime_baby}，使用世界光照。
 */
public class SlimeRenderer extends AbstractAttachmentEntityGeoRenderer<Slime> {

    public SlimeRenderer() {
        super(Summoner.rl("entity/summon/slime_baby"));
    }

    @Override
    protected GeoRenderContext createContext(Slime slime, PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, int packedLight, PathNode visualNode) {
        RenderContext<Slime> renderContext = RenderContext.<Slime>builder()
                .model(new ModelConfig<Slime>()
                               .rotationOffset(180, 0, 0)
                               .alphaDistanceFactor(1.5F))
                .build();
        return new GeoRenderContext(slime, visualNode, Color.WHITE, partialTick, packedLight, renderContext);
    }
}
