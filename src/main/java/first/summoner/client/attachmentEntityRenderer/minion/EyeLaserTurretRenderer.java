package first.summoner.client.attachmentEntityRenderer.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.AbstractAttachmentEntityGeoRenderer;
import first.lyra.client.render.IAttachmentEntityRenderer;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.trail.ModelConfig;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.Summoner;
import first.summoner.common.minion.sentry.EyeLaserTurret;
import net.minecraft.client.renderer.MultiBufferSource;
import software.bernie.geckolib.util.Color;

public class EyeLaserTurretRenderer implements IAttachmentEntityRenderer<EyeLaserTurret> {

    private final EyeLaserTurretBaseRenderer baseRenderer = new EyeLaserTurretBaseRenderer();
    private final EyeLaserTurretHeadRenderer headRenderer = new EyeLaserTurretHeadRenderer();

    @Override
    public void render(EyeLaserTurret turret, PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, int packedLight, PathNode visualNode) {
        baseRenderer.render(turret, poseStack, bufferSource, partialTick, packedLight, new PathNode(visualNode.pos(), 0.0F, 0.0F, 0.0F));
        headRenderer.render(turret, poseStack, bufferSource, partialTick, packedLight, visualNode);
    }

    /** 基座：固定朝向，向下平移 1.699 格使原点落在基座顶部。 */
    private static final class EyeLaserTurretBaseRenderer extends AbstractAttachmentEntityGeoRenderer<EyeLaserTurret> {

        private EyeLaserTurretBaseRenderer() {
            super(Summoner.rl("entity/summon/eye_laser_turret_base"));
        }

        @Override
        protected GeoRenderContext createContext(EyeLaserTurret turret, PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, int packedLight, PathNode visualNode) {
            RenderContext<EyeLaserTurret> renderContext = RenderContext.<EyeLaserTurret>builder()
                    .model(new ModelConfig<EyeLaserTurret>()
                                   .translateOffset(0.0F, -1.699F, 0.0F))
                    .build();
            return new GeoRenderContext(turret, visualNode, Color.WHITE, partialTick, packedLight, renderContext);
        }
    }

    /** 眼球：绕自身枢轴旋转，偏航偏移 180° 使模型正面与实体正面一致。 */
    private static final class EyeLaserTurretHeadRenderer extends AbstractAttachmentEntityGeoRenderer<EyeLaserTurret> {

        private EyeLaserTurretHeadRenderer() {
            super(Summoner.rl("entity/summon/eye_laser_turret_head"));
        }

        @Override
        protected GeoRenderContext createContext(EyeLaserTurret turret, PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, int packedLight, PathNode visualNode) {
            RenderContext<EyeLaserTurret> context = RenderContext.<EyeLaserTurret>builder()
                    .model(new ModelConfig<EyeLaserTurret>()
                                   .translateOffset(0.0F, -1.81F, 0.0F)
                                   .rotationOffset(180, 0, 0))
                    .build();
            return new GeoRenderContext(turret, visualNode, Color.WHITE, partialTick, packedLight, context);
        }
    }
}
