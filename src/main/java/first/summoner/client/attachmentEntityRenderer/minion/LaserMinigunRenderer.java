package first.summoner.client.attachmentEntityRenderer.minion;

import first.lyra.client.render.ModelContext;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.geo.AbstractAttachmentEntityGeoRenderer;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.Summoner;
import first.summoner.common.minion.minion.LaserMinigun;

/**
 * 激光机枪渲染器：geo 模型 {@code entity/summon/laser_minigun}，使用世界光照，
 * 待机/开火动画由 geo 动画控制器驱动。
 */
public class LaserMinigunRenderer extends AbstractAttachmentEntityGeoRenderer<LaserMinigun> {

    public LaserMinigunRenderer() {
        super(Summoner.rl("entity/summon/laser_minigun"));
    }

    @Override
    protected RenderContext<LaserMinigun> createContext(LaserMinigun entity, PathNode visualNode, float partialTick, int packedLight) {
        return super.createContext(entity, visualNode, partialTick, packedLight)
                .model(new ModelContext()
                               .translateOffset(0, -0.625f, 0.2f)
                               .rotationOffset(180, 0, 0)
                               .alphaDistanceFactor(1.5f));
    }
}
