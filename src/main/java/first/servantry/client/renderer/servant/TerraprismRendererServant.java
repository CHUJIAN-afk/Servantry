package first.servantry.client.renderer.servant;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import first.servantry.api.PathNode;
import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.api.common.attachment.EntityData;
import first.servantry.common.servent.Terraprism;
import first.servantry.register.AttachmentRegister;
import first.servantry.register.ItemRegister;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import org.jetbrains.annotations.NotNull;

/**
 * 泰拉棱镜渲染器。
 * <p>
 * 使用丝带拖尾，渲染带色调渐变的棱镜模型。
 */
public class TerraprismRendererServant extends AbstractAttachmentEntityRenderer<Terraprism> {

    @Override
    protected RenderContext<Terraprism> createContext(Terraprism servant) {
        int timer = servant.attacking ? servant.trailTimer : 0;
        return RenderContext.<Terraprism>ribbon(timer, 0xFFFFFF)
                .trailHistoryLength(4)
                .trailSegmentsPerNode(4)
                .ribbonWidth(0.75f)
                .ribbonDiamondSize(0.15f)
                .trailShaderType(RenderContext.ShaderType.UNLIT)
                .trailColorFunction((terraprism, progress, timeShift) -> {
                    EntityData data = terraprism.getOwner().getData(AttachmentRegister.EntityData);
                    int order = data.getOrder(terraprism);
                    int total = Math.max(1, data.getSameSize(terraprism));
                    float hue = (((float) order / total) + timeShift) % 1.0f;
                    float saturation = 0.55f;
                    float brightness = Mth.lerp(progress, 1f, 0.4f);
                    return Mth.hsvToRgb(hue, saturation, brightness);
                })
                .trailTipAlphaBoost((s, progress) -> {
                    if (progress < 0.3f) {
                        return Mth.lerp(progress / 0.3f, 2.5f, 1.0f);
                    }
                    return 1.0f;
                })
                .trailTipBrightnessBoost((s, progress) -> {
                    if (progress < 0.25f) {
                        return Mth.lerp(progress / 0.25f, 1.5f, 1.0f);
                    }
                    return 1.0f;
                })
                .modelScale(1.0f)
                .modelRotationOffset(0, 90, -45)
                .visualNodeFunction((terraprism, partialTick, rawNode) ->
                        rawNode.lerp(terraprism.getInterpolatedIdleState(partialTick),
                                Mth.lerp(partialTick, terraprism.idleBlendO, terraprism.idleBlend)));
    }

    @Override
    protected void renderEntity(Terraprism terraprism, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<Terraprism> config) {
        // 计算色调和呼吸效果
        var owner = terraprism.getOwner();
        EntityData data = terraprism.getOwner().getData(AttachmentRegister.EntityData);
        int order = data.getOrder(terraprism);
        int total = Math.max(1, data.getSameSize(terraprism));

        float hueShift = ((float) order / total + (owner.tickCount) * 0.015f) % 1.0f;
        float breathFactor = 0.5f + 0.5f * Mth.sin(hueShift * Mth.TWO_PI);
        float currentScale = 1.0f + 0.10f * breathFactor;

        poseStack.scale(currentScale, currentScale, currentScale);

        int mColorRGB = Mth.hsvToRgb(hueShift, 0.75f - 0.35f * breathFactor, 1.0f);
        int mr = (mColorRGB >> 16) & 0xFF, mg = (mColorRGB >> 8) & 0xFF, mb = mColorRGB & 0xFF;

        Minecraft.getInstance().getItemRenderer().renderStatic(
                ItemRegister.TerraPrism.get().getDefaultInstance(),
                ItemDisplayContext.FIXED,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                type -> new TintedVertexConsumer(bufferSource.getBuffer(type), mr, mg, mb, 255),
                owner.level(),
                0
        );

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