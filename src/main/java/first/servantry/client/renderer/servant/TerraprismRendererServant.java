package first.servantry.client.renderer.servant;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import first.servantry.api.client.IServantRibbonTrailRenderer;
import first.servantry.api.client.IServantRenderer;
import first.servantry.api.servant.PathNode;
import first.servantry.api.servant.Servant;
import first.servantry.common.attachment.ServantData;
import first.servantry.common.servent.Terraprism;
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

public class TerraprismRendererServant implements IServantRibbonTrailRenderer, IServantRenderer<Terraprism> {

    @Override
    public PathNode getVisualRenderNode(Servant servant, float partialTick, PathNode rawRenderNode) {
        Terraprism terraprism = (Terraprism) servant;
        return rawRenderNode.lerp(terraprism.getInterpolatedIdleState(terraprism.getOwner(), partialTick), Mth.lerp(partialTick, terraprism.idleBlendO, terraprism.idleBlend));
    }

    @Override
    public void render(Terraprism servant, PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, int packedLight, PathNode renderNode) {
        PathNode visualNode = getVisualRenderNode(servant, partialTick, renderNode);
        poseStack.pushPose();
        Vec3 offset = visualNode.pos().subtract(renderNode.pos());
        poseStack.translate(offset.x, offset.y, offset.z);
        renderSword(servant, poseStack, bufferSource, partialTick, visualNode);
        poseStack.popPose();
    }

    private void renderSword(Terraprism servant, PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, PathNode visualNode) {
        Player owner = servant.getOwner();
        ServantData data = owner.getData(AttachmentRegister.ServantData);
        int order = data.getOrder(servant);
        int total = Math.max(1, data.getServants().size());

        float hueShift = ((float) order / total + (owner.tickCount + partialTick) * 0.015f) % 1.0f;
        float breathFactor = 0.5f + 0.5f * Mth.sin(hueShift * Mth.TWO_PI);
        float currentScale = 1.0f + 0.10f * breathFactor;

        poseStack.pushPose();
        poseStack.mulPose(Axis.YN.rotationDegrees(visualNode.yaw()));
        poseStack.mulPose(Axis.XP.rotationDegrees(visualNode.pitch()));
        poseStack.mulPose(Axis.ZP.rotationDegrees(visualNode.roll()));
        poseStack.mulPose(Axis.XP.rotationDegrees(90));
        poseStack.mulPose(Axis.ZP.rotationDegrees(-45));
        poseStack.scale(currentScale, currentScale, currentScale);

        int mColorRGB = Mth.hsvToRgb(hueShift, 0.75f - 0.35f * breathFactor, 1.0f);
        int mr = (mColorRGB >> 16) & 0xFF, mg = (mColorRGB >> 8) & 0xFF, mb = mColorRGB & 0xFF;

        MultiBufferSource safeTintedBufferSource = type -> new TintedVertexConsumer(bufferSource.getBuffer(type), mr, mg, mb, 255);

        Minecraft.getInstance().getItemRenderer().renderStatic(ItemRegister.TerraPrism.get().getDefaultInstance(), ItemDisplayContext.FIXED, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, poseStack, safeTintedBufferSource, owner.level(), 0);

        poseStack.popPose();
    }

    @Override
    public int getTrailTimer(Servant servant) {
        return ((Terraprism) servant).trailTimer;
    }

    @Override
    public int getTrailHistoryLength() {
        return 3;
    }

    /**
     * 获取轨迹颜色：与主色调一致，不使用色彩渐变，使用亮度渐变。
     * <p>
     * 颜色计算逻辑：
     * <ul>
     *   <li>色相与主剑色调一致，基于仆从顺序和时间偏移</li>
     *   <li>饱和度固定为 0.75，保持鲜艳</li>
     *   <li>亮度随进度递减：剑尖处最亮（1.0），尾部较暗（0.4）</li>
     * </ul>
     * </p>
     */
    @Override
    public int getTrailColor(Servant servant, float progress, float timeShift) {
        ServantData data = servant.getOwner().getData(AttachmentRegister.ServantData);
        int order = data.getOrder(servant);
        int total = Math.max(1, data.getServants().size());

        // 色相与主剑保持一致
        float hue = (((float) order / total) + timeShift) % 1.0f;

        // 固定饱和度，亮度随进度递减（剑尖亮，尾部暗）
        float saturation = 0.75f;
        float brightness = Mth.lerp(progress, 1.0f, 0.4f);

        return Mth.hsvToRgb(hue, saturation, brightness);
    }

    /**
     * 获取轨迹剑尖处的额外不透明度增强系数。
     * <p>
     * 剑尖处（progress 较小）的不透明度更高，使轨迹头部更加醒目。
     * </p>
     */
    @Override
    public float getTrailTipAlphaBoost(Servant servant, float progress) {
        // 剑尖处（progress < 0.3）获得额外不透明度增强
        if (progress < 0.3f) {
            return Mth.lerp(progress / 0.3f, 2.5f, 1.0f);
        }
        return 1.0f;
    }

    /**
     * 获取轨迹剑尖处的额外亮度增强系数。
     * <p>
     * 剑尖处的亮度额外提升，使轨迹头部更加明亮醒目。
     * </p>
     */
    @Override
    public float getTrailTipBrightnessBoost(Servant servant, float progress) {
        // 剑尖处（progress < 0.25）获得额外亮度增强
        if (progress < 0.25f) {
            return Mth.lerp(progress / 0.25f, 1.5f, 1.0f);
        }
        return 1.0f;
    }

    private record TintedVertexConsumer(VertexConsumer base, int r, int g, int b, int a) implements VertexConsumer {
        @Override
        public @NotNull VertexConsumer addVertex(float x, float y, float z) {
            base.addVertex(x, y, z);
            return this;
        }

        @Override
        public @NotNull VertexConsumer setColor(int r0, int g0, int b0, int a0) {
            base.setColor(this.r, this.g, this.b, this.a);
            return this;
        }

        @Override
        public @NotNull VertexConsumer setUv(float u, float v) {
            base.setUv(u, v);
            return this;
        }

        @Override
        public @NotNull VertexConsumer setUv1(int u, int v) {
            base.setUv1(u, v);
            return this;
        }

        @Override
        public @NotNull VertexConsumer setUv2(int u, int v) {
            base.setUv2(u, v);
            return this;
        }

        @Override
        public @NotNull VertexConsumer setNormal(float x, float y, float z) {
            base.setNormal(x, y, z);
            return this;
        }
    }

}