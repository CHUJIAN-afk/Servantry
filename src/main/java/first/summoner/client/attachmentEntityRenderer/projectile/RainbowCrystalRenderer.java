package first.summoner.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.light.LyraLights;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.ModelContext;
import first.lyra.client.render.RenderContext;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.client.render.RainbowCrystalEffect;
import first.summoner.common.projectile.MiniRainbowCrystal;
import net.minecraft.client.renderer.MultiBufferSource;

public class RainbowCrystalRenderer extends AbstractAttachmentEntityRenderer<MiniRainbowCrystal> {

    @Override
    protected RenderContext<MiniRainbowCrystal> createContext(MiniRainbowCrystal crystal, PathNode visualNode, float partialTick, int packedLight) {
        return super.createContext(crystal, visualNode, partialTick, packedLight)
                .model(new ModelContext().alphaDistanceFactor(0f));
    }

    @Override
    protected void render(PoseStack poseStack, MultiBufferSource bufferSource) {
        MiniRainbowCrystal crystal = context.entity;
        float age = crystal.getTickCount() + context.partialTick;
        float time = (crystal.getLevel().getGameTime() % 240000 + context.partialTick) / 20f;
        float phase = (crystal.getUuid().hashCode() & 65535) * (float) (Math.PI / 32768);
        RainbowCrystalEffect.projectile(poseStack, crystal.getSourcePosition().subtract(context.visualNode.pos()), age, context.color.getAlphaFloat(), time, phase);
        float pulse = MiniRainbowCrystal.PULSE_TICKS;
        float light = age < pulse * 2 ? (float) Math.sin(age % pulse / pulse * Math.PI)
                : Math.max(0f, (MiniRainbowCrystal.LIFETIME - age) / MiniRainbowCrystal.SPARK_MAX_LIFE);
        var pos = context.visualNode.pos();
        LyraLights.next().point()
                .at(pos.x(), pos.y(), pos.z())
                .color(110, 220, 255)
                .intensity(0.003f * light)
                .range(4f)
                .volumetric(0f)
                .shadows(false);
    }
}
