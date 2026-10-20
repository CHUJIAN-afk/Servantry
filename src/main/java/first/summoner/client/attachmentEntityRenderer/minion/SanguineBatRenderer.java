package first.summoner.client.attachmentEntityRenderer.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.light.LyraLights;
import first.lyra.client.render.ModelContext;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.geo.AbstractAttachmentEntityGeoRenderer;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.Summoner;
import first.summoner.common.minion.minion.SanguineBat;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.util.Color;

/**
 * 血蝙蝠渲染器 - geo 模型 {@code entity/summon/sanguine_bat}，常亮渲染。
 */
public class SanguineBatRenderer extends AbstractAttachmentEntityGeoRenderer<SanguineBat> {

    public SanguineBatRenderer() {
        super(Summoner.rl("entity/summon/sanguine_bat"));
    }

    @Override
    protected RenderContext<SanguineBat> createContext(SanguineBat bat, PathNode visualNode, float partialTick, int packedLight) {
        return super.createContext(bat, visualNode, partialTick, packedLight)
                .packedLight(LightTexture.FULL_BRIGHT)
                .model(new ModelContext().alphaDistanceFactor(1.5f).scale(0.5f).rotationOffset(180, 0, 0).translateOffset(0, -0.5f, 0));
    }

    @Override
    protected void render(PoseStack poseStack, MultiBufferSource bufferSource) {
        Vec3 pos = context.visualNode.pos();
        Color color = Color.ofOpaque(0xd40000);
        LyraLights.next().point()
                .at(pos.x(), pos.y(), pos.z())
                .color(color.getRed(), color.getGreen(), color.getBlue())
                .intensity(0.01f)
                .range(1.25f)
                .volumetric(1f)
                .shadows(false);
    }
}
