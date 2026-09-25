package first.summoner.client.attachmentEntityRenderer.minion;

import first.lyra.client.render.ModelContext;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.geo.AbstractAttachmentEntityGeoRenderer;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.Summoner;
import first.summoner.common.minion.minion.Finch;

/**
 * 小鸟渲染器 - geo 模型 {@code entity/summon/finch_baby}，使用世界光照。
 */
public class FinchRenderer extends AbstractAttachmentEntityGeoRenderer<Finch> {

    public FinchRenderer() {
        super(Summoner.rl("entity/summon/finch_baby"));
    }

    @Override
    protected RenderContext<Finch> createContext(Finch finch, PathNode visualNode, float partialTick, int packedLight) {
        return super.createContext(finch, visualNode, partialTick, packedLight)
                .model(new ModelContext().translateOffset(0, -0.1f, 0.05f).rotationOffset(180, 0, 0));
    }
}
