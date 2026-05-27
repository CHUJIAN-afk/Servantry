package first.servantry.client.creativeTab;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public record AnimInfo(int frameHeight, int frameTime, int totalFrames) {
    private static final Map<ResourceLocation, AnimInfo> animCache = new HashMap<>();
    private static final Map<ResourceLocation, long[]> animState = new HashMap<>();

    private static AnimInfo resolveAnim(ResourceLocation texture) {
        return animCache.computeIfAbsent(texture, loc -> {
            try {
                Resource res = Minecraft.getInstance().getResourceManager().getResourceOrThrow(loc);
                AnimationMetadataSection anim = res.metadata().getSection(AnimationMetadataSection.SERIALIZER).orElse(null);
                int imageHeight;
                try (NativeImage img = NativeImage.read(res.open())) {
                    imageHeight = img.getHeight();
                }
                if (anim != null && anim != AnimationMetadataSection.EMPTY) {
                    int frameHeight = anim.calculateFrameSize(0, imageHeight).height();
                    int frameTime = anim.getDefaultFrameTime();
                    int totalFrames = imageHeight / frameHeight;
                    return new AnimInfo(frameHeight, frameTime, totalFrames);
                }
                return new AnimInfo(imageHeight, 1, 1);
            } catch (IOException e) {
                return new AnimInfo(18, 1, 1);
            }
        });
    }

    private static int currentFrame(AnimInfo info, ResourceLocation texture, boolean playing) {
        long now = System.currentTimeMillis();
        long[] state = animState.computeIfAbsent(texture, k -> new long[]{0, now});
        if (playing) {
            state[0] += now - state[1];
        }
        state[1] = now;
        return (int) ((state[0] / (info.frameTime * 50L)) % info.totalFrames);
    }

    public static void blitAnimated(GuiGraphics graphics, ResourceLocation texture, int x, int y, int width, int mouseX, int mouseY, boolean hoverDriven) {
        AnimInfo info = resolveAnim(texture);
        boolean playing = hoverDriven
                ? mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + info.frameHeight
                : true;
        int frame = currentFrame(info, texture, playing);
        RenderSystem.setShaderTexture(0, texture);
        graphics.blit(texture,
                x, y,
                0, frame * info.frameHeight,
                width, info.frameHeight,
                width, info.totalFrames * info.frameHeight
        );
    }
}
