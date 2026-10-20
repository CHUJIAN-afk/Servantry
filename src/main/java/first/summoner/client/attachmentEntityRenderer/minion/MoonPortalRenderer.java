package first.summoner.client.attachmentEntityRenderer.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.light.LyraLights;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.ModelContext;
import first.lyra.client.render.RenderContext;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.client.render.MoonPortalEffect;
import first.summoner.common.minion.sentry.MoonPortal;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.util.Color;

/**
 * 月亮传送门：体积旋涡与青色照明。
 */
public class MoonPortalRenderer extends AbstractAttachmentEntityRenderer<MoonPortal> {

    @Override
    protected RenderContext<MoonPortal> createContext(MoonPortal portal, PathNode visualNode, float partialTick, int packedLight) {
        return super.createContext(portal, visualNode, partialTick, packedLight)
                .model(new ModelContext()
                               .scale(0.75f)
                               .alphaDistanceFactor(0f));
    }

    @Override
    protected void render(PoseStack poseStack, MultiBufferSource bufferSource) {
        MoonPortal portal = context.entity;
        float time = (portal.getLevel().getGameTime() % 240000 + context.partialTick) / 20f;
        float phase = (portal.getUuid().hashCode() & 65535) * (float) (Math.PI / 32768);
        MoonPortalEffect.submit(poseStack, context.color.getAlphaFloat(), time, phase);
        Vec3 pos = context.visualNode.pos();
        Color color = Color.ofOpaque(0x21b0e9);
        LyraLights.next().point()
                .at(pos.x(), pos.y(), pos.z())
                .color(color.getRed(), color.getGreen(), color.getBlue())
                .intensity(0.05f)
                .volumetric(0.75f)
                .range(2.5f)
                .shadows(false);
    }
}
