package first.summoner.client.attachmentEntityRenderer.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.dynamicLight.DynamicLightDispatcher;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.RenderUtil;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.entity.PathNode;
import first.summoner.common.minion.EnchantedThrowingKnives;
import first.summoner.register.SummonerModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FastColor;

/**
 * 附魔飞刀渲染器。
 * <p>
 * 使用丝带拖尾，渲染飞刀本体和拖尾轨迹。
 * </p>
 */
public class EnchantedThrowingKnivesRenderer extends AbstractAttachmentEntityRenderer<EnchantedThrowingKnives> {

    @Override
    protected RenderContext<EnchantedThrowingKnives> createContext(EnchantedThrowingKnives minion, float partialTick) {
        return RenderContext.<EnchantedThrowingKnives>builder()
                .model(new ModelConfig<EnchantedThrowingKnives>()
                               .scale(0.5f)
                               .translateOffset(-0.5f, -0.5f, -0.5f)
                               .rotationOffset(0, 90, 0))
                .build();
    }

    @Override
    protected void render(EnchantedThrowingKnives minion, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<EnchantedThrowingKnives> context, float partialTick, int packedLight, float alpha) {
        RenderUtil.renderStandalone(SummonerModelRegister.ENCHANTED_THROWING_KNIVES, poseStack, bufferSource, FastColor.ARGB32.color((int) (alpha * 255), 255, 255, 255), packedLight);
        DynamicLightDispatcher.addLightSources(visualNode.pos(), 4);
    }
}
