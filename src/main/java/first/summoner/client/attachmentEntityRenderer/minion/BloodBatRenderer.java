package first.summoner.client.attachmentEntityRenderer.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.RenderUtil;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.entity.PathNode;
import first.summoner.common.minion.BloodBat;
import first.summoner.register.SummonerModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FastColor;

/**
 * 血蝙蝠渲染器 - 攻击时显示丝带拖尾，始终处于渲染混合状态（空闲/攻击平滑过渡）。
 */
public class BloodBatRenderer extends AbstractAttachmentEntityRenderer<BloodBat> {

    @Override
    protected RenderContext<BloodBat> createContext(BloodBat minion, float partialTick) {
        return RenderContext.<BloodBat>builder()
                .model(new ModelConfig<BloodBat>()
                               .scale(0.5f)
                               .translateOffset(-0.5f, -0.5f, -0.5f)
                               .rotationOffset(0, 90, 0))
                .build();
    }

    @Override
    protected void render(BloodBat minion, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<BloodBat> context, float partialTick, int packedLight, float alpha) {
        RenderUtil.renderStandalone(SummonerModelRegister.ENCHANTED_THROWING_KNIVES, poseStack, bufferSource, FastColor.ARGB32.color((int) (alpha * 255), 220, 20, 60), packedLight);
    }
}
