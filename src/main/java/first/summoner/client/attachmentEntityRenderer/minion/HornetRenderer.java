package first.summoner.client.attachmentEntityRenderer.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.model.LyraModelRenderer;
import first.lyra.client.render.model.virtual.VirtualEntityPose;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.common.minion.minion.Hornet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.EntityType;

public class HornetRenderer extends AbstractAttachmentEntityRenderer<Hornet> {

    @Override
    protected RenderContext<Hornet> createContext(Hornet hornet, float partialTick) {
        return RenderContext.<Hornet>builder()
                .model(new ModelConfig<Hornet>()
                               .translateOffset(0, -0.5f, 0)
                               .alphaDistanceFactor(1.5f))
                .build();
    }

    @Override
    protected void render(Hornet hornet, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<Hornet> context, float partialTick, int packedLight, float alpha) {
        LyraModelRenderer.virtualEntity(EntityType.BEE, partialTick)
                .pose(VirtualEntityPose.create()
                        .ageTicks(hornet.getTickCount())
                        .look(0, 0, 0)
                        .walk(hornet.getTickCount(), 1)
                        .attack(0)
                        .packedLight(packedLight)
                        .alpha(alpha))
                .render(poseStack, bufferSource);
    }
}
