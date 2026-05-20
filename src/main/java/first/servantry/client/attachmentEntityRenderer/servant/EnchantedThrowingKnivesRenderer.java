package first.servantry.client.attachmentEntityRenderer.servant;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.PathNode;
import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.ModelRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.api.client.render.renderConfig.ModelConfig;
import first.servantry.api.client.render.renderConfig.RibbonTrailConfig;
import first.servantry.common.servant.EnchantedThrowingKnives;
import first.servantry.register.ModelRegister;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
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
        int trailTimer = servant.attacking ? servant.trailTimer : 0;
        BakedModel bakedModel = Minecraft.getInstance().getModelManager().getModel(ModelRegister.ENCHANTED_THROWING_KNIVES);
        int dominantColor = InfiniteShadowRenderer.extractDominantColor(bakedModel);
        return RenderContext.<EnchantedThrowingKnives>builder()
                .trail(new RibbonTrailConfig<EnchantedThrowingKnives>()
                        .timer(trailTimer)
                        .colorRGB(dominantColor)
                        .historyLength(4)
                        .width(0.225f)
                        .diamondSize(0.25f)
                        .colorFunction((s, progress, timeShift) -> {
                            float brightness = Mth.lerp(progress, 1f, 0.4f);
                            int r = (int) (((dominantColor >> 16) & 0xFF) * brightness);
                            int g = (int) (((dominantColor >> 8) & 0xFF) * brightness);
                            int b = (int) ((dominantColor & 0xFF) * brightness);
                            return (r << 16) | (g << 8) | b;
                        })
                        .tipAlphaBoost((s, progress) -> progress < 0.3f ? Mth.lerp(progress / 0.3f, 2.0f, 1.0f) : 1.0f)
                        .tipBrightnessBoost((s, progress) -> progress < 0.25f ? Mth.lerp(progress / 0.25f, 1.3f, 1.0f) : 1.0f))
                .model(new ModelConfig<EnchantedThrowingKnives>()
                        .scale(0.5f)
                        .translateOffset(-0.5f, -0.5f, -0.5f)
                        .rotationOffset(0, 90, 0)
                        .visualNodeFunction((knives, partialTick, rawNode) -> rawNode.lerp(knives.getInterpolatedIdleState(partialTick), Mth.lerp(partialTick, knives.idleBlendO, knives.idleBlend))))
                .build();
    }

    @Override
    protected void renderEntity(EnchantedThrowingKnives servant, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<EnchantedThrowingKnives> config) {
        ModelRenderer.renderModel(ModelRegister.ENCHANTED_THROWING_KNIVES, poseStack, bufferSource);
    }

}
