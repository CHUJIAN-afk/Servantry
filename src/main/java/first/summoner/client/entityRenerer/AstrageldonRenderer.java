package first.summoner.client.entityRenerer;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.EntityPathRender;
import first.summoner.client.render.AstrageldonArenaEffect;
import first.summoner.common.entity.Astrageldon;
import first.summoner.register.SummonerEntityRegister;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

@SuppressWarnings("UnstableApiUsage")
public class AstrageldonRenderer extends GeoEntityRenderer<Astrageldon> {

    public AstrageldonRenderer(EntityRendererProvider.Context context) {
        super(context, SummonerEntityRegister.ASTRAGELDON.get());
    }

    @Override
    public void render(@NotNull Astrageldon entity, float entityYaw, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight) {
        AstrageldonArenaEffect.submit(entity, partialTick);
        poseStack.pushPose();
        EntityPathRender.translate(entity, poseStack, partialTick);
        EntityPathRender.rotate(entity, poseStack, partialTick);
        poseStack.popPose();
    }

    @Override
    public boolean shouldRender(@NotNull Astrageldon livingEntity, @NotNull Frustum camera, double camX, double camY, double camZ) {
        return true;
    }
}
