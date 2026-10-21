package first.summoner.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.light.LyraLights;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.rendererHelper.LaserRendererHelper;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.common.projectile.Laser;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.util.Color;

public class LaserRenderer extends AbstractAttachmentEntityRenderer<Laser> {

    @Override
    protected RenderContext<Laser> createContext(Laser laser, PathNode visualNode, float partialTick, int packedLight) {
        return super.createContext(laser, visualNode, partialTick, packedLight);
    }

    @Override
    protected void render(PoseStack poseStack, MultiBufferSource bufferSource) {
        AABB hitbox = context.entity.getHitbox();
        float length = (float) hitbox.getZsize();
        float width = (float) hitbox.getXsize() * 0.5f;
        LaserRendererHelper.builder()
                .length(length)
                .radius(width, width)
                .layers(4)
                .segments(4)
                .color(0xFF3333)
                .alpha(0.95f)
                .innerRatio(0.15f)
                .render(poseStack, bufferSource);
        Vec3 pos = context.visualNode.pos();
        Color color = Color.ofOpaque(0xFF3333);
        LyraLights.next().point()
                .at(pos.x(), pos.y(), pos.z())
                .color(color.getRed(), color.getGreen(), color.getBlue())
                .intensity(0.005f)
                .range(1f)
                .shadows(false);
    }
}
