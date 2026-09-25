package first.summoner.client.attachmentEntityRenderer.minion;

import first.lyra.client.render.ModelContext;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.geo.AbstractAttachmentEntityGeoRenderer;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.Summoner;
import first.summoner.common.minion.minion.Slime;

/**
 * 史莱姆渲染器 - geo 模型 {@code entity/summon/slime_baby}，使用世界光照。
 */
public class SlimeRenderer extends AbstractAttachmentEntityGeoRenderer<Slime> {

    public SlimeRenderer() {
        super(Summoner.rl("entity/summon/slime_baby"));
    }

    @Override
    protected RenderContext<Slime> createContext(Slime slime, PathNode visualNode, float partialTick, int packedLight) {
        return super.createContext(slime, visualNode, partialTick, packedLight)
                .model(new ModelContext().rotationOffset(180, 0, 0));
    }
}
