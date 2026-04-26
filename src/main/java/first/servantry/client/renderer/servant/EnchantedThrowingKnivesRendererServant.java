package first.servantry.client.renderer.servant;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.PathNode;
import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.ModelRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.api.common.attachment.EntityData;
import first.servantry.common.servant.EnchantedThrowingKnives;
import first.servantry.register.AttachmentRegister;
import first.servantry.register.ModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * 附魔飞刀渲染器。
 * <p>
 * 使用丝带拖尾，渲染飞刀本体和拖尾轨迹。
 * </p>
 */
public class EnchantedThrowingKnivesRendererServant extends AbstractAttachmentEntityRenderer<EnchantedThrowingKnives> {

    @Override
    protected RenderContext<EnchantedThrowingKnives> createContext(EnchantedThrowingKnives servant) {
        int trailTimer = servant.attacking ? servant.trailTimer : 0;
        return RenderContext.<EnchantedThrowingKnives>ribbon(trailTimer, 0x88CCFF)
                .trailHistoryLength(3)
                .ribbonDiamondSize(0.25f)
                .ribbonWidth(0.225f)
                .trailStartIndex(Math.max(0, 10 - trailTimer))
                .trailColorFunction((s, progress, timeShift) -> {
                    // 淡蓝色基调 (136, 204, 255)
                    int r = 136;
                    int g = 204;
                    int b = 255;

                    // 亮度随进度递减
                    float brightness = Mth.lerp(progress, 1.0f, 0.5f);
                    r = Math.round(r * brightness);
                    g = Math.round(g * brightness);
                    b = Math.round(b * brightness);

                    return (r << 16) | (g << 8) | b;
                })
                .trailTipAlphaBoost((s, progress) -> {
                    if (progress < 0.3f) {
                        return Mth.lerp(progress / 0.3f, 2.0f, 1.0f);
                    }
                    return 1.0f;
                })
                .trailTipBrightnessBoost((s, progress) -> {
                    if (progress < 0.25f) {
                        return Mth.lerp(progress / 0.25f, 1.3f, 1.0f);
                    }
                    return 1.0f;
                })
                .modelTranslateOffset(-0.25f, -0.25f, -0.25f)
                .modelScale(0.5f)
                .modelRotationOffset(0, 90, 0)
                .visualNodeFunction((knives, partialTick, rawNode) -> {
                    float blend = Mth.lerp(partialTick, knives.idleBlendO, knives.idleBlend);
                    var owner = knives.getOwner();
                    if (blend > 0f && owner != null) {
                        EntityData data = owner.getData(AttachmentRegister.EntityData);
                        PathNode idealNode = knives.getInterpolatedIdleState(owner, data.getOrder(knives), Math.max(1, data.getSameSize(knives)), partialTick);
                        Vec3 pos = rawNode.pos().lerp(idealNode.pos(), blend);
                        float yaw = Mth.rotLerp(blend, rawNode.yaw(), idealNode.yaw());
                        float pitch = Mth.rotLerp(blend, rawNode.pitch(), idealNode.pitch());
                        float roll = Mth.rotLerp(blend, rawNode.roll(), idealNode.roll());
                        return new PathNode(pos, yaw, pitch, roll);
                    }
                    return rawNode;
                });
    }

    @Override
    protected void renderEntity(EnchantedThrowingKnives servant, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<EnchantedThrowingKnives> config) {
        ModelRenderer.renderModel(ModelRegister.ENCHANTED_THROWING_KNIVES, poseStack, bufferSource);
    }

}