package first.servantry.common.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import first.servantry.api.client.IRibbonTrailRenderer;
import first.servantry.api.client.ServantRenderer;
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

public class TerraprismRenderer extends ServantRenderer<Terraprism> implements IRibbonTrailRenderer {

    @Override
    public PathNode getVisualRenderNode(Servant servant, float partialTick, PathNode rawRenderNode) {
        Terraprism terraprism = (Terraprism) servant;
        return rawRenderNode.lerp(terraprism.getInterpolatedIdleState(terraprism.getOwner(), partialTick), Mth.lerp(partialTick, terraprism.getIdleBlendO(), terraprism.getIdleBlend()));
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
        return ((Terraprism) servant).getTrailTimer();
    }

    @Override
    public int getTrailColor(Servant servant, float progress, float timeShift) {
        ServantData data = servant.getOwner().getData(AttachmentRegister.ServantData);
        int order = data.getOrder(servant);
        int total = Math.max(1, data.getServants().size());
        float hue = (progress * 0.85f + ((float) order / total + timeShift)) % 1.0f;
        return Mth.hsvToRgb(hue, 0.45f, 0.65f);
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