package first.summoner.client.attachmentEntityRenderer.minion;

import first.lyra.client.render.ModelContext;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.geo.AbstractAttachmentEntityGeoRenderer;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.Summoner;
import first.summoner.common.minion.minion.SnowFlinx;

/**
 * 小雪怪渲染器 - geo 模型 {@code entity/summon/summon_snow_flinx}。
 */
public class SnowFlinxRenderer extends AbstractAttachmentEntityGeoRenderer<SnowFlinx> {

    public SnowFlinxRenderer() {
        super(Summoner.rl("entity/summon/summon_snow_flinx"));
    }

    @Override
    protected RenderContext<SnowFlinx> createContext(SnowFlinx flinx, PathNode visualNode, float partialTick, int packedLight) {
        return super.createContext(flinx, visualNode, partialTick, packedLight)
                .model(new ModelContext().rotationOffset(90, 0, 0));
    }
}
