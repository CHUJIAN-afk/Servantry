package first.servantry.client.attachmentEntityRenderer.servant;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.ModelRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.api.client.render.renderConfig.ModelConfig;
import first.servantry.api.client.render.renderConfig.RibbonTrailConfig;
import first.servantry.api.entity.PathNode;
import first.servantry.common.servant.EnchantedThrowingKnives;
import first.servantry.register.ModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;

/**
 * 附魔飞刀渲染器。
 * <p>
 * 使用丝带拖尾，渲染飞刀本体和拖尾轨迹。
 * </p>
 */
public class EnchantedThrowingKnivesRenderer extends AbstractAttachmentEntityRenderer<EnchantedThrowingKnives> {

    @Override
    protected RenderContext<EnchantedThrowingKnives> createContext(EnchantedThrowingKnives servant) {
        return RenderContext.<EnchantedThrowingKnives>builder()
                .trail(new RibbonTrailConfig<EnchantedThrowingKnives>()
                               .timer(servant.trailTimer)
                               .colorRGB(0x7759ff)
                               .historyLength(4)
                               .upOffset(0.225f))
                .model(new ModelConfig<EnchantedThrowingKnives>()
                               .scale(0.5f)
                               .translateOffset(-0.5f, -0.5f, -0.5f)
                               .rotationOffset(0, 90, 0)
                               .visualNodeFunction((knives, partialTick, rawNode) -> rawNode.lerp(knives.getRenderNode(partialTick), Mth.lerp(partialTick, knives.idleBlendO, knives.idleBlendO))))
                .build();
    }

    @Override
    protected void renderEntity(EnchantedThrowingKnives servant, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<EnchantedThrowingKnives> config) {
        ModelRenderer.renderModel(ModelRegister.ENCHANTED_THROWING_KNIVES, poseStack, bufferSource);
    }

}
