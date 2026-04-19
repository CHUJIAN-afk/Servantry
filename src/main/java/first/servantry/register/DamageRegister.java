package first.servantry.register;

import first.servantry.Servantry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.Level;

public class DamageRegister {

    public static final ResourceKey<DamageType> Servant = ResourceKey.create(Registries.DAMAGE_TYPE, Servantry.rl("servant"));

    public static DamageSource getDamageSource(ResourceKey<DamageType> resourceKey, Level level) {
        Registry<DamageType> damageTypes = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
        return new DamageSource(damageTypes.getHolderOrThrow(resourceKey), null, null, null);
    }

}
