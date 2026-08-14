package first.summoner.utils;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.common.entity.AttachmentEntity;
import first.lyra.register.LyraRegistries;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class RenderUtil {

    private static final Map<Object, Integer> COLOR_CACHE = new WeakHashMap<>();

    public static void renderDebugLabel(PoseStack poseStack, MultiBufferSource bufferSource, AttachmentEntity entity) {


        ResourceLocation location = LyraRegistries.ATTACHMENT_ENTITY_TYPES.getKey(entity.getType());
        assert location != null;
        String var10000 = location.getNamespace();
        String key = "servant." + var10000 + "." + location.getPath();
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

    public static int getDominantColor(ItemStack itemStack) {
        if (!itemStack.isEmpty()) {
            return getDominantColor(Minecraft.getInstance().getItemRenderer().getModel(itemStack, null, null, 0));
        }
        return 0xFFFFFF;
    }

    public static int getDominantColor(BakedModel bakedModel) {
        return COLOR_CACHE.computeIfAbsent(bakedModel, k -> extractDominantColor(bakedModel));
    }

    @SuppressWarnings("deprecation")
    public static int extractDominantColor(BakedModel model) {
        try {
            Minecraft minecraft = Minecraft.getInstance();
            ClientLevel level = minecraft.level;
            if (level == null) return 0xFFFFFF;
            List<BakedQuad> quads = model.getQuads(null, null, level.random);
            if (quads.isEmpty()) {
                for (Direction dir : Direction.values()) {
                    quads = model.getQuads(null, dir, minecraft.level.random);
                    if (!quads.isEmpty()) break;
                }
            }
            if (quads.isEmpty()) return 0xFFFFFF;

            TextureAtlasSprite sprite = quads.getFirst().getSprite();
            SpriteContents contents = sprite.contents();

            int width = contents.width();
            int height = contents.height();
            if (width <= 0 || height <= 0) return 0xFFFFFF;

            long totalR = 0, totalG = 0, totalB = 0;
            long count = 0;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int pixel = contents.getOriginalImage().getPixelRGBA(x, y);
                    int a = (pixel >> 24) & 0xFF;
                    if (a < 128) continue;
                    int r = pixel & 0xFF;
                    int g = (pixel >> 8) & 0xFF;
                    int b = (pixel >> 16) & 0xFF;
                    totalR += r;
                    totalG += g;
                    totalB += b;
                    count++;
                }
            }
            if (count == 0) return 0xFFFFFF;
            return ((int) (totalR / count) << 16) | ((int) (totalG / count) << 8) | (int) (totalB / count);
        } catch (Exception e) {
            return 0xFFFFFF;
        }
    }
}
