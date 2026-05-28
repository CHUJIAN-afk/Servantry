package first.servantry.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class RenderUtil {

    private static final Map<Object, Integer> COLOR_CACHE = new WeakHashMap<>();

    public static int getDominantColor(ItemStack itemStack) {
        return getDominantColor(Minecraft.getInstance().getItemRenderer().getModel(itemStack, null, null, 0));
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
