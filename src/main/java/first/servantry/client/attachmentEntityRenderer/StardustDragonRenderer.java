package first.servantry.client.attachmentEntityRenderer;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.PathNode;
import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.ModelRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.common.servant.StardustDragon;
import first.servantry.register.ModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.ModelResourceLocation;

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
        int total = dragon.getTotalSegments();
        int index = dragon.getSegmentIndex();
        boolean b = index == total - 1;
        return RenderContext.<StardustDragon>none()
                .modelScale(dragon.getScale())
                .modelTranslateOffset(-0.5f, b ? -0.4845f : -0.425f, b ? -0.103075f : -0.5f)
                .alphaDistanceFactor(dragon.getScale())
                .modelRotationOffset(180, 0, 0);
    }

    @Override
    protected void renderEntity(StardustDragon dragon, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<StardustDragon> config) {
        int total = dragon.getTotalSegments();
        int index = dragon.getSegmentIndex();
        ModelResourceLocation model;
        // 多体节渲染逻辑
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
