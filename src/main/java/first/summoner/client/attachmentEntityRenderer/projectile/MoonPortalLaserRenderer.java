package first.summoner.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.light.LyraLights;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.ModelContext;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.rendererHelper.LaserRendererHelper;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.common.projectile.MoonPortalLaser;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.util.Color;

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
        PathNode visualNode = context.visualNode;
        Vec3 pos = visualNode.pos();
        Color color = Color.ofOpaque(0x00fdd6);
        Vec3 normalize = Vec3.directionFromRotation(visualNode.pitch(), visualNode.yaw()).normalize();
        LyraLights.next().spot(0f, 4)
                .at(pos.x(), pos.y(), pos.z())
                .direction((float) normalize.x(), (float) normalize.y(), (float) normalize.z())
                .range((float) hitbox.getZsize())
                .color(color.getRed(), color.getGreen(), color.getBlue())
                .intensity(3f)
                .volumetric(1f)
                .shadows(false);
        LaserRendererHelper.builder()
                .length(length)
                .radius(0, width * 2)
                .layers(3)
                .segments(8)
                .color(COLOR)
                .alpha(0.95f)
                .innerRatio(0.35f)
                .render(poseStack, bufferSource);
    }
}
