package first.summoner.client.attachmentEntityRenderer.minion;

import first.lyra.client.render.ModelContext;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.geo.AbstractAttachmentEntityGeoRenderer;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.Summoner;
import first.summoner.common.minion.sentry.Cannon;
import net.minecraft.client.renderer.LightTexture;

/**
 * 玉米炮哨兵渲染器：geo 模型 {@code entity/summon/cannon}，常亮渲染，开火动画由 geo 动画控制器驱动。
 */
public class CannonRenderer extends AbstractAttachmentEntityGeoRenderer<Cannon> {

    public CannonRenderer() {
        super(Summoner.rl("entity/summon/cannon"));
    }

    @Override
    protected RenderContext<Cannon> createContext(Cannon cannon, PathNode visualNode, float partialTick, int packedLight) {
        return super.createContext(cannon, visualNode, partialTick, packedLight)
                .packedLight(LightTexture.FULL_BRIGHT)
                .model(new ModelContext()
                               .scale(0.6f)
                               .translateOffset(0, -0.625f, -0.375f)
                               .alphaDistanceFactor(1.5f));
    }
}
