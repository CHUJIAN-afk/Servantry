package first.servantry.mixin;

import first.servantry.api.ServantDamageSource;
import first.servantry.common.servent.EnchantedThrowingKnives;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(CombatRules.class)
public class CombatRulesMixin {

    @ModifyVariable(
            method = "getDamageAfterAbsorb",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 1
    )
    private static float modifyArmorValue(float armorValue, LivingEntity entity, float damage, DamageSource damageSource, float armorToughness) {
        if (damageSource instanceof ServantDamageSource servantDamageSource) {
            if (servantDamageSource.getServant() instanceof EnchantedThrowingKnives) {
                armorValue -= 2.5f;
            }
        }
        return Math.max(armorValue, 0);
    }

}
