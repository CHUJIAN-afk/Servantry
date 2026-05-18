package first.servantry.api.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;

/**
 * 模型渲染工具类。
 * <p>
 * 提供直接从资源包加载模型文件并渲染的能力，无需注册占位物品。
 * </p>
 *
 * <h3>渲染类型选择</h3>
 * <pre>{@code
 * 常用渲染类型：
 * - Sheets.cutoutBlockSheet()        - 物品 Cutout（透明像素不渲染）
 * - Sheets.translucentItemSheet()    - 物品半透明（支持半透明像素）
 * - Sheets.translucentCullBlockSheet() - 实体半透明（带背面剔除）
 * - RenderType.entitySolid()         - 实体不透明
 * - RenderType.entityCutoutNoCull()  - 实体 Cutout（无背面剔除）
 * - RenderType.entityTranslucentCull() - 实体半透明（带背面剔除）
 * }</pre>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 使用默认渲染类型（物品 Cutout）
 * ModelRenderer.renderModel(MODEL, poseStack, bufferSource);
 *
 * // 自定义渲染类型
 * ModelRenderer.renderModel(MODEL, poseStack, bufferSource, Sheets.translucentItemSheet());
 *
 * // 使用函数动态选择渲染类型
 * ModelRenderer.renderModel(MODEL, poseStack, bufferSource, type -> bufferSource.getBuffer(type));
 * }</pre>
 */
public final class ModelRenderer {

    private ModelRenderer() {}

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
        renderModel(modelLocation, poseStack, bufferSource, Sheets.translucentItemSheet());
    }

    /**
     * 渲染模型（指定渲染类型）。
     *
     * @param modelLocation 模型资源位置
     * @param poseStack     变换矩阵栈
     * @param bufferSource  顶点缓冲源
     * @param renderType    渲染类型
     */
    public static void renderModel(ModelResourceLocation modelLocation, PoseStack poseStack, MultiBufferSource bufferSource, RenderType renderType) {
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(modelLocation);
        renderBakedModel(model, poseStack, bufferSource, renderType);
    }

    /**
     * 渲染已烘焙的模型（使用默认物品渲染类型）。
     *
     * @param model        已烘焙模型
     * @param poseStack    变换矩阵栈
     * @param bufferSource 顶点缓冲源
     */
    public static void renderBakedModel(BakedModel model, PoseStack poseStack, MultiBufferSource bufferSource) {
        renderBakedModel(model, poseStack, bufferSource, Sheets.cutoutBlockSheet());
    }

    /**
     * 渲染已烘焙的模型（指定渲染类型）。
     *
     * @param model        已烘焙模型
     * @param poseStack    变换矩阵栈
     * @param bufferSource 顶点缓冲源
     * @param renderType   渲染类型
     */
    public static void renderBakedModel(BakedModel model, PoseStack poseStack, MultiBufferSource bufferSource, RenderType renderType) {
        Minecraft.getInstance().getItemRenderer().renderModelLists(
                model,
                net.minecraft.world.item.ItemStack.EMPTY,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                bufferSource.getBuffer(renderType)
        );
    }

    /**
     * 获取已烘焙的模型。
     *
     * @param modelLocation 模型资源位置
     * @return 已烘焙模型
     */
    public static BakedModel getModel(ModelResourceLocation modelLocation) {
        return Minecraft.getInstance().getModelManager().getModel(modelLocation);
    }

    /**
     * 创建独立模型资源位置。
     *
     * @param location 模型资源位置（如 "servantry:itemStack/my_model"）
     * @return 模型资源位置
     */
    public static ModelResourceLocation standalone(ResourceLocation location) {
        return ModelResourceLocation.standalone(location);
    }
}
