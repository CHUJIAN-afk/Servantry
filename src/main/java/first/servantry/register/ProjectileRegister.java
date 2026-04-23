package first.servantry.register;

import first.servantry.Servantry;
import first.servantry.api.register.ProjectileType;
import first.servantry.api.register.ServantryRegistries;
import first.servantry.common.projectile.StardustProjectile;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 射弹类型注册类。
 * <p>
 * 所有射弹类型都需要在此注册，以便网络同步和渲染调度。
 * </p>
 */
public class ProjectileRegister {

    /** 射弹类型注册表 */
    public static final DeferredRegister<ProjectileType<?>> Register = DeferredRegister.create(ServantryRegistries.PROJECTILE_TYPES, Servantry.MODID);

    /** 星细胞射弹类型 */
    public static final DeferredHolder<ProjectileType<?>, ProjectileType<StardustProjectile>> StardustProjectile =
            Register.register("stardust_projectile", () -> new ProjectileType<>(StardustProjectile::new));

    /**
     * 注册射弹类型到事件总线。
     *
     * @param eventBus 事件总线
     */
    public static void register(IEventBus eventBus) {
        Register.register(eventBus);
    }
}