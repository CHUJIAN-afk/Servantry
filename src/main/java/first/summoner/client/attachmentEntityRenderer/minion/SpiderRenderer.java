package first.summoner.client.attachmentEntityRenderer.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityGeoRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.Summoner;
import first.summoner.common.minion.minion.Spider;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.util.Color;

/**
 * 蜘蛛渲染器 - geo 模型 {@code entity/summon/spider}，按 {@code spider.variant} 切换三套贴图。
 */
public class SpiderRenderer extends AbstractAttachmentEntityGeoRenderer<Spider> {

    private static final ResourceLocation DEFAULT_TEXTURE = Summoner.rl("textures/entity/summon/spider.png");
    private static final ResourceLocation JUMPER_TEXTURE = Summoner.rl("textures/entity/summon/spider/jumper.png");
    private static final ResourceLocation DANGEROUS_TEXTURE = Summoner.rl("textures/entity/summon/spider/dangerous.png");

    public SpiderRenderer() {
        super(Summoner.rl("entity/summon/spider"));
    }

    @Override
    public ResourceLocation getTextureLocation(Spider spider) {
        int variant = spider.variant < 0 ? 0 : Math.floorMod(spider.variant, 3);
        return variant == 1 ? JUMPER_TEXTURE : variant == 2 ? DANGEROUS_TEXTURE : DEFAULT_TEXTURE;
    }

    @Override
    protected GeoRenderContext createContext(Spider spider, PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, int packedLight, PathNode visualNode) {
        RenderContext<Spider> renderContext = RenderContext.<Spider>builder()
                .model(new ModelConfig<Spider>()
                               .rotationOffset(180, 0, 0)
                               .alphaDistanceFactor(1.5F))
                .build();
        return new GeoRenderContext(spider, visualNode, Color.WHITE, partialTick, packedLight, renderContext);
    }
}
