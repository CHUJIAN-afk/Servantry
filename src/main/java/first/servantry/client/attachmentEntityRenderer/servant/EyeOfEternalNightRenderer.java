package first.servantry.client.attachmentEntityRenderer.servant;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.PathNode;
import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.ModelRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.api.client.render.renderConfig.ModelConfig;
import first.servantry.common.servant.EyeOfEternalNight;
import first.servantry.register.ModelRegister;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;

/**
 * 永夜之眼渲染器 - 环绕仆从本体 + 激光射线渲染。
 */
public class EyeOfEternalNightRenderer extends AbstractAttachmentEntityRenderer<EyeOfEternalNight> {

    @Override
    protected RenderContext<EyeOfEternalNight> createContext(EyeOfEternalNight servant) {
        float partialTick = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
        float scale = Mth.lerp(partialTick, servant.getPreShootCooldown(), servant.getShootCooldown()) / 128;
        return RenderContext.<EyeOfEternalNight>builder()
                .model(new ModelConfig<EyeOfEternalNight>()
                        .scale(0.5f + scale)
                        .translateOffset(-0.5f, -0.5f, -0.5f)
                        .alphaDistanceFactor(1.5f)
                )
                .build();
    }

    @Override
    protected void renderEntity(EyeOfEternalNight servant, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<EyeOfEternalNight> config) {
        ModelRenderer.renderModel(ModelRegister.STARDUST_CELL, poseStack, bufferSource);
    }
}
