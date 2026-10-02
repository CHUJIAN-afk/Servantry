package first.summoner.client.attachmentEntityRenderer.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.api.LyraAPI;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.ModelContext;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.RenderUtil;
import first.lyra.client.render.model.LyraModelRenderer;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.common.minion.minion.StardustDragon;
import first.summoner.register.SummonerModelRegister;
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
    protected RenderContext<StardustDragon> createContext(StardustDragon dragon, PathNode visualNode, float partialTick, int packedLight) {
        int total = Math.max(1, dragon.getSameSize());
        int index = dragon.getOrder();
        boolean isTail = index == total - 1;
        return super.createContext(dragon, visualNode, partialTick, packedLight)
                .model(new ModelContext()
                               .translateOffset(-0.5f, isTail ? -0.4845f : -0.425f, isTail ? -0.103075f : -0.5f)
                               .rotationOffset(180, 0, 0));
    }

    @Override
    protected void render(PoseStack poseStack, MultiBufferSource bufferSource) {
        int total = Math.max(1, context.entity.getSameSize());
        int index = context.entity.getOrder();
        ModelResourceLocation model;
        if (index == 0) {
            model = SummonerModelRegister.STARDUST_DRAGON_HEAD;
        } else if (index == total - 1) {
            model = SummonerModelRegister.STARDUST_DRAGON_BODY3;
        } else if (index % 2 == 0) {
            model = SummonerModelRegister.STARDUST_DRAGON_BODY1;
        } else {
            model = SummonerModelRegister.STARDUST_DRAGON_BODY2;
        }
        LyraModelRenderer.json(model)
                .color(context.color.argbInt())
                .light(RenderUtil.FULL_LIGHT)
                .render(poseStack, bufferSource);
        LyraAPI.light(context.visualNode.pos(), 8);
    }

}
