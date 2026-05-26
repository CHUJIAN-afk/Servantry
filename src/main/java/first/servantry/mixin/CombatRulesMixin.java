package first.servantry.mixin;

import first.servantry.api.servant.Servant;
import first.servantry.api.servant.ServantDamageSource;
import first.servantry.common.servant.EnchantedThrowingKnives;
import first.servantry.register.AttributeRegister;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
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
            Servant servant = servantDamageSource.getServant();
            if (servant != null) {
                if (servant instanceof EnchantedThrowingKnives) {
                    armorValue -= 2.5f;
                }
                Player owner = servant.getOwner();
                AttributeInstance instance = owner.getAttribute(AttributeRegister.ServantArmorPierce);
                if (instance != null) {
                    armorValue -= (float) instance.getValue();
                }
            }
        }
        return Math.max(armorValue, 0);
    }
}
