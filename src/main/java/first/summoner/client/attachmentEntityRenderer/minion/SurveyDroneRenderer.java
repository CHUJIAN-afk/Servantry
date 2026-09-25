package first.summoner.client.attachmentEntityRenderer.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.ModelContext;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.model.LyraModelRenderer;
import first.lyra.client.render.rendererHelper.LaserRendererHelper;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.common.minion.minion.OreScout;
import first.summoner.register.SummonerModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * 探矿无人机渲染器：使用独立 survey_drone 模型。
 */
public class SurveyDroneRenderer extends AbstractAttachmentEntityRenderer<OreScout> {

    @Override
    protected RenderContext<OreScout> createContext(OreScout scout, PathNode visualNode, float partialTick, int packedLight) {
        return super.createContext(scout, visualNode, partialTick, packedLight)
                .model(new ModelContext()
                               .scale(0.7f)
                               .translateOffset(-0.5f, -0.19f, -0.44f)
                               .alphaDistanceFactor(1.5f));
    }

    @Override
    protected void render(PoseStack poseStack, MultiBufferSource bufferSource) {
        LyraModelRenderer.json(SummonerModelRegister.SURVEY_DRONE)
                .color(context.color.argbInt())
                .light(context.packedLight)
                .render(poseStack, bufferSource);
    }

    @Override
    public void render(OreScout scout, PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, int packedLight, PathNode visualNode) {
        super.render(scout, poseStack, bufferSource, partialTick, packedLight, visualNode);
        BlockPos target = scout.getOreTarget();
        if (target != null) {
            Vec3 direction = target.getCenter().subtract(visualNode.pos());
            float length = (float) direction.length();
            if (length <= 0.001f) {
                return;
            }
            Quaternionf rotation = new Quaternionf().rotateTo(
                    new Vector3f(0, 0, -1),
                    new Vector3f((float) direction.x, (float) direction.y, (float) direction.z).normalize());
            poseStack.pushPose();
            poseStack.mulPose(rotation);
            LaserRendererHelper.builder()
                    .length(length)
                    .radius(0.06f, 0.06f)
                    .layers(4)
                    .segments(8)
                    .color(0xFF4444)
                    .alpha(0.9f)
                    .innerRatio(0.2f)
                    .render(poseStack, bufferSource);
            poseStack.popPose();
        }
    }
}
