package first.summoner.client.attachmentEntityRenderer.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityGeoRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.Summoner;
import first.summoner.common.minion.minion.Imp;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import software.bernie.geckolib.util.Color;

/**
 * 小鬼仆从渲染器 - geo 模型 {@code entity/summon/summon_imp}，常亮渲染。
 */
public class ImpRenderer extends AbstractAttachmentEntityGeoRenderer<Imp> {

    public ImpRenderer() {
        super(Summoner.rl("entity/summon/summon_imp"));
    }

    @Override
    protected GeoRenderContext createContext(Imp imp, PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, int packedLight, PathNode visualNode) {
        RenderContext<Imp> renderContext = RenderContext.<Imp>builder()
                .model(new ModelConfig<Imp>()
                               .scale(0.8F)
                               .translateOffset(0, -1.15F, 0)
                               .rotationOffset(180, 0, 0)
                               .alphaDistanceFactor(1.5F))
                .build();
        return new GeoRenderContext(imp, visualNode, Color.WHITE, partialTick, LightTexture.FULL_BRIGHT, renderContext);
    }
}
