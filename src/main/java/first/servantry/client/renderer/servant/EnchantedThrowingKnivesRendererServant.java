package first.servantry.client.renderer.servant;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.PathNode;
import first.servantry.api.client.servant.AbstractServantRenderer;
import first.servantry.api.client.servant.ServantRenderConfig;
import first.servantry.api.common.attachment.ServantData;
import first.servantry.common.servent.EnchantedThrowingKnives;
import first.servantry.common.servent.Terraprism;
import first.servantry.register.AttachmentRegister;
import first.servantry.register.ItemRegister;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;

/**
 * 附魔飞刀渲染器。
 * <p>
 * 使用丝带拖尾，渲染飞刀本体和拖尾轨迹。
 * </p>
 */
public class EnchantedThrowingKnivesRendererServant extends AbstractServantRenderer<EnchantedThrowingKnives> {

    @Override
    protected ServantRenderConfig<EnchantedThrowingKnives> createConfig(EnchantedThrowingKnives servant) {
        return ServantRenderConfig.<EnchantedThrowingKnives>ribbon(servant.trailTimer, 0x88CCFF)
                .trailHistoryLength(3)
                .trailStartIndex(Math.max(0, 10 - servant.trailTimer))
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
                .modelScale(0.8f)
                .modelRotationOffset(0, 90, 0)
                .visualNodeFunction((knives, partialTick, rawNode) -> {
                    float blend = Mth.lerp(partialTick, knives.idleBlendO, knives.idleBlend);
                    var owner = knives.getOwner();
                    if (blend > 0f && owner != null) {
                        ServantData data = owner.getData(AttachmentRegister.ServantData);
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
    protected void renderModelItem(EnchantedThrowingKnives servant, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, ServantRenderConfig<EnchantedThrowingKnives> config) {
        Minecraft.getInstance().getItemRenderer().renderStatic(
                ItemRegister.EnchantedThrowingKnives.get().getDefaultInstance(),
                ItemDisplayContext.FIXED,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                bufferSource,
                servant.getOwner().level(),
                0
        );
    }

}