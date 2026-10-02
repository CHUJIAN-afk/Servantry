package first.summoner.client.attachmentEntityRenderer.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.api.LyraAPI;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.ModelContext;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.trail.RibbonTrailContext;
import first.lyra.client.render.trail.TrailContext;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.common.minion.minion.InfiniteShadow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import static first.summoner.utils.RenderUtil.getDominantColor;

public class InfiniteShadowRenderer extends AbstractAttachmentEntityRenderer<InfiniteShadow> {

    @Override
    protected RenderContext<InfiniteShadow> createContext(InfiniteShadow infiniteShadow, PathNode visualNode, float partialTick, int packedLight) {
        ItemStack itemStack = infiniteShadow.getItemStack();
        if (!itemStack.isEmpty()) {
            int dominantColor = getDominantColor(itemStack);
            TrailContext<InfiniteShadow> trail = null;
            if (!(itemStack.getItem() instanceof BlockItem)) {
                trail = new RibbonTrailContext<InfiniteShadow>()
                        .upOffset(1.015f)
                        .timer(infiniteShadow.trailTimer)
                        .colorRGB(dominantColor)
                        .historyLength(4);
            }
            return super.createContext(infiniteShadow, visualNode, partialTick, packedLight)
                    .trail(trail)
                    .model(new ModelContext()
                                   .scale(1.5f)
                                   .rotationOffset(0, 90, -45));
        }
        return null;
    }

    @Override
    protected void render(PoseStack poseStack, MultiBufferSource bufferSource) {
        ItemStack itemStack = context.entity.getItemStack();
        if (!itemStack.isEmpty()) {
            ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
            itemRenderer.renderStatic(
                    itemStack,
                    ItemDisplayContext.FIXED,
                    LightTexture.FULL_BRIGHT,
                    OverlayTexture.NO_OVERLAY,
                    poseStack,
                    bufferSource,
                    context.entity.getLevel(),
                    0
            );
            LyraAPI.light(context.visualNode.pos(), 8);
        }
    }
}
