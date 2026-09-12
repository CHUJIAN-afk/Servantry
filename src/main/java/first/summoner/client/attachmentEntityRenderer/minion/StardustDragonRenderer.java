package first.summoner.client.attachmentEntityRenderer.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.dynamicLight.DynamicLightDispatcher;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.RenderUtil;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.entity.PathNode;
import first.summoner.common.minion.StardustDragon;
import first.summoner.register.SummonerModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.util.FastColor;

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
    protected RenderContext<StardustDragon> createContext(StardustDragon dragon, float partialTick) {
        int total = Math.max(1, dragon.getSameSize());
        int index = dragon.getSameOrder();
        boolean isTail = index == total - 1;
        return RenderContext.<StardustDragon>builder()
                .model(new ModelConfig<StardustDragon>()
                        .translateOffset(-0.5f, isTail ? -0.4845f : -0.425f, isTail ? -0.103075f : -0.5f)
                        .rotationOffset(180, 0, 0))
                .build();
    }

    @Override
    protected void render(StardustDragon dragon, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<StardustDragon> context, float partialTick, int packedLight, float alpha) {
        int total = Math.max(1, dragon.getSameSize());
        int index = dragon.getSameOrder();
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
        RenderUtil.renderStandalone(model, poseStack, bufferSource, FastColor.ARGB32.color((int) (alpha * 255), 255, 255, 255), RenderUtil.FULL_LIGHT);
        DynamicLightDispatcher.addLightSources(visualNode.pos(), 8);
    }

}
