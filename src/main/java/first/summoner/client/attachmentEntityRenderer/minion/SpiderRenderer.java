package first.summoner.client.attachmentEntityRenderer.minion;

import first.lyra.client.render.ModelContext;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.geo.AbstractAttachmentEntityGeoRenderer;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.Summoner;
import first.summoner.common.minion.minion.Spider;
import net.minecraft.resources.ResourceLocation;

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
    protected RenderContext<Spider> createContext(Spider spider, PathNode visualNode, float partialTick, int packedLight) {
        return super.createContext(spider, visualNode, partialTick, packedLight)
                .model(new ModelContext().rotationOffset(180, 0, 0));
    }
}
