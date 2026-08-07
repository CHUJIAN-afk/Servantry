package first.servantry.client.attachmentEntityRenderer.servant;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.dynamicLight.DynamicLightDispatcher;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.ModelRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.TintedVertexConsumer;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.client.render.trail.RibbonTrailConfig;
import first.lyra.common.entity.PathNode;
import first.servantry.common.servant.Terraprism;
import first.servantry.register.ServantryModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FastColor;

/**
 * 泰拉棱镜渲染器。
 * <p>
 * 使用丝带拖尾，渲染带色调渐变的棱镜模型。
 */
public class TerraprismRenderer extends AbstractAttachmentEntityRenderer<Terraprism> {

    @Override
    protected RenderContext<Terraprism> createContext(Terraprism servant) {
        return RenderContext.<Terraprism>builder()
                .trail(new RibbonTrailConfig<Terraprism>()
                               .timer(servant.attacking ? servant.trailTimer : 0)
                               .colorRGB(0xFFFFFF)
                               .segmentsPerNode(4)
                               .historyLength(5)
                               .upOffset(1.015f)
                               .colorFunction((terraprism, progress, partialTick) -> terraprism.getColor(partialTick)))
                .model(new ModelConfig<Terraprism>()
                               .scale(1.5f)
                               .translateOffset(-0.5f, -0.5f, -0.5f)
                               .rotationOffset(0, 90, 45))
                .build();
    }

    @Override
    protected void render(Terraprism terraprism, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<Terraprism> context, float partialTick) {
        int color = terraprism.getColor(partialTick);
        int red = FastColor.ARGB32.red(color);
        int green = FastColor.ARGB32.green(color);
        int blue = FastColor.ARGB32.blue(color);
        ModelRenderer.renderModel(ServantryModelRegister.TERRAPRISM, poseStack, type -> new TintedVertexConsumer(bufferSource.getBuffer(type), red, green, blue, 255));
        DynamicLightDispatcher.addLightSources(visualNode.pos(), 8);
    }
}