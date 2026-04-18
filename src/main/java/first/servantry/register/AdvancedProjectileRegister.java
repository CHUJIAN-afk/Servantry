package first.servantry.register;

import first.servantry.Servantry;
import first.servantry.api.register.ProjectileType;
import first.servantry.api.register.Registries;
import first.servantry.common.projectile.StardustLaser;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class AdvancedProjectileRegister {

    public static final DeferredRegister<ProjectileType<?>> Register = DeferredRegister.create(Registries.PROJECTILE_TYPES, Servantry.MODID);

    public static final DeferredHolder<ProjectileType<?>, ProjectileType<StardustLaser>> StardustLaser = Register.register("stardust_laser", () -> new ProjectileType<>(StardustLaser::new));

    public static void register(IEventBus eventBus) {
        Register.register(eventBus);
    }

}
