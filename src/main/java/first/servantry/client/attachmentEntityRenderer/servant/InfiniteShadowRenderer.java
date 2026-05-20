package first.servantry.client.attachmentEntityRenderer.servant;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.PathNode;
import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.api.client.render.renderConfig.ConeTrailConfig;
import first.servantry.api.client.render.renderConfig.ModelConfig;
import first.servantry.api.client.render.renderConfig.RibbonTrailConfig;
import first.servantry.api.client.render.renderConfig.TrailConfig;
import first.servantry.common.servant.InfiniteShadow;
import first.servantry.utils.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class InfiniteShadowRenderer extends AbstractAttachmentEntityRenderer<InfiniteShadow> {

    @Override
    protected void renderEntity(InfiniteShadow infiniteShadow, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<InfiniteShadow> config) {
        ItemStack itemStack = infiniteShadow.getItemStack();
        if (!itemStack.isEmpty()) {
            ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
            itemRenderer.renderStatic(
                    itemStack,
                    ItemDisplayContext.FIXED,
                    LightTexture.FULL_BRIGHT,
                    OverlayTexture.NO_OVERLAY,
                    poseStack,
                    bufferSource,
                    infiniteShadow.getOwner().level(),
                    0
            );
        }
    }

    @Override
    protected RenderContext<InfiniteShadow> createContext(InfiniteShadow infiniteShadow) {
        int timer = infiniteShadow.attacking ? infiniteShadow.trailTimer : 0;
        ItemStack itemStack = infiniteShadow.getItemStack();
        if (!itemStack.isEmpty()) {
            int dominantColor = RenderUtil.getDominantColor(itemStack);
            TrailConfig<InfiniteShadow, ?> trailConfig;
            if (!(itemStack.getItem() instanceof BlockItem)) {
                trailConfig = new RibbonTrailConfig<InfiniteShadow>()
                        .timer(timer)
                        .colorRGB(dominantColor)
                        .historyLength(4)
                        .width(0.7075f)
                        .diamondSize(0.15f)
                        .colorFunction((shadow, progress, timeShift) -> {
                            float brightness = Mth.lerp(progress, 1f, 0.4f);
                            int r = (int) (((dominantColor >> 16) & 0xFF) * brightness);
                            int g = (int) (((dominantColor >> 8) & 0xFF) * brightness);
                            int b = (int) ((dominantColor & 0xFF) * brightness);
                            return (r << 16) | (g << 8) | b;
                        })
                        .tipAlphaBoost((s, progress) -> progress < 0.3f ? Mth.lerp(progress / 0.3f, 2.5f, 1.0f) : 1.0f)
                        .tipBrightnessBoost((s, progress) -> progress < 0.25f ? Mth.lerp(progress / 0.25f, 1.5f, 1.0f) : 1.0f);
            } else {
                trailConfig = new ConeTrailConfig<InfiniteShadow>()
                        .timer(timer)
                        .colorRGB(dominantColor)
                        .maxRadius(0.25f)
                        .minRadiusRatio(0.1f)
                        .resolution(12)
                        .fadeOut(progress -> (float) Math.pow(Math.max(0.0f, 1.0f - progress), 2.0));
            }
            return RenderContext.<InfiniteShadow>builder()
                    .trail(trailConfig)
                    .model(new ModelConfig<InfiniteShadow>()
                            .rotationOffset(0, 90, -45)
                            .visualNodeFunction((shadow, partialTick, rawNode) -> rawNode.lerp(shadow.getInterpolatedIdleState(partialTick), Mth.lerp(partialTick, shadow.idleBlendO, shadow.idleBlend))))
                    .build();
        }
        return null;
    }
}