package first.summoner.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.dynamicLight.DynamicLightDispatcher;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.rendererHelper.LightningRendererHelper;
import first.lyra.client.render.rendererHelper.SphereRendererHelper;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.common.projectile.BlitzBall;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.Set;

public class BlitzBallRenderer extends AbstractAttachmentEntityRenderer<BlitzBall> {

    @Override
    protected RenderContext<BlitzBall> createContext(BlitzBall entity, float partialTick) {
        return RenderContext.<BlitzBall>builder()
                .build();
    }

    @Override
    protected void modelModify(BlitzBall blitzBall, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<BlitzBall> context, float partialTick, int packedLight, float alpha) {
        super.modelModify(blitzBall, poseStack, bufferSource, visualNode, context, partialTick, packedLight, alpha);
        Set<Integer> list = blitzBall.getIdList();
        Level level = blitzBall.getLevel();
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
                        .alpha(0.5f * alpha)
                        .render(poseStack, bufferSource, random);
            }
        }
    }

    @Override
    protected void render(BlitzBall entity, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<BlitzBall> context, float partialTick, int packedLight, float alpha) {
        SphereRendererHelper.builder()
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
