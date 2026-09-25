package first.summoner.client.attachmentEntityRenderer;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.common.attachmentEntity.AttachmentEntity;
import first.lyra.register.LyraRegistries;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

/**
 * 简易渲染器：交给函数式渲染器自行绘制，未提供时退回实体名称标签。
 */
public class SimpleRenderer<T extends AttachmentEntity> extends AbstractAttachmentEntityRenderer<T> {

    private final Render<T> renderer;

    @FunctionalInterface
    public interface Render<T extends AttachmentEntity> {
        void render(PoseStack poseStack, MultiBufferSource bufferSource, RenderContext<T> context);
    }

    public SimpleRenderer(@Nullable Render<T> renderer) {
        this.renderer = renderer;
    }

    @Override
    protected void render(PoseStack poseStack, MultiBufferSource bufferSource) {
        if (renderer != null) {
            renderer.render(poseStack, bufferSource, context);
        } else {
            // 26.2 submitNameTag 在 1.21.1 无等价 API；1.21.1 通用文本标签写法（见 1.21.1 TextOnlyRenderer）。
            ResourceLocation location = LyraRegistries.ATTACHMENT_ENTITY_TYPES.getKey(context.entity.getType());
            if (location != null) {
                String key = "summon." + location.getNamespace() + "." + location.getPath();
                Component component = Component.translatable(key).withStyle(ChatFormatting.DARK_AQUA);
                Minecraft minecraft = Minecraft.getInstance();
                Font font = minecraft.font;
                poseStack.pushPose();
                Matrix4f pose = poseStack.last().pose();
                float x = pose.m30(), y = pose.m31(), z = pose.m32();
                poseStack.setIdentity();
                poseStack.translate(x, y, z);
                poseStack.mulPose(minecraft.getEntityRenderDispatcher().cameraOrientation());
                poseStack.scale(0.02F, -0.02F, 0.02F);
                PoseStack.Pose last = poseStack.last();
                font.drawInBatch(component, (float) -font.width(component) / 2, -font.lineHeight, 0xFFFFFF, true, last.pose(), bufferSource, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
                poseStack.popPose();
            }
        }
    }
}
