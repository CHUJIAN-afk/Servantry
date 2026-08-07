package first.servantry.client.attachmentEntityRenderer.servant;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.geo.GeoSideloader;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.entity.PathNode;
import first.servantry.Servantry;
import first.servantry.common.sentryServant.Cannon;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * 玉米加农炮渲染器 - 占位模型。
 */
public class CannonRenderer extends AbstractAttachmentEntityRenderer<Cannon> {

    @Override
    protected RenderContext<Cannon> createContext(Cannon entity) {
        return RenderContext.<Cannon>builder()
                .model(new ModelConfig<Cannon>()
                        .scale(2f)
                        .translateOffset(0f, -0.5f, -0.5f)
                        .rotationOffset(180, 0, 0)
                )
                .build();
    }

    @Override
    protected void render(Cannon cannon, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<Cannon> context, float partialTick) {
        GeoSideloader sideloader = GeoSideloader.create(Servantry.rl("cannon"));
        if (!cannon.isHasCorn()) {
            sideloader.hideBone("玉米棒");
        }
        sideloader.setAnimation("shooting", cannon.getShootingTick(partialTick));
        sideloader.render(poseStack, bufferSource, partialTick, LightTexture.FULL_BRIGHT);
    }
}
