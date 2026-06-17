package first.servantry.client.attachmentEntityRenderer.servant;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.api.client.render.renderConfig.ModelConfig;
import first.servantry.api.client.render.renderConfig.RibbonTrailConfig;
import first.servantry.api.client.render.renderConfig.TrailConfig;
import first.servantry.api.entity.PathNode;
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
    protected RenderContext<InfiniteShadow> createContext(InfiniteShadow infiniteShadow) {
        int timer = infiniteShadow.attacking ? infiniteShadow.trailTimer : 0;
        ItemStack itemStack = infiniteShadow.getItemStack();
        if (!itemStack.isEmpty()) {
            int dominantColor = RenderUtil.getDominantColor(itemStack);
            TrailConfig<InfiniteShadow, ?> trailConfig = null;
            if (!(itemStack.getItem() instanceof BlockItem)) {
                trailConfig = new RibbonTrailConfig<InfiniteShadow>()
                        .timer(timer)
                        .colorRGB(dominantColor)
                        .historyLength(4)
                        .upOffset(1.015f)
                        .colorFunction((shadow, progress, timeShift) -> dominantColor);
            }
            return RenderContext.<InfiniteShadow>builder()
                    .trail(trailConfig)
                    .model(new ModelConfig<InfiniteShadow>()
                                   .scale(1.5f)
                                   .rotationOffset(0, 90, -45)
                                   .visualNodeFunction((shadow, partialTick, rawNode) -> rawNode.lerp(shadow.getInterpolatedIdleState(partialTick), Mth.lerp(partialTick, shadow.idleBlendO, shadow.idleBlend))))
                    .build();
        }
        return null;
    }

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
}