package first.servantry.client.attachmentEntityRenderer.servant;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.dynamicLight.DynamicLightDispatcher;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.ModelRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.client.render.trail.RibbonTrailConfig;
import first.lyra.common.entity.PathNode;
import first.servantry.common.servant.EnchantedThrowingKnives;
import first.servantry.register.ServantryModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;

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
                               .rotationOffset(0, 90, 0))
                .build();
    }

    @Override
    protected void render(EnchantedThrowingKnives servant, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<EnchantedThrowingKnives> context, float partialTick) {
        ModelRenderer.renderModel(ServantryModelRegister.ENCHANTED_THROWING_KNIVES, poseStack, bufferSource);
        DynamicLightDispatcher.addLightSources(visualNode.pos(), 8);
    }
}
