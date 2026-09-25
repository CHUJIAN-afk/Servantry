package first.summoner.client.attachmentEntityRenderer.minion;

import first.lyra.client.render.ModelContext;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.geo.AbstractAttachmentEntityGeoRenderer;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.Summoner;
import first.summoner.common.minion.minion.Hornet;
import net.minecraft.client.renderer.LightTexture;

/**
 * 黄蜂渲染器 - geo 模型 {@code entity/summon/hornet_baby}，常亮渲染。
 */
public class HornetRenderer extends AbstractAttachmentEntityGeoRenderer<Hornet> {

    public HornetRenderer() {
        super(Summoner.rl("entity/summon/hornet_baby"));
    }

    @Override
    protected RenderContext<Hornet> createContext(Hornet hornet, PathNode visualNode, float partialTick, int packedLight) {
        return super.createContext(hornet, visualNode, partialTick, packedLight)
                .packedLight(LightTexture.FULL_BRIGHT)
                .model(new ModelContext().scale(0.6F).translateOffset(0, -0.2F, 0.3F).rotationOffset(180, -35, 0));
    }
}
