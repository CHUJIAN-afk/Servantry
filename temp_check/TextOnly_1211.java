package first.summoner.client.attachmentEntityRenderer.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.common.entity.AttachmentEntity;
import first.lyra.common.entity.PathNode;
import first.lyra.register.LyraRegistries;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

/**
 * 通用文本标签渲染器。
 * <p>
 * 为没有专属模型的附件实体渲染面向相机的实体名称标签，
 * 作为占位渲染（注册表传 null 渲染器时自动回退到此实现）。
 * </p>
 */
public class TextOnlyRenderer<T extends AttachmentEntity> extends AbstractAttachmentEntityRenderer<T> {

    @Override
    protected RenderContext<T> createContext(T entity, float partialTick) {
        return RenderContext.<T>builder()
                .build();
    }

    @Override
    protected void render(T entity, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<T> context, float partialTick, float alpha) {
        ResourceLocation location = LyraRegistries.ATTACHMENT_ENTITY_TYPES.getKey(entity.getType());
        assert location != null;
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
        int packedLight = LightTexture.FULL_BRIGHT;
        PoseStack.Pose last = poseStack.last();
        font.drawInBatch(component, (float) -font.width(component) / 2, -font.lineHeight, 0xFFFFFF, true, last.pose(), bufferSource, Font.DisplayMode.NORMAL, 0, packedLight);
        poseStack.popPose();
    }
}
