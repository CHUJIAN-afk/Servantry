package first.summoner.client.attachmentEntityRenderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.light.LyraLights;
import first.lyra.client.render.AbstractAttachmentEntityRenderer;
import first.lyra.client.render.ModelContext;
import first.lyra.client.render.RenderContext;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.client.render.FlameEffect;
import first.summoner.common.projectile.DemonFlame;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.util.Color;

public class DemonFlameRenderer extends AbstractAttachmentEntityRenderer<DemonFlame> {

    @Override
    protected RenderContext<DemonFlame> createContext(DemonFlame flame, PathNode visualNode, float partialTick, int packedLight) {
        return super.createContext(flame, visualNode, partialTick, packedLight)
                .model(new ModelContext().alphaDistanceFactor(0f));
    }

    @Override
    protected void render(PoseStack poseStack, MultiBufferSource bufferSource) {
        DemonFlame flame = context.entity;
        float age = flame.getTickCount() + context.partialTick;
        float time = (flame.getLevel().getGameTime() % 240000 + context.partialTick) / 20f;
        float phase = (flame.getUuid().hashCode() & 65535) * (float) (Math.PI / 32768);
        FlameEffect.Profile profile = new FlameEffect.Profile((float) (flame.getHitbox().getSize() / 4f), 10, 0xc8f8ad, 0x56df08, 2f);
        FlameEffect.submit(poseStack, profile, age, context.color.getAlphaFloat(), time, phase);
        Vec3 pos = context.visualNode.pos();
        Color color = profile.color(age);
        LyraLights.next().point()
                .at(pos.x(), pos.y(), pos.z())
                .color(color.getRed(), color.getGreen(), color.getBlue())
                .intensity(0.001f * profile.opacity(age))
                .range(1.25f)
                .volumetric(0.1f)
                .shadows(false);
    }
}
