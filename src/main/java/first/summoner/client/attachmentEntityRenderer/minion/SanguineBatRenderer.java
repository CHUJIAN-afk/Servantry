package first.summoner.client.attachmentEntityRenderer.minion;

import first.lyra.client.render.ModelContext;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.geo.AbstractAttachmentEntityGeoRenderer;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.Summoner;
import first.summoner.common.minion.minion.SanguineBat;
import net.minecraft.client.renderer.LightTexture;

/**
 * 血蝙蝠渲染器 - geo 模型 {@code entity/summon/sanguine_bat}，常亮渲染。
 */
public class SanguineBatRenderer extends AbstractAttachmentEntityGeoRenderer<SanguineBat> {

    public SanguineBatRenderer() {
        super(Summoner.rl("entity/summon/sanguine_bat"));
    }

    @Override
    protected RenderContext<SanguineBat> createContext(SanguineBat bat, PathNode visualNode, float partialTick, int packedLight) {
        return super.createContext(bat, visualNode, partialTick, packedLight)
                .packedLight(LightTexture.FULL_BRIGHT)
                .model(new ModelContext().scale(0.5f).rotationOffset(180, 0, 0).translateOffset(0, -0.5f, 0));
    }
}
