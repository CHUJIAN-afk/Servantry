package first.summoner.client.attachmentEntityRenderer.projectile;

import first.lyra.client.render.ModelContext;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.geo.AbstractAttachmentEntityGeoRenderer;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.Summoner;
import first.summoner.common.projectile.Corn;
import net.minecraft.client.renderer.LightTexture;

/**
 * 玉米炮射弹渲染器：geo 模型 {@code entity/summon/corn}，常亮渲染。
 */
public class CornRenderer extends AbstractAttachmentEntityGeoRenderer<Corn> {

    public CornRenderer() {
        super(Summoner.rl("entity/summon/corn"));
    }

    @Override
    protected RenderContext<Corn> createContext(Corn corn, PathNode visualNode, float partialTick, int packedLight) {
        return super.createContext(corn, visualNode, partialTick, packedLight)
                .packedLight(LightTexture.FULL_BRIGHT)
                .model(new ModelContext()
                               .scale(0.45f)
                               .translateOffset(0, -0.8125f, -0.3125f)
                               .alphaDistanceFactor(1.5f));
    }
}
