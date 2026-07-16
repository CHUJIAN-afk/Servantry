package first.servantry.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.client.dynamicLight.DynamicLightDispatcher;
import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.api.client.render.renderer.LightningRendererHelper;
import first.servantry.api.client.render.sphere.SphereRenderer;
import first.servantry.api.entity.PathNode;
import first.servantry.common.projectile.BlitzBall;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.Set;

public class BlitzBallRenderer extends AbstractAttachmentEntityRenderer<BlitzBall> {

    @Override
    protected RenderContext<BlitzBall> createContext(BlitzBall entity) {
        return RenderContext.<BlitzBall>builder()
                .build();
    }

    @Override
    protected void modelModify(BlitzBall blitzBall, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<BlitzBall> context, float partialTick) {
        super.modelModify(blitzBall, poseStack, bufferSource, visualNode, context, partialTick);
        Set<Integer> list = blitzBall.getIdList();
        Player owner = blitzBall.getOwner();
        Level level = owner.level();
        for (Integer id : list) {
            Entity entity = level.getEntity(id);
            if (entity instanceof LivingEntity living) {
                RandomSource random = living.getRandom();
                random.setSeed(blitzBall.hashCode() + living.hashCode());
                AABB box = living.getBoundingBox();
                LightningRendererHelper.builder()
                        .from(visualNode.pos())
                        .to(box.getCenter().offsetRandom(random, (float) box.getSize() * 0.5f))
                        .renderOrigin(visualNode.pos())
                        .layers(1)
                        .segments(4)
                        .branches(0)
                        .jitter(0.1f)
                        .branchLength(0f)
                        .radius(0.02f, 0.02f)
                        .color(0x38ffec)
                        .alpha(0.5f)
                        .render(poseStack, bufferSource, random);
            }
        }
    }

    @Override
    protected void render(BlitzBall entity, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<BlitzBall> context, float partialTick) {
        SphereRenderer.builder()
                .radius(0.25f)
                .layers(3)
                .sides(6)
                .color(0x38ffec)
                .alpha(0.75f)
                .innerRatio(0.5f)
                .render(poseStack, bufferSource);
        DynamicLightDispatcher.addLightSources(visualNode.pos(), 8);
    }
}
