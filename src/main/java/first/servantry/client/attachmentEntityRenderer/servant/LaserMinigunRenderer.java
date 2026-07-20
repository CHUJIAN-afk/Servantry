package first.servantry.client.attachmentEntityRenderer.servant;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.Servantry;
import first.servantry.api.client.geo.GeoSideloader;
import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.api.client.render.renderConfig.ModelConfig;
import first.servantry.api.entity.PathNode;
import first.servantry.common.servant.LaserMinigun;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;

/**
 * 激光机枪渲染器 - 占位模型+蓝色粒子拖尾。
 */
public class LaserMinigunRenderer extends AbstractAttachmentEntityRenderer<LaserMinigun> {

    @Override
    protected RenderContext<LaserMinigun> createContext(LaserMinigun entity) {
        return RenderContext.<LaserMinigun>builder()
                .model(new ModelConfig<LaserMinigun>()
                               .translateOffset(0.0375f, -0.35f, 0)
                               .rotationOffset(180, 0, 0)
                               .alphaDistanceFactor(1.5f))
                .build();
    }

    @Override
    protected void render(LaserMinigun servant, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<LaserMinigun> context, float partialTick) {
        int tickCount = servant.getTickCount();
        GeoSideloader.create(Servantry.rl("laser_minigun"))
                .setAnimation("shooting", Mth.lerp(partialTick, tickCount, tickCount + 1))
                .render(poseStack, bufferSource, partialTick, LightTexture.FULL_BRIGHT);
    }
}
