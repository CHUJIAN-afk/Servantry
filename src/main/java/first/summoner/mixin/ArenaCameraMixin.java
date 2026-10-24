package first.summoner.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import first.summoner.client.AstrageldonArenaClient;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class ArenaCameraMixin {

    @Shadow
    protected abstract void setPosition(Vec3 position);

    @ModifyReturnValue(method = "getMaxZoom", at = @At("RETURN"))
    private float summoner$arenaZoom(float zoom) {
        return AstrageldonArenaClient.cameraZoom((Camera) (Object) this, zoom);
    }

    @Inject(method = "setup", at = @At("TAIL"))
    private void summoner$arenaCamera(BlockGetter level, Entity entity, boolean detached, boolean reverse, float partialTick, CallbackInfo ci) {
        if (detached) {
            setPosition(AstrageldonArenaClient.cameraPosition((Camera) (Object) this));
        }
    }
}
