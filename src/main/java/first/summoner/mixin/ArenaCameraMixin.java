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
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Camera.class)
public abstract class ArenaCameraMixin {

    @Shadow
    protected abstract void setPosition(Vec3 position);

    @ModifyArgs(method = "setup", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setPosition(DDD)V", ordinal = 0))
    private void summoner$arenaCarry(Args args) {
        Vec3 offset = AstrageldonArenaClient.cameraOffset((Camera) (Object) this);
        args.set(0, (double) args.get(0) + offset.x);
        args.set(1, (double) args.get(1) + offset.y);
        args.set(2, (double) args.get(2) + offset.z);
    }

    @ModifyReturnValue(method = "getMaxZoom", at = @At("RETURN"))
    private float summoner$arenaZoom(float zoom) {
        return AstrageldonArenaClient.cameraZoom((Camera) (Object) this, zoom);
    }

    @Inject(method = "setup", at = @At("TAIL"))
    private void summoner$arenaCamera(BlockGetter level, Entity entity, boolean detached, boolean reverse, float partialTick, CallbackInfo ci) {
        setPosition(AstrageldonArenaClient.cameraPosition((Camera) (Object) this));
    }
}
