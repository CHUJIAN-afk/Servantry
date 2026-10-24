package first.summoner.client.entityRenerer;

import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.EntityPathRender;
import first.lyra.common.entity.IPathEntity;
import first.summoner.Summoner;
import first.summoner.client.render.AstrageldonArenaEffect;
import first.summoner.common.entity.Astrageldon;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

@SuppressWarnings("UnstableApiUsage")
public class AstrageldonRenderer<T extends Entity & GeoAnimatable & IPathEntity> extends GeoEntityRenderer<T> {

    public AstrageldonRenderer(EntityRendererProvider.Context context) {
        super(context, new DefaultedEntityGeoModel<>(Summoner.rl("astrageldon")));
    }

    @Override
    public void render(@NotNull T entity, float entityYaw, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight) {
        if (entity instanceof Astrageldon boss) {
            AstrageldonArenaEffect.submit(boss, partialTick);
        }
        poseStack.pushPose();
        EntityPathRender.translate(entity, poseStack, partialTick);
        EntityPathRender.rotate(entity, poseStack, partialTick);
        poseStack.popPose();
    }

    @Override
    public boolean shouldRender(@NotNull T livingEntity, @NotNull Frustum camera, double camX, double camY, double camZ) {
        return true;
    }
}
