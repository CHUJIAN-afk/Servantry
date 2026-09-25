package first.summoner.client.attachmentEntityRenderer.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityGeoRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.Summoner;
import first.summoner.common.minion.minion.DeadlySphere;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import software.bernie.geckolib.util.Color;

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
    protected GeoRenderContext createContext(DeadlySphere sphere, PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, int packedLight, PathNode visualNode) {
        RenderContext<DeadlySphere> renderContext = RenderContext.<DeadlySphere>builder()
                .model(new ModelConfig<DeadlySphere>()
                               .rotationOffset(0, (sphere.getTickCount() + partialTick) * 4.5f, 90)
                               .alphaDistanceFactor(1.5F))
                .build();
        return new GeoRenderContext(sphere, visualNode, Color.WHITE, partialTick, LightTexture.FULL_BRIGHT, renderContext);
    }
}
