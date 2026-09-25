package first.summoner.client.attachmentEntityRenderer.minion;

import first.lyra.client.render.ModelContext;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.geo.AbstractAttachmentEntityGeoRenderer;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.Summoner;
import first.summoner.common.minion.minion.DeadlySphere;
import net.minecraft.client.renderer.LightTexture;

/**
 * 致命球渲染器 - 按当前形态切换 geo 基础路径
 * {@code entity/summon/deadly_sphere_{spikes|flames|blade}}，滚动时随实体 yaw/pitch/roll 旋转。
 */
public class DeadlySphereRenderer extends AbstractAttachmentEntityGeoRenderer<DeadlySphere> {

    public DeadlySphereRenderer() {
        super(ctx -> Summoner.rl("entity/summon/deadly_sphere_" + switch (ctx.entity.getForm()) {
            case 1 -> "flames";
            case 2 -> "blade";
            default -> "spikes";
        }));
    }

    @Override
    protected RenderContext<DeadlySphere> createContext(DeadlySphere sphere, PathNode visualNode, float partialTick, int packedLight) {
        return super.createContext(sphere, visualNode, partialTick, packedLight)
                .packedLight(LightTexture.FULL_BRIGHT)
                .model(new ModelContext().rotationOffset(0, (sphere.getTickCount() + partialTick) * 4.5f, 90));
    }
}
