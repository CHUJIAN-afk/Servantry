package first.servantry.mixin;

import first.servantry.api.ServantDamageSource;
import first.servantry.api.event.ServantIncomingDamageEvent;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CommonHooks.class)
public class CommonHooksMixin {

    @Inject(
            method = "onEntityIncomingDamage",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/neoforged/bus/api/IEventBus;post(Lnet/neoforged/bus/api/Event;)Lnet/neoforged/bus/api/Event;"
            ),
            cancellable = true
    )
    private static void onEntityIncomingDamage(LivingEntity entity, DamageContainer container, CallbackInfoReturnable<Boolean> cir) {
        if (container.getSource() instanceof ServantDamageSource) {
            cir.setReturnValue(NeoForge.EVENT_BUS.post(new ServantIncomingDamageEvent(entity, container)).isCanceled());
        }
    }

}
