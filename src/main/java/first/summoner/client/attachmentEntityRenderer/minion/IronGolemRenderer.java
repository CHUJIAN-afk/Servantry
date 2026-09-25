package first.summoner.client.attachmentEntityRenderer.minion;

import first.lyra.client.render.ModelContext;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.geo.AbstractAttachmentEntityGeoRenderer;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.Summoner;
import first.summoner.common.minion.minion.IronGolem;

/**
 * 铁傀儡渲染器 - geo 模型 {@code entity/summon/iron_golem_32}，使用世界光照。
 */
public class IronGolemRenderer extends AbstractAttachmentEntityGeoRenderer<IronGolem> {

    public IronGolemRenderer() {
        super(Summoner.rl("entity/summon/iron_golem_32"));
    }

    @Override
    protected RenderContext<IronGolem> createContext(IronGolem golem, PathNode visualNode, float partialTick, int packedLight) {
        return super.createContext(golem, visualNode, partialTick, packedLight)
                .model(new ModelContext().rotationOffset(180, 0, 0));
    }
}
