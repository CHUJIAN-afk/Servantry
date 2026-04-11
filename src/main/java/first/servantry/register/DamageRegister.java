package first.servantry.register;

import first.servantry.Servantry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;

public class DamageRegister {

    public static final ResourceKey<DamageType> Servant = ResourceKey.create(Registries.DAMAGE_TYPE, Servantry.rl("servant"));

}
