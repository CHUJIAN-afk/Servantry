package first.summoner.client.attachmentEntityRenderer.minion;

import first.lyra.client.render.ModelContext;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.geo.AbstractAttachmentEntityGeoRenderer;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.Summoner;
import first.summoner.common.minion.minion.RuinRelic;

/**
 * 禁戒遗迹仆从渲染器 - geo 模型 {@code entity/summon/ruin_relic}，附带呼吸缩放脉动。
 */
public class RuinRelicRenderer extends AbstractAttachmentEntityGeoRenderer<RuinRelic> {

    public RuinRelicRenderer() {
        super(Summoner.rl("entity/summon/ruin_relic"));
    }

    @Override
    protected RenderContext<RuinRelic> createContext(RuinRelic relic, PathNode visualNode, float partialTick, int packedLight) {
        return super.createContext(relic, visualNode, partialTick, packedLight)
                .model(new ModelContext()
                        .translateOffset(0, -0.35f, 0.015f)
                        .rotationOffset(90, 0, 0));
    }
}
