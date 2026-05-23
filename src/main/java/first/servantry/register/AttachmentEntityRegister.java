package first.servantry.register;

import first.servantry.Servantry;
import first.servantry.api.entity.AttachmentEntity;
import first.servantry.api.entity.AttachmentEntityType;
import first.servantry.api.register.ServantryRegistries;
import first.servantry.common.projectile.*;
import first.servantry.common.servant.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * 附件实体类型注册类。
 */
public class AttachmentEntityRegister {

    private static final DeferredRegister<AttachmentEntityType<?>> Register = DeferredRegister.create(ServantryRegistries.ATTACHMENT_ENTITY_TYPES, Servantry.MODID);

    // ===================== 仆从类型 =====================

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<InfiniteShadow>> InfiniteShadow = register("infinite_shadow", InfiniteShadow::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<Terraprism>> TerraPrism = register("terraprism", Terraprism::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<StardustCell>> StardustCell = register("stardust_cell", StardustCell::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<EnchantedThrowingKnives>> EnchantedThrowingKnives = register("enchanted_throwing_knives", EnchantedThrowingKnives::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<StardustDragon>> StardustDragon = register("stardust_dragon", StardustDragon::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<Twins>> Twins = register("twins", Twins::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<Sharknado>> Sharknado = register("sharknado", Sharknado::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<DeadlySphere>> DeadlySphere = register("deadly_sphere", DeadlySphere::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<EyeOfEternalNight>> EyeOfEternalNight = register("eye_of_eternal_night", EyeOfEternalNight::new);

    // ===================== 射弹类型 =====================

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<StardustProjectile>> StardustProjectile = register("stardust_projectile", StardustProjectile::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<LaserProjectile>> LaserProjectile = register("laser_projectile", LaserProjectile::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<DemonFlameProjectile>> DemonFlameProjectile = register("demon_flame_projectile", DemonFlameProjectile::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<SharkDragonProjectile>> SharkDragonProjectile = register("shark_dragon_projectile", SharkDragonProjectile::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<EternalNightLaserProjectile>> EternalNightLaserProjectile = register("eternal_night_laser_projectile", EternalNightLaserProjectile::new);

    private static <T extends AttachmentEntity> DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<T>> register(String name, Supplier<T> supplier) {
        return Register.register(name, () -> new AttachmentEntityType<>(supplier));
    }

    public static void register(IEventBus eventBus) {
        Register.register(eventBus);
    }
}