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
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;

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
                               .segmentsPerNode(16)
                               .historyLength(5)
                               .upOffset(1.015f)
                               .colorFunction((terraprism, progress, timeShift) -> {
                                   float partialTick = Minecraft.getInstance()
                                           .getTimer()
                                           .getGameTimeDeltaPartialTick(true);
                                   return terraprism.getColor(partialTick);
                               }))
                .model(new ModelConfig<Terraprism>()
                               .scale(1.5f)
                               .translateOffset(-0.5f, -0.5f, -0.5f)
                               .rotationOffset(0, 90, 45)
                               .visualNodeFunction((terraprism, partialTick, rawNode) -> rawNode.lerp(terraprism.getInterpolatedIdleState(partialTick), Mth.lerp(partialTick, terraprism.idleBlendO, terraprism.idleBlend))))
                .build();
    }

    @Override
    protected void renderEntityModel(Terraprism terraprism, PoseStack poseStack, MultiBufferSource bufferSource, PathNode node, RenderContext<Terraprism> config) {
        super.renderEntityModel(terraprism, poseStack, bufferSource, node, config);
    }

    @Override
    protected void renderEntity(Terraprism terraprism, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<Terraprism> config) {
        int mColorRGB = terraprism.getColor(Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true));
        int mr = (mColorRGB >> 16) & 0xFF, mg = (mColorRGB >> 8) & 0xFF, mb = mColorRGB & 0xFF;
        ModelRenderer.renderModel(ModelRegister.TERRAPRISM, poseStack, type -> new TintedVertexConsumer(bufferSource.getBuffer(type), mr, mg, mb, 255));
    }
}