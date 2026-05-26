package first.servantry.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import first.servantry.api.servant.Servant;
import first.servantry.api.servant.ServantDamageSource;
import first.servantry.register.AttributeRegister;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @ModifyArg(
            method = "hurt",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDD)V"
            ),
            index = 0
    )
    private double knockback(double strength, @Local(argsOnly = true) DamageSource damageSource) {
        if (damageSource instanceof ServantDamageSource servantDamageSource) {
            Servant servant = servantDamageSource.getServant();
            Player owner = servant.getOwner();
            AttributeInstance instance = owner.getAttribute(AttributeRegister.ServantKnockback);
            double scale = instance != null ? instance.getValue() : 1;
            scale *= 0.8 + (0.4 * owner.getRandom().nextDouble());
            return servant.getKnockback() * scale;
        }
        return strength;
    }

}
