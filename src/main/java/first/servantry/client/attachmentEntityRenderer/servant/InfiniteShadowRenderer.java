package first.servantry.client.attachmentEntityRenderer.servant;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.PathNode;
import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.ModelRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.api.client.render.renderConfig.ModelConfig;
import first.servantry.api.client.render.renderConfig.RibbonTrailConfig;
import first.servantry.api.common.attachment.EntityData;
import first.servantry.common.servant.InfiniteShadow;
import first.servantry.register.AttachmentRegister;
import first.servantry.register.ModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public class InfiniteShadowRenderer extends AbstractAttachmentEntityRenderer<InfiniteShadow> {

    @Override
    protected RenderContext<InfiniteShadow> createContext(InfiniteShadow infiniteShadow) {
        int timer = infiniteShadow.attacking ? infiniteShadow.trailTimer : 0;
        return RenderContext.<InfiniteShadow>builder()
                .trail(new RibbonTrailConfig<InfiniteShadow>()
                        .timer(timer)
                        .colorRGB(0xFFFFFF)
                        .historyLength(4)
                        .width(0.7075f)
                        .diamondSize(0.15f)
                        .colorFunction((terraprism, progress, timeShift) -> {
                            EntityData data = terraprism.getOwner().getData(AttachmentRegister.EntityData);
                            int order = data.getOrder(terraprism);
                            int total = Math.max(1, data.getSameSize(terraprism));
                            float hue = (((float) order / total) + timeShift) % 1.0f;
                            return Mth.hsvToRgb(hue, 0.65f, Mth.lerp(progress, 1f, 0.4f));
                        })
                        .tipAlphaBoost((s, progress) -> progress < 0.3f ? Mth.lerp(progress / 0.3f, 2.5f, 1.0f) : 1.0f)
                        .tipBrightnessBoost((s, progress) -> progress < 0.25f ? Mth.lerp(progress / 0.25f, 1.5f, 1.0f) : 1.0f))
                .model(new ModelConfig<InfiniteShadow>()
                        .translateOffset(-0.5f, -0.5f, -0.5f)
                        .rotationOffset(0, 90, 45)
                        .visualNodeFunction((terraprism, partialTick, rawNode) -> rawNode.lerp(terraprism.getInterpolatedIdleState(partialTick), Mth.lerp(partialTick, terraprism.idleBlendO, terraprism.idleBlend))))
                .build();
    }

    @Override
    protected void renderEntity(InfiniteShadow infiniteShadow, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<InfiniteShadow> config) {
        Player owner = infiniteShadow.getOwner();
        EntityData data = owner.getData(AttachmentRegister.EntityData);
        int order = data.getOrder(infiniteShadow);
        int total = Math.max(1, data.getSameSize(infiniteShadow));

        float hueShift = ((float) order / total + owner.tickCount * 0.015f) % 1.0f;
        float breathFactor = 0.5f + 0.5f * Mth.sin(hueShift * Mth.TWO_PI);
        int mColorRGB = Mth.hsvToRgb(hueShift, 0.75f - 0.35f * breathFactor, 1.0f);
        int mr = (mColorRGB >> 16) & 0xFF, mg = (mColorRGB >> 8) & 0xFF, mb = mColorRGB & 0xFF;
        ModelRenderer.renderModel(ModelRegister.TERRAPRISM, poseStack, type -> new TerraprismRenderer.TintedVertexConsumer(bufferSource.getBuffer(type), mr, mg, mb, 255));
    }
}
