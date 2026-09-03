package first.summoner.client.attachmentEntityRenderer;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.common.entity.AttachmentEntity;
import first.lyra.common.entity.PathNode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class SimpleRenderer<T extends AttachmentEntity> extends AbstractAttachmentEntityRenderer<T> {

    private final Render<T> renderer;

    @FunctionalInterface
    public interface Render<T extends AttachmentEntity> {
        void render(T entity, PoseStack poseStack, SubmitNodeCollector collector, PathNode visualNode, RenderContext<T> context, float partialTick, float alpha);
    }

    public SimpleRenderer(@Nullable Render<T> renderer) {
        this.renderer = renderer;
    }

    @Override
    protected RenderContext<T> createContext(T entity, float partialTick) {
        return RenderContext.<T>builder()
                .build();
    }

    @Override
    protected void modelModify(T entity, PoseStack poseStack, SubmitNodeCollector collector, PathNode visualNode, RenderContext<T> context, float partialTick, int packedLight, float alpha) {
        if (renderer != null) {
            renderer.render(entity, poseStack, collector, visualNode, context, partialTick, alpha);
        } else {
            collector.submitNameTag(poseStack, Vec3.ZERO, 0, entity.getType().getDisplayName(), false, LightCoordsUtil.FULL_BRIGHT, Minecraft.getInstance().gameRenderer.gameRenderState().levelRenderState.cameraRenderState);
        }
    }

    @Override
    protected void render(T entity, PoseStack poseStack, SubmitNodeCollector collector, PathNode visualNode, RenderContext<T> context, float partialTick, int packedLight, float alpha) {
    }
}
