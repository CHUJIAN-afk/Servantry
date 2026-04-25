package first.servantry.client.renderer.servant;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.PathNode;
import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.common.servant.StardustDragon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * 星尘龙渲染器。
 * <p>
 * 根据体节索引决定渲染内容：
 * <ul>
 *   <li>头部（index=0）：渲染下界合金块</li>
 *   <li>中段：渲染钻石块</li>
 *   <li>尾部（index=total-1）：渲染金块</li>
 * </ul>
 * 不渲染轨迹。
 * </p>
 */
public class StardustDragonRenderer extends AbstractAttachmentEntityRenderer<StardustDragon> {

    @Override
    protected RenderContext<StardustDragon> createContext(StardustDragon dragon) {
        return RenderContext.<StardustDragon>none().modelScale(0.5f);
    }

    @Override
    protected void renderEntity(StardustDragon dragon, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<StardustDragon> config) {
        int total = dragon.getTotalSegments();
        int index = dragon.getSegmentIndex();
        ItemStack renderItem;
        // 多体节渲染逻辑
        if (index == 0) {
            renderItem = Items.DRAGON_HEAD.getDefaultInstance();
        } else if (index == total - 1) {
            renderItem = Items.GOLD_BLOCK.getDefaultInstance();
        } else {
            renderItem = Items.DIAMOND_BLOCK.getDefaultInstance();
        }
        Minecraft.getInstance().getItemRenderer().renderStatic(
                renderItem,
                ItemDisplayContext.NONE,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                bufferSource,
                dragon.getOwner().level(),
                0
        );
    }

}
