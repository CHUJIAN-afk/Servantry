package first.summoner.register;

import first.lyra.common.entity.AttachmentEntity;
import first.lyra.common.entity.AttachmentEntityType;
import first.lyra.register.LyraRegistries;
import first.summoner.Summoner;
import first.summoner.common.minion.*;
import first.summoner.common.projectile.*;
import first.summoner.common.sentry.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * 附件实体类型注册类。
 * <p>
 * 只注册实体类型本身；渲染器注册见 {@link SummonerAttachmentEntityRenderRegister}（仅客户端加载）。
 * 渲染器引用客户端类，绝不能在本类中直接引用，否则服务端加载本类时会被
 * RuntimeDistCleaner 拦截（Attempted to load class ... for invalid dist DEDICATED_SERVER）导致崩溃。
 */
public class SummonerAttachmentEntityRegister {

    private static final DeferredRegister<AttachmentEntityType<?>> Register = DeferredRegister.create(LyraRegistries.ATTACHMENT_ENTITY_TYPES, Summoner.MODID);

    // ===================== 仆从类型 =====================

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<InfiniteShadow>> INFINITE_SHADOW =
            register("infinite_shadow", InfiniteShadow::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<Terraprism>> TERRA_PRISM =
            register("terraprism", Terraprism::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<StardustCell>> STARDUST_CELL =
            register("stardust_cell", StardustCell::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<EnchantedThrowingKnives>> ENCHANTED_THROWING_KNIVES =
            register("enchanted_throwing_knives", EnchantedThrowingKnives::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<StardustDragon>> STARDUST_DRAGON =
            register("stardust_dragon", StardustDragon::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<Twins>> TWINS =
            register("twins", Twins::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<Sharknado>> SHARKNADO =
            register("sharknado", Sharknado::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<DeadlySphere>> DEADLY_SPHERE =
            register("deadly_sphere", DeadlySphere::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<EtherealStellarCore>> ETHEREAL_STELLAR_CORE =
            register("ethereal_stellar_core", EtherealStellarCore::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<OreScout>> ORE_SCOUT =
            register("survey_drone", OreScout::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<ScavengerFairy>> SCAVENGER_FAIRY =
            register("scavenger_fairy", ScavengerFairy::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<ChlorophyteCrystal>> CHLOROPHYTE_CRYSTAL =
            register("chlorophyte_crystal", ChlorophyteCrystal::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<MoonPortal>> MOON_PORTAL =
            register("moon_portal", MoonPortal::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<RainbowCrystal>> RAINBOW_CRYSTAL =
            register("rainbow_crystal", RainbowCrystal::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<Ballista>> BALLISTA =
            register("ballista", Ballista::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<SuperPeashooter>> SUPER_PEASHOOTER =
            register("super_peashooter", SuperPeashooter::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<VoidEater>> VOID_EATER =
            register("void_eater", VoidEater::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<Cloud>> CLOUD =
            register("cloud", Cloud::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<PulseTurret>> PULSE_TURRET =
            register("pulse_turret", PulseTurret::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<Hornet>> HORNET =
            register("hornet", Hornet::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<Imp>> IMP =
            register("imp", Imp::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<UFO>> UFO =
            register("ufo", UFO::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<Cannon>> CANNON =
            register("cannon", Cannon::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<NecroSpirit>> NECRO_SPIRIT =
            register("necro_spirit", NecroSpirit::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<LaserMinigun>> LASER_MINIGUN =
            register("laser_minigun", LaserMinigun::new);

    // ===================== 射弹类型 =====================

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<MiniStardustCell>> MINI_STARDUST_CELL =
            register("mini_stardust_cell", MiniStardustCell::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<Laser>> LASER =
            register("laser", Laser::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<DemonFlame>> DEMON_FLAME =
            register("demon_flame", DemonFlame::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<SharkDragon>> SHARK_DRAGON =
            register("shark_dragon", SharkDragon::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<ShatteredStellarCore>> SHATTERED_STELLAR_CORE =
            register("shattered_stellar_core", ShatteredStellarCore::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<MiniChlorophyteCrystal>> MINI_CHLOROPHYTE_CRYSTAL =
            register("mini_chlorophyte_crystal", MiniChlorophyteCrystal::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<Zenith>> ZENITH =
            register("zenith", Zenith::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<CustomLaser>> CUSTOM_LASER =
            register("custom_laser", CustomLaser::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<MiniRainbowCrystal>> MINI_RAINBOW_CRYSTAL =
            register("mini_rainbow_crystal", MiniRainbowCrystal::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<CrossbowBolt>> CROSSBOW_BOLT =
            register("crossbow_bolt", CrossbowBolt::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<GodFlame>> GOD_FLAME =
            register("god_flame", GodFlame::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<BlitzBall>> BLITZ_BALL =
            register("blitz_ball", BlitzBall::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<Rain>> RAIN =
            register("rain", Rain::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<DestructionBullet>> DESTRUCTION_BULLET =
            register("destruction_bullet", DestructionBullet::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<HornetStinger>> HORNET_STINGER =
            register("hornet_stinger", HornetStinger::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<ImpFireball>> IMP_FIREBALL =
            register("imp_fireball", ImpFireball::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<Corn>> CORN =
            register("corn", Corn::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<MiniNecroSpirit>> MINI_NECRO_SPIRIT =
            register("mini_necro_spirit", MiniNecroSpirit::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<ElectricLaser>> ELECTRIC_LASER =
            register("electric_laser", ElectricLaser::new);

    private static <T extends AttachmentEntity> DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<T>> register(String name, Supplier<T> supplier) {
        AttachmentEntityType<T> type = new AttachmentEntityType<>(supplier);
        return Register.register(name, () -> type);
    }

    public static void register(IEventBus eventBus) {
        Register.register(eventBus);
    }
}
