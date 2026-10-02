package first.summoner.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.rendererHelper.LaserRendererHelper;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.common.projectile.DestructionBullet;
import first.summoner.utils.DynamicLightUtil;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.AABB;

public class DestructionBulletRenderer extends AbstractAttachmentEntityRenderer<DestructionBullet> {

    @Override
    protected RenderContext<DestructionBullet> createContext(DestructionBullet laser, PathNode visualNode, float partialTick, int packedLight) {
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
                .layers(2)
                .segments(4)
                .color(0xff3d00)
                .alpha(0.5f)
                .innerRatio(0.4f)
                .render(poseStack, bufferSource);
        DynamicLightUtil.addLight(context.visualNode, hitbox, 8);
    }
}
