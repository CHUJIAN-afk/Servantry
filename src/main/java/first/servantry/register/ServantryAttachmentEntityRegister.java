package first.servantry.register;

import first.servantry.Servantry;
import first.servantry.api.entity.AttachmentEntity;
import first.servantry.api.entity.AttachmentEntityType;
import first.servantry.api.register.ServantryRegistries;
import first.servantry.common.projectile.*;
import first.servantry.common.projectile.ChlorophyteCrystal;
import first.servantry.common.projectile.RainbowCrystal;
import first.servantry.common.sentryServant.*;
import first.servantry.common.servant.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * 附件实体类型注册类。
 */
public class ServantryAttachmentEntityRegister {

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

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<EtherealStellarCore>> EtherealStellarCore = register("ethereal_stellar_core", EtherealStellarCore::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<OreScout>> OreScout = register("survey_drone", OreScout::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<ScavengerFairy>> ScavengerFairy = register("scavenger_fairy", ScavengerFairy::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<first.servantry.common.servant.ChlorophyteCrystal>> ChlorophyteCrystal = register("chlorophyte_crystal", first.servantry.common.servant.ChlorophyteCrystal::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<MoonPortal>> MoonPortal = register("moon_portal", MoonPortal::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<first.servantry.common.sentryServant.RainbowCrystal>> RainbowCrystal = register("rainbow_crystal", first.servantry.common.sentryServant.RainbowCrystal::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<Ballista>> Ballista = register("ballista", Ballista::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<SuperPeashooter>> SuperPeashooter = register("super_peashooter", SuperPeashooter::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<VoidEater>> VoidEater = register("void_eater", VoidEater::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<Cloud>> Cloud = register("cloud", Cloud::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<PulseTurret>> PulseTurret = register("pulse_turret", PulseTurret::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<Hornet>> Hornet = register("hornet", Hornet::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<Imp>> Imp = register("imp", Imp::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<UFO>> UFO = register("ufo", UFO::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<Cannon>> Cannon = register("cannon", Cannon::new);

    // ===================== 射弹类型 =====================

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<MiniStardustCell>> StardustProjectile = register("stardust_projectile", MiniStardustCell::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<Laser>> LaserProjectile = register("laser_projectile", Laser::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<DemonFlame>> DemonFlameProjectile = register("demon_flame_projectile", DemonFlame::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<SharkDragon>> SharkDragonProjectile = register("shark_dragon_projectile", SharkDragon::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<ShatteredStellarCore>> EternalNightLaserProjectile = register("eternal_night_laser_projectile", ShatteredStellarCore::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<ChlorophyteCrystal>> ChlorophyteCrystalProjectile = register("chlorophyte_crystal_projectile", first.servantry.common.projectile.ChlorophyteCrystal::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<Zenith>> ZenithProjectile = register("zenith_projectile", Zenith::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<CustomLaser>> CustomLaserProjectile = register("custom_laser_projectile", CustomLaser::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<RainbowCrystal>> RainbowCrystalProjectile = register("rainbow_crystal_projectile", first.servantry.common.projectile.RainbowCrystal::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<CrossbowBolt>> CrossbowBoltProjectile = register("crossbow_bolt_projectile", CrossbowBolt::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<GodFlame>> GodFlameProjectile = register("god_flame_projectile", GodFlame::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<BlitzBall>> BlitzBall = register("blitz_ball", BlitzBall::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<Rain>> Rain = register("rain", Rain::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<DestructionBullet>> DestructionBullet = register("destruction_bullet", DestructionBullet::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<HornetStinger>> HornetStinger = register("hornet_stinger", HornetStinger::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<ImpFireball>> ImpFireball = register("imp_fireball", ImpFireball::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<Corn>> CornProjectile = register("corn_projectile", Corn::new);

    private static <T extends AttachmentEntity> DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<T>> register(String name, Supplier<T> supplier) {
        return Register.register(name, () -> new AttachmentEntityType<>(supplier));
    }

    public static void register(IEventBus eventBus) {
        Register.register(eventBus);
    }
}