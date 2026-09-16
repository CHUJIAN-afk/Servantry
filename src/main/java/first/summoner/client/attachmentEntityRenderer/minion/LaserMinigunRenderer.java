package first.summoner.client.attachmentEntityRenderer.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.model.LyraModelRenderer;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.Summoner;
import first.summoner.common.minion.minion.LaserMinigun;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FastColor;

/**
 * 激光机枪渲染器 - 当前使用 BBModel 验证模型、UV 与循环动画链路。
 */
public class LaserMinigunRenderer extends AbstractAttachmentEntityRenderer<LaserMinigun> {

    @Override
    protected RenderContext<LaserMinigun> createContext(LaserMinigun entity, float partialTick) {
        return RenderContext.<LaserMinigun>builder()
                .model(new ModelConfig<LaserMinigun>()
                               .translateOffset(0.0375f, -0.35f, 0)
                               .rotationOffset(180, 0, 0)
                               .alphaDistanceFactor(1.5f))
                .build();
    }

    @Override
    protected void render(LaserMinigun minion, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<LaserMinigun> context, float partialTick, int packedLight, float alpha) {
        LyraModelRenderer.bbmodel(Summoner.rl("bear"))
                .light(packedLight)
                .color(FastColor.ARGB32.color((int) (alpha * 255), 255, 255, 255))
                .animation("run", minion.getRenderTick(partialTick))
                .render(poseStack, bufferSource);
    }
}
