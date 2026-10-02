package first.summoner.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.ModelContext;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.rendererHelper.LaserRendererHelper;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.common.projectile.MoonPortalLaser;
import first.summoner.utils.DynamicLightUtil;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.AABB;

/**
 * 月亮传送门激光渲染器 - 青绿色激光束。
 */
public class MoonPortalLaserRenderer extends AbstractAttachmentEntityRenderer<MoonPortalLaser> {

    private static final int COLOR = 0x00fdd6;

    @Override
    protected RenderContext<MoonPortalLaser> createContext(MoonPortalLaser laser, PathNode visualNode, float partialTick, int packedLight) {
        return super.createContext(laser, visualNode, partialTick, packedLight)
                .model(new ModelContext()
                               .alphaDistanceFactor(0)
                               .rotationOffset(180, 0, 0));
    }

    @Override
    protected void render(PoseStack poseStack, MultiBufferSource bufferSource) {
        AABB hitbox = context.entity.getHitbox();
        float length = (float) hitbox.getZsize();
        float width = (float) hitbox.getXsize() * 0.5f;
        LaserRendererHelper.builder()
                .length(length)
                .radius(width * 0.25F, width)
                .layers(4)
                .segments(16)
                .color(COLOR)
                .alpha(0.95f)
                .innerRatio(0.35f)
                .render(poseStack, bufferSource);
        DynamicLightUtil.addLight(context.visualNode, hitbox, 8);
    }
}
