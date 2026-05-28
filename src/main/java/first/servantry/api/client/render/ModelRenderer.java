package first.servantry.api.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * 模型渲染工具类。
 */
public class ModelRenderer {

    /**
     * 渲染模型（使用默认物品渲染类型）。
     * <p>
     * 默认使用 {@link Sheets#translucentItemSheet()}，适合大多数物品模型。
     * </p>
     *
     * @param modelLocation 模型资源位置
     * @param poseStack     变换矩阵栈
     * @param bufferSource  顶点缓冲源
     */
    public static void renderModel(ModelResourceLocation modelLocation, PoseStack poseStack, MultiBufferSource bufferSource) {
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(modelLocation);
        Minecraft.getInstance().getItemRenderer().renderModelLists(
                model,
                ItemStack.EMPTY,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                bufferSource.getBuffer(Sheets.translucentItemSheet())
        );

    }
}
