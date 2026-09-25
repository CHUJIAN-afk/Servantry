package first.summoner.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.dynamicLight.DynamicLightDispatcher;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.rendererHelper.LaserRendererHelper;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.common.projectile.GodFlame;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.AABB;

public class GodFlameRenderer extends AbstractAttachmentEntityRenderer<GodFlame> {

    @Override
    protected RenderContext<GodFlame> createContext(GodFlame projectile, PathNode visualNode, float partialTick, int packedLight) {
        return super.createContext(projectile, visualNode, partialTick, packedLight);
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
                .color(0x6f19d4)
                .alpha(0.95f)
                .innerRatio(0.35f)
                .render(poseStack, bufferSource);
        DynamicLightDispatcher.addLightSources(context.visualNode, hitbox, 8);
    }
}
