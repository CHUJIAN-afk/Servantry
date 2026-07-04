package first.servantry.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.api.client.render.laser.LightningRenderer;
import first.servantry.api.client.render.sphere.SphereRenderer;
import first.servantry.api.entity.PathNode;
import first.servantry.common.projectile.BlitzBall;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

import java.util.Set;

public class BlitzBallRenderer extends AbstractAttachmentEntityRenderer<BlitzBall> {

    @Override
    protected RenderContext<BlitzBall> createContext(BlitzBall entity) {
        return RenderContext.<BlitzBall>builder()
                .build();
    }

    @Override
    protected void renderEntityModel(BlitzBall blitzBall, PoseStack poseStack, MultiBufferSource bufferSource, PathNode node, RenderContext<BlitzBall> config) {
        super.renderEntityModel(blitzBall, poseStack, bufferSource, node, config);
        Set<LivingEntity> list = blitzBall.getIdList();
        for (LivingEntity livingEntity : list) {
            RandomSource random = livingEntity.getRandom();
            random.setSeed(blitzBall.getUuid().hashCode());
            AABB box = livingEntity.getBoundingBox();
            LightningRenderer.builder()
                    .from(node.pos())
                    .to(box.getCenter().offsetRandom(random, (float) box.getSize() * 0.5f))
                    .renderOrigin(node.pos())
                    .layers(1)
                    .segments(6)
                    .branches(0)
                    .jitter(0.1f)
                    .branchLength(0f)
                    .radius(0.03f, 0.03f)
                    .color(0x38ffec)
                    .alpha(0.75f)
                    .render(poseStack, bufferSource, random);
        }
    }

    @Override
    protected void renderEntity(BlitzBall entity, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<BlitzBall> config) {
        SphereRenderer.builder()
                .radius(0.1f)
                .layers(2)
                .sides(16)
                .color(0x38ffec)
                .alpha(0.75f)
                .innerRatio(0.9f)
                .render(poseStack, bufferSource);
    }
}
