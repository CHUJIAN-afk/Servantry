package first.summoner.client.attachmentEntityRenderer.minion;

import first.lyra.client.render.ModelContext;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.geo.AbstractAttachmentEntityGeoRenderer;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.Summoner;
import first.summoner.common.minion.minion.Imp;
import net.minecraft.client.renderer.LightTexture;

/**
 * 小鬼仆从渲染器 - geo 模型 {@code entity/summon/summon_imp}，常亮渲染。
 */
public class ImpRenderer extends AbstractAttachmentEntityGeoRenderer<Imp> {

    public ImpRenderer() {
        super(Summoner.rl("entity/summon/summon_imp"));
    }

    @Override
    protected RenderContext<Imp> createContext(Imp imp, PathNode visualNode, float partialTick, int packedLight) {
        return super.createContext(imp, visualNode, partialTick, packedLight)
                .packedLight(LightTexture.FULL_BRIGHT)
                .model(new ModelContext().scale(0.8F).translateOffset(0, -1.15F, 0).rotationOffset(180, 0, 0));
    }
}
