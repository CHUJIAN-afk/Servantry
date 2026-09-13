package first.summoner.client.entityRenderer;

import com.mojang.blaze3d.vertex.PoseStack;
import first.summoner.common.master.DoomsdayStar;
import net.minecraft.client.model.SlimeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.SlimeOuterLayer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class DoomsdayStarRenderer extends AbstractBossRenderer<DoomsdayStar, SlimeModel<DoomsdayStar>> {

    public DoomsdayStarRenderer(EntityRendererProvider.Context context) {
        super(context, new SlimeModel<>(context.bakeLayer(ModelLayers.SLIME)), 1.0F);
        this.addLayer(new SlimeOuterLayer<>(this, context.getModelSet()));
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull DoomsdayStar entity) {
        return ResourceLocation.withDefaultNamespace("textures/entity/slime/slime.png");
    }

    @Override
    protected void scale(@NotNull DoomsdayStar entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(4.0F, 4.0F, 4.0F);
    }

    @Override
    protected float getModelScaleY(DoomsdayStar entity) {
        return 4.0F;
    }
}
