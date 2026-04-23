package first.servantry.client.renderer.servant;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import first.servantry.api.client.IServantRibbonTrailRenderer;
import first.servantry.api.client.IServantRenderer;
import first.servantry.api.servant.PathNode;
import first.servantry.api.servant.Servant;
import first.servantry.common.attachment.ServantData;
import first.servantry.common.servent.EnchantedThrowingKnives;
import first.servantry.register.AttachmentRegister;
import first.servantry.register.ItemRegister;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * 附魔飞刀渲染器。
 * <p>
 * 实现丝带拖尾渲染效果，渲染飞刀本体和拖尾轨迹。
 * </p>
 */
public class EnchantedThrowingKnivesRendererServant implements IServantRibbonTrailRenderer, IServantRenderer<EnchantedThrowingKnives> {

    @Override
    public PathNode getVisualRenderNode(Servant servant, float partialTick, PathNode rawRenderNode) {
        EnchantedThrowingKnives knives = (EnchantedThrowingKnives) servant;
        float blend = Mth.lerp(partialTick, knives.idleBlendO, knives.idleBlend);
        Player owner = knives.getOwner();
        if (blend > 0f && owner != null) {
            ServantData data = owner.getData(AttachmentRegister.ServantData);
            PathNode idealNode = knives.getInterpolatedIdleState(owner, data.getOrder(knives), Math.max(1, data.getSameSize(knives)), partialTick);
            Vec3 pos = rawRenderNode.pos().lerp(idealNode.pos(), blend);
            float yaw = Mth.rotLerp(blend, rawRenderNode.yaw(), idealNode.yaw());
            float pitch = Mth.rotLerp(blend, rawRenderNode.pitch(), idealNode.pitch());
            float roll = Mth.rotLerp(blend, rawRenderNode.roll(), idealNode.roll());
            return new PathNode(pos, yaw, pitch, roll);
        }
        return rawRenderNode;
    }

    @Override
    public void render(EnchantedThrowingKnives servant, PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, int packedLight, PathNode renderNode) {
        PathNode visualNode = getVisualRenderNode(servant, partialTick, renderNode);
        poseStack.pushPose();
        Vec3 offset = visualNode.pos().subtract(renderNode.pos());
        poseStack.translate(offset.x, offset.y, offset.z);
        renderKnife(servant, poseStack, bufferSource, partialTick, visualNode);
        poseStack.popPose();
    }

    private void renderKnife(EnchantedThrowingKnives servant, PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, PathNode visualNode) {
        Player owner = servant.getOwner();
        poseStack.pushPose();
        poseStack.mulPose(Axis.YN.rotationDegrees(visualNode.yaw()));
        poseStack.mulPose(Axis.XP.rotationDegrees(visualNode.pitch()));
        poseStack.mulPose(Axis.ZP.rotationDegrees(visualNode.roll()));
        poseStack.mulPose(Axis.XP.rotationDegrees(90));
        poseStack.scale(0.8f, 0.8f, 0.8f);

        Minecraft.getInstance().getItemRenderer().renderStatic(
                ItemRegister.EnchantedThrowingKnives.get().getDefaultInstance(),
                ItemDisplayContext.FIXED,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                bufferSource,
                owner.level(),
                0
        );

        poseStack.popPose();
    }

    @Override
    public int getTrailTimer(Servant servant) {
        return ((EnchantedThrowingKnives) servant).trailTimer;
    }

    @Override
    public int getTrailHistoryLength() {
        return 3;
    }

    @Override
    public int getTrailStartIndex(Servant servant) {
        return Math.max(0, 10 - getTrailTimer(servant));
    }

    /**
     * 获取轨迹颜色：淡蓝色渐变。
     */
    @Override
    public int getTrailColor(Servant servant, float progress, float timeShift) {
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
    }

    /**
     * 获取轨迹剑尖处的额外不透明度增强系数。
     */
    @Override
    public float getTrailTipAlphaBoost(Servant servant, float progress) {
        if (progress < 0.3f) {
            return Mth.lerp(progress / 0.3f, 2.0f, 1.0f);
        }
        return 1.0f;
    }

    /**
     * 获取轨迹剑尖处的额外亮度增强系数。
     */
    @Override
    public float getTrailTipBrightnessBoost(Servant servant, float progress) {
        if (progress < 0.25f) {
            return Mth.lerp(progress / 0.25f, 1.3f, 1.0f);
        }
        return 1.0f;
    }

}
