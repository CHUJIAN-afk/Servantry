package first.summoner.client.attachmentEntityRenderer.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.dynamicLight.DynamicLightDispatcher;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.RenderUtil;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.client.render.trail.RibbonTrailConfig;
import first.lyra.common.entity.PathNode;
import first.summoner.common.minion.Terraprism;
import first.summoner.register.SummonerModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FastColor;

public class TerraprismRenderer extends AbstractAttachmentEntityRenderer<Terraprism> {

    @Override
    protected RenderContext<Terraprism> createContext(Terraprism minion, float partialTick) {
        return RenderContext.<Terraprism>builder()
                .trail(new RibbonTrailConfig<Terraprism>()
                               .timer(minion.attacking ? minion.trailTimer : 0)
                               .colorRGB(minion.getColor(partialTick))
                               .segmentsPerNode(4)
                               .historyLength(6)
                               .upOffset(1.015f))
                .model(new ModelConfig<Terraprism>()
                               .scale(1.5f)
                               .translateOffset(-0.5f, -0.5f, -0.5f)
                               .rotationOffset(0, 90, 45))
                .build();
    }

    @Override
    protected void render(Terraprism terraprism, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<Terraprism> context, float partialTick, int packedLight, float alpha) {
        int color = terraprism.getColor(partialTick);
        RenderUtil.renderStandalone(SummonerModelRegister.TERRAPRISM, poseStack, bufferSource, FastColor.ARGB32.color((int) (alpha * 255), FastColor.ARGB32.red(color), FastColor.ARGB32.green(color), FastColor.ARGB32.blue(color)), RenderUtil.FULL_LIGHT);
        DynamicLightDispatcher.addLightSources(visualNode.pos(), 8);
    }
}
