package first.summoner.client.attachmentEntityRenderer.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityGeoRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.Summoner;
import first.summoner.common.minion.minion.VampireFrog;
import net.minecraft.client.renderer.MultiBufferSource;
import software.bernie.geckolib.util.Color;

/**
 * 吸血鬼青蛙渲染器 - geo 模型 {@code entity/summon/vampire_frog}。
 */
public class VampireFrogRenderer extends AbstractAttachmentEntityGeoRenderer<VampireFrog> {

    public VampireFrogRenderer() {
        super(Summoner.rl("entity/summon/vampire_frog"));
    }

    @Override
    protected GeoRenderContext createContext(VampireFrog frog, PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, int packedLight, PathNode visualNode) {
        RenderContext<VampireFrog> renderContext = RenderContext.<VampireFrog>builder()
                .model(new ModelConfig<VampireFrog>()
                               .rotationOffset(180, 0, 0)
                               .alphaDistanceFactor(1.5F))
                .build();
        return new GeoRenderContext(frog, visualNode, Color.WHITE, partialTick, packedLight, renderContext);
    }
}
