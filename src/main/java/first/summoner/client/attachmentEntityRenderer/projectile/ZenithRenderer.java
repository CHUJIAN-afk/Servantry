package first.summoner.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.api.LyraAPI;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.ModelContext;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.RenderUtil;
import first.lyra.client.render.model.LyraModelRenderer;
import first.lyra.common.attachmentEntity.PathNode;
import first.lyra.utils.EasingCurve;
import first.summoner.Summoner;
import first.summoner.common.projectile.Zenith;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.phys.Vec3;

public class ZenithRenderer extends AbstractAttachmentEntityRenderer<Zenith> {

    @Override
    protected RenderContext<Zenith> createContext(Zenith zenith, PathNode visualNode, float partialTick, int packedLight) {
        if (zenith.getTickCount() >= 1) {
            context = super.createContext(zenith, visualNode, partialTick, packedLight);
            if (zenith.alpha > 0.2) {
                context = context.trail(new ZenithTrailContext().upOffset(1.32575f).tipAlphaBoost((entity, progress) -> (1.0F - progress) * 20.0F * zenith.alpha).timer(1).colorRGB(zenith.renderType.getColor()));
            }
            return context.model(new ModelContext().scale(2).translateOffset(-0.5f, -0.5f, -0.5f).rotationOffset(0, 90, 45));
        } else {
            return null;
        }
    }

    @Override
    protected void render(PoseStack poseStack, MultiBufferSource bufferSource) {
        Zenith zenith = context.entity;
        PathNode visualNode = context.visualNode;
        LyraModelRenderer.json(ModelResourceLocation.standalone(zenith.renderType.getTexture()))
                .color(context.color.argbInt())
                .light(RenderUtil.FULL_LIGHT)
                .render(poseStack, bufferSource);
        float light = zenith.alpha * 0.5f;
        if (zenith.getTickCount() < 2) {
            light *= context.partialTick;
        }
        LyraAPI.light(visualNode.pos(), light);
        if (context.trail != null && zenith.alpha > 0.75) {
            float alpha = 0;
            float progress = zenith.getProgress(context.partialTick);
            if (progress >= 0.3 && progress <= 0.5) {
                alpha = (progress - 0.3f) / 0.2f;
            }
            if (progress >= 0.5 && progress <= 0.7) {
                alpha = (0.7f - progress) / 0.2f;
            }
            alpha = EasingCurve.EASE_IN_OUT_QUAD.apply(alpha);
            if (alpha > 0) {
                Vec3 pos = visualNode.pos().add(Vec3.directionFromRotation(visualNode.pitch(), visualNode.yaw()).normalize().scale(1.15));
                int color = FastColor.ARGB32.color((int) (alpha * 255), context.trail.colorRGB);
                RenderUtil.renderImage(Summoner.rl("textures/zenith.png"), pos, 6 * alpha, 1.5f * alpha, bufferSource, false, color);
            }
        }
    }

    @Override
    protected float getAlphaModify() {
        float alpha = 1;
        if (Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
            alpha = super.getAlphaModify();
        } else {
            float tick = context.entity.getTickCount() + context.partialTick;
            if (tick < 4) {
                alpha = Math.clamp(tick / 4f, 0.21f, 1f);
            }
            if (tick > 8) {
                alpha = Math.clamp((12 - tick) / 4f, 0.102F, 1f);
            }
        }
        if (context.entity.alpha < alpha) {
            alpha = context.entity.alpha;
        }
        if (context.entity.getTickCount() < 2) {
            if (context.partialTick < alpha) {
                alpha = context.partialTick;
            }
        }
        return alpha;
    }
}
