package first.servantry.client.attachmentEntityRenderer.servant;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.AlphaBufferSource;
import first.servantry.api.client.render.ModelRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.api.client.render.renderConfig.ModelConfig;
import first.servantry.api.entity.PathNode;
import first.servantry.common.servant.Sharknado;
import first.servantry.register.ModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;

public class SharknadoRenderer extends AbstractAttachmentEntityRenderer<Sharknado> {

    @Override
    protected RenderContext<Sharknado> createContext(Sharknado entity) {
        return RenderContext.<Sharknado>builder()
                .model(new ModelConfig<Sharknado>()
                        .translateOffset(-0.5f, -0.5f, -0.5f)
                        .alphaDistanceFactor(1.5f)
                )
                .build();
    }

    @Override
    protected void renderEntityModel(Sharknado entity, PoseStack poseStack, MultiBufferSource bufferSource, PathNode node, RenderContext<Sharknado> config) {
        ModelConfig<Sharknado> model = config.model;
        poseStack.pushPose();

        poseStack.mulPose(Axis.YN.rotationDegrees(node.yaw()));
        poseStack.mulPose(Axis.XP.rotationDegrees(node.pitch()));
        poseStack.mulPose(Axis.ZP.rotationDegrees(node.roll()));

        poseStack.mulPose(Axis.YN.rotationDegrees(model.yawOffset));
        poseStack.mulPose(Axis.XP.rotationDegrees(model.pitchOffset));
        poseStack.mulPose(Axis.ZP.rotationDegrees(model.rollOffset));

        poseStack.scale(model.scale, model.scale, model.scale);
        poseStack.translate(model.translateX, model.translateY, model.translateZ);

        renderEntity(entity, poseStack, bufferSource, node, config);

        poseStack.popPose();

        if (bufferSource instanceof AlphaBufferSource alphaBufferSource) {
            float alphaBufferSourceAlpha = alphaBufferSource.getAlpha();
            alphaBufferSource.setAlpha(alphaBufferSourceAlpha * 0.35f);
            poseStack.pushPose();

            poseStack.mulPose(Axis.YN.rotationDegrees(node.yaw() * 2f));
            poseStack.mulPose(Axis.XP.rotationDegrees(node.pitch() * 2f));
            poseStack.mulPose(Axis.ZP.rotationDegrees(node.roll() * 2f));

            poseStack.mulPose(Axis.YN.rotationDegrees(model.yawOffset));
            poseStack.mulPose(Axis.XP.rotationDegrees(model.pitchOffset));
            poseStack.mulPose(Axis.ZP.rotationDegrees(model.rollOffset));

            poseStack.scale(model.scale * 1.2f, model.scale * 1.2f, model.scale * 1.2f);
            poseStack.translate(model.translateX, model.translateY, model.translateZ);


            renderEntity(entity, poseStack, bufferSource, node, config);
            poseStack.popPose();
            alphaBufferSource.setAlpha(alphaBufferSourceAlpha);
        }
    }

    @Override
    protected void renderEntity(Sharknado entity, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<Sharknado> config) {
        ModelRenderer.renderModel(ModelRegister.SHARKNADO, poseStack, bufferSource);
    }
}
