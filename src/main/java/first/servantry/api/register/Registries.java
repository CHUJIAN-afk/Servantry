package first.servantry.api.register;

import first.servantry.Servantry;
import first.servantry.api.projectile.Projectile;
import first.servantry.api.servant.Servant;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

/**
 * 自定义注册表管理类。
 * <p>
 * 包含仆从类型和射弹类型的自定义注册表。
 * </p>
 */
@EventBusSubscriber(modid = Servantry.MODID)
public class Registries {

    /** 仆从类型注册表键 */
    private static final ResourceKey<Registry<ServantType<? extends Servant>>> SERVANT_TYPE_KEY = ResourceKey.createRegistryKey(Servantry.rl("servant_types"));
    /** 仆从类型注册表 */
    public static final Registry<ServantType<? extends Servant>> SERVANT_TYPES = new RegistryBuilder<>(SERVANT_TYPE_KEY).sync(true).create();

    /** 射弹类型注册表键 */
    private static final ResourceKey<Registry<ProjectileType<? extends Projectile>>> PROJECTILE_TYPE_KEY = ResourceKey.createRegistryKey(Servantry.rl("projectile_types"));
    /** 射弹类型注册表 */
    public static final Registry<ProjectileType<? extends Projectile>> PROJECTILE_TYPES = new RegistryBuilder<>(PROJECTILE_TYPE_KEY).sync(true).create();

    @SubscribeEvent
    public static void createRegistry(NewRegistryEvent event) {
        event.register(SERVANT_TYPES);
        event.register(PROJECTILE_TYPES);
    }

}