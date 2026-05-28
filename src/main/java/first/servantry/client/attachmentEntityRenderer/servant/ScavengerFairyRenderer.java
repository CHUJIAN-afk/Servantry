package first.servantry.client.attachmentEntityRenderer.servant;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.Servantry;
import first.servantry.api.client.geo.GeoSideloader;
import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.api.client.render.renderConfig.ModelConfig;
import first.servantry.api.entity.PathNode;
import first.servantry.common.servant.ScavengerFairy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;

public class ScavengerFairyRenderer extends AbstractAttachmentEntityRenderer<ScavengerFairy> {

    @Override
    protected RenderContext<ScavengerFairy> createContext(ScavengerFairy servant) {
        return RenderContext.<ScavengerFairy>builder()
                .model(new ModelConfig<ScavengerFairy>()
                        .scale(0.2f)
                        .rotationOffset(180, 0, 0)
                        .translateOffset(0, -0.5f, 0)
                        .alphaDistanceFactor(1.25f)
                        .visualNodeFunction((fairy, partialTick, rawNode) -> rawNode)
                )
                .build();
    }

    @Override
    protected void renderEntity(ScavengerFairy servant, PoseStack poseStack, MultiBufferSource bufferSource, PathNode visualNode, RenderContext<ScavengerFairy> config) {
        int tickCount = servant.getOwner().tickCount;
        float partialTick = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
        GeoSideloader sideloader = GeoSideloader.getGeoSideloader(Servantry.rl("test_boss"));
        sideloader.setAnimation("连段攻击_1", Mth.lerp(partialTick, tickCount - 1, tickCount));
        sideloader.render(poseStack, bufferSource, partialTick, LightTexture.FULL_BRIGHT);
    }
}
