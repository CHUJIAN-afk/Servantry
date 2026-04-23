package first.servantry.client.renderer.servant;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.PathNode;
import first.servantry.api.client.servant.AbstractServantRenderer;
import first.servantry.api.client.servant.ServantRenderConfig;
import first.servantry.common.servent.StardustCell;
import first.servantry.register.ItemRegister;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;

/**
 * 星尘细胞渲染器。
 * <p>
 * 使用圆锥拖尾，渲染旋转的星尘细胞模型。
 * </p>
 */
public class StardustCellRendererServant extends AbstractServantRenderer<StardustCell> {

    @Override
    protected ServantRenderConfig<StardustCell> createConfig(StardustCell servant) {
        return ServantRenderConfig.<StardustCell>cone(servant.trailTimer, 0x8AE0FF, 0.2f)
                .trailResolution(12)
                .trailFadeOut(progress -> (float) Math.pow(Math.max(0.0f, 1.0f - progress), 2.0))
                .modelScale(0.5f)
                .visualNodeFunction((cell, partialTick, rawNode) -> {
                    float y = cell.getRenderYaw(partialTick);
                    float p = cell.getRenderPitch(partialTick);
                    float r = cell.getRenderRoll(partialTick);
                    return new PathNode(rawNode.pos(), y, p, r);
                });
    }

    @Override
    protected void renderModelItem(StardustCell servant, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, ServantRenderConfig<StardustCell> config) {
        Minecraft.getInstance().getItemRenderer().renderStatic(
                ItemRegister.StardustCell.get().getDefaultInstance(),
                ItemDisplayContext.FIXED,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                bufferSource,
                servant.getOwner().level(),
                0
        );
    }

}