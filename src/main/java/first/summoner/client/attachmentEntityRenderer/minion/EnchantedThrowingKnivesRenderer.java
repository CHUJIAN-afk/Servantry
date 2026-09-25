package first.summoner.client.attachmentEntityRenderer.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.dynamicLight.DynamicLightDispatcher;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.ModelContext;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.model.LyraModelRenderer;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.common.minion.minion.EnchantedThrowingKnives;
import first.summoner.register.SummonerModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * 附魔飞刀渲染器。
 * <p>
 * 使用丝带拖尾，渲染飞刀本体和拖尾轨迹。
 * </p>
 */
public class EnchantedThrowingKnivesRenderer extends AbstractAttachmentEntityRenderer<EnchantedThrowingKnives> {

    @Override
    protected RenderContext<EnchantedThrowingKnives> createContext(EnchantedThrowingKnives minion, PathNode visualNode, float partialTick, int packedLight) {
        return super.createContext(minion, visualNode, partialTick, packedLight)
                .model(new ModelContext()
                               .scale(0.5f)
                               .translateOffset(-0.5f, -0.5f, -0.5f)
                               .rotationOffset(0, 90, 0));
    }

    @Override
    protected void render(PoseStack poseStack, MultiBufferSource bufferSource) {
        LyraModelRenderer.json(SummonerModelRegister.ENCHANTED_THROWING_KNIVES)
                .color(context.color.argbInt())
                .light(context.packedLight)
                .render(poseStack, bufferSource);
        DynamicLightDispatcher.addLightSources(context.visualNode.pos(), 4);
    }
}
