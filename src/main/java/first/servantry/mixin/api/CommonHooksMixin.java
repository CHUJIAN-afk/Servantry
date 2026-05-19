package first.servantry.mixin.api;

import first.servantry.api.event.ServantIncomingDamageEvent;
import first.servantry.api.servant.Servant;
import first.servantry.api.servant.ServantDamageSource;
import first.servantry.register.AttributeRegister;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
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
        if (container.getSource() instanceof ServantDamageSource source) {
            ServantIncomingDamageEvent event = new ServantIncomingDamageEvent(entity, container);
            Servant servant = source.getServant();
            Player owner = servant.getOwner();
            AttributeInstance instance = owner.getAttribute(AttributeRegister.ServantDamage);
            float scale = instance != null ? (float) instance.getValue() : 1;
            event.setAmount(event.getAmount() * scale);
            cir.setReturnValue(NeoForge.EVENT_BUS.post(event).isCanceled());
        }
    }

}
