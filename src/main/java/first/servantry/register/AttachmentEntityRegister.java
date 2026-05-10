package first.servantry.register;

import first.servantry.Servantry;
import first.servantry.api.entity.AttachmentEntityType;
import first.servantry.api.register.ServantryRegistries;
import first.servantry.common.projectile.LaserProjectile;
import first.servantry.common.projectile.StardustProjectile;
import first.servantry.common.servant.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 附件实体类型注册类。
 * <p>
 * 所有附件实体类型（仆从和射弹）都需要在此注册，以便网络同步和渲染调度。
 * </p>
 */
public class AttachmentEntityRegister {

    /**
     * 附件实体类型注册表
     */
    public static final DeferredRegister<AttachmentEntityType<?>> Register = DeferredRegister.create(ServantryRegistries.ATTACHMENT_ENTITY_TYPES, Servantry.MODID);

    // ===================== 仆从类型 =====================

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<Terraprism>> TerraPrism = Register.register("terraprism", () -> new AttachmentEntityType<>(Terraprism::new));
    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<StardustCell>> StardustCell = Register.register("stardust_cell", () -> new AttachmentEntityType<>(StardustCell::new));
    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<EnchantedThrowingKnives>> EnchantedThrowingKnives = Register.register("enchanted_throwing_knives", () -> new AttachmentEntityType<>(EnchantedThrowingKnives::new));
    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<StardustDragon>> StardustDragon = Register.register("stardust_dragon", () -> new AttachmentEntityType<>(StardustDragon::new));
    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<Twins>> Twins = Register.register("twins", () -> new AttachmentEntityType<>(Twins::new));

    // ===================== 射弹类型 =====================

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<StardustProjectile>> StardustProjectile = Register.register("stardust_projectile", () -> new AttachmentEntityType<>(StardustProjectile::new));
    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<LaserProjectile>> LaserProjectile = Register.register("laser_projectile", () -> new AttachmentEntityType<>(LaserProjectile::new));

    /**
     * 注册到事件总线。
     *
     * @param eventBus 事件总线
     */
    public static void register(IEventBus eventBus) {
        Register.register(eventBus);
    }
}
