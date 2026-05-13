package first.servantry.client.attachmentEntityRenderer.servant;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.PathNode;
import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.ModelRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.api.client.render.renderConfig.ModelConfig;
import first.servantry.common.servant.StardustDragon;
import first.servantry.register.ModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.ModelResourceLocation;

/**
 * 星尘龙渲染器。
 * <p>
 * 根据体节索引决定渲染内容：
 * <ul>
 *   <li>头部</li>
 *   <li>中段</li>
 *   <li>尾部</li>
 * </ul>
 * 不渲染轨迹。
 * </p>
 */
public class StardustDragonRenderer extends AbstractAttachmentEntityRenderer<StardustDragon> {

    @Override
    protected RenderContext<StardustDragon> createContext(StardustDragon dragon) {
        int total = dragon.getTotalSegments();
        int index = dragon.getSegmentIndex();
        boolean isTail = index == total - 1;
        return RenderContext.<StardustDragon>builder()
                .model(new ModelConfig<StardustDragon>()
                        .scale(dragon.getScale())
                        .translateOffset(-0.5f, isTail ? -0.4845f : -0.425f, isTail ? -0.103075f : -0.5f)
                        .rotationOffset(180, 0, 0)
                        .alphaDistanceFactor(dragon.getScale()))
                .build();
    }

    @Override
    protected void renderEntity(StardustDragon dragon, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<StardustDragon> config) {
        int total = dragon.getTotalSegments();
        int index = dragon.getSegmentIndex();
        ModelResourceLocation model;
        if (index == 0) {
            model = ModelRegister.STARDUST_DRAGON_HEAD;
        } else if (index == total - 1) {
            model = ModelRegister.STARDUST_DRAGON_BODY3;
        } else if (index % 2 == 0) {
            model = ModelRegister.STARDUST_DRAGON_BODY1;
        } else {
            model = ModelRegister.STARDUST_DRAGON_BODY2;
        }
        ModelRenderer.renderModel(model, poseStack, bufferSource);
    }

}