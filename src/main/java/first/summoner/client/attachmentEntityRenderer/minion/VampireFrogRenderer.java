package first.summoner.client.attachmentEntityRenderer.minion;

import first.lyra.client.render.ModelContext;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.geo.AbstractAttachmentEntityGeoRenderer;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.Summoner;
import first.summoner.common.minion.minion.VampireFrog;

/**
 * 吸血鬼青蛙渲染器 - geo 模型 {@code entity/summon/vampire_frog}。
 */
public class VampireFrogRenderer extends AbstractAttachmentEntityGeoRenderer<VampireFrog> {

    public VampireFrogRenderer() {
        super(Summoner.rl("entity/summon/vampire_frog"));
    }

    @Override
    protected RenderContext<VampireFrog> createContext(VampireFrog frog, PathNode visualNode, float partialTick, int packedLight) {
        return super.createContext(frog, visualNode, partialTick, packedLight)
                .model(new ModelContext().rotationOffset(180, 0, 0));
    }
}
