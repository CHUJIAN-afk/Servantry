package first.servantry.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import first.servantry.api.client.dynamicLight.DynamicLightDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {

    @Inject(
            method = "getPackedLightCoords",
            at = @At("RETURN"),
            cancellable = true
    )
    private void getPackedLightCoords(Entity entity, float partialTicks, CallbackInfoReturnable<Integer> cir, @Local BlockPos blockpos) {
        cir.setReturnValue(DynamicLightDispatcher.getDynamicLight(blockpos, cir.getReturnValue()));
    }
}
