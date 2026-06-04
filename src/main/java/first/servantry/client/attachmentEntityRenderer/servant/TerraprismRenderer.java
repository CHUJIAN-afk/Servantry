package first.servantry.client.attachmentEntityRenderer.servant;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.ModelRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.api.client.render.TintedVertexConsumer;
import first.servantry.api.client.render.renderConfig.ModelConfig;
import first.servantry.api.client.render.renderConfig.RibbonTrailConfig;
import first.servantry.api.entity.PathNode;
import first.servantry.common.servant.Terraprism;
import first.servantry.register.ModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

/**
 * 泰拉棱镜渲染器。
 * <p>
 * 使用丝带拖尾，渲染带色调渐变的棱镜模型。
 */
public class TerraprismRenderer extends AbstractAttachmentEntityRenderer<Terraprism> {

    @Override
    protected RenderContext<Terraprism> createContext(Terraprism servant) {
        int timer = servant.attacking ? servant.trailTimer : 0;
        return RenderContext.<Terraprism>builder()
                .trail(new RibbonTrailConfig<Terraprism>()
                        .timer(timer)
                        .colorRGB(0xFFFFFF)
                        .historyLength(4)
                        .width(1.0575f)
                        .diamondSize(0.15f)
                        .colorFunction((terraprism, progress, timeShift) -> {
                            int order = terraprism.getOrder();
                            int total = Math.max(1, terraprism.getSameSize());
                            float hue = (((float) order / total) + timeShift) % 1.0f;
                            return Mth.hsvToRgb(hue, 0.65f, Mth.lerp(progress, 1f, 0.4f));
                        })
                        .tipAlphaBoost((s, progress) -> progress < 0.3f ? Mth.lerp(progress / 0.3f, 2.5f, 1.0f) : 1.5f)
                        .tipBrightnessBoost((s, progress) -> progress < 0.25f ? Mth.lerp(progress / 0.25f, 1.5f, 1.0f) : 1.5f))
                .model(new ModelConfig<Terraprism>()
                        .scale(1.5f)
                        .translateOffset(-0.5f, -0.5f, -0.5f)
                        .rotationOffset(0, 90, 45)
                        .visualNodeFunction((terraprism, partialTick, rawNode) -> rawNode.lerp(terraprism.getInterpolatedIdleState(partialTick), Mth.lerp(partialTick, terraprism.idleBlendO, terraprism.idleBlend))))
                .build();
    }

    @Override
    protected void renderEntity(Terraprism terraprism, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<Terraprism> config) {
        Player owner = terraprism.getOwner();
        int order = terraprism.getOrder();
        int total = Math.max(1, terraprism.getSameSize());
        float hueShift = ((float) order / total + owner.tickCount * 0.015f) % 1.0f;
        float breathFactor = 0.5f + 0.5f * Mth.sin(hueShift * Mth.TWO_PI);
        int mColorRGB = Mth.hsvToRgb(hueShift, 0.75f - 0.35f * breathFactor, 1.0f);
        int mr = (mColorRGB >> 16) & 0xFF, mg = (mColorRGB >> 8) & 0xFF, mb = mColorRGB & 0xFF;
        ModelRenderer.renderModel(ModelRegister.TERRAPRISM, poseStack, type -> new TintedVertexConsumer(bufferSource.getBuffer(type), mr, mg, mb, 255));
    }

}