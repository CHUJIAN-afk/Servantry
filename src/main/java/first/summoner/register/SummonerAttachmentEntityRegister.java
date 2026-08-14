package first.summoner.register;

import first.lyra.client.render.AttachmentEntityRenderDispatcher;
import first.lyra.client.render.IAttachmentEntityRenderer;
import first.lyra.common.entity.AttachmentEntity;
import first.lyra.common.entity.AttachmentEntityType;
import first.lyra.register.LyraRegistries;
import first.summoner.Summoner;
import first.summoner.client.attachmentEntityRenderer.minion.*;
import first.summoner.client.attachmentEntityRenderer.projectile.*;
import first.summoner.common.minion.*;
import first.summoner.common.projectile.*;
import first.summoner.common.sentry.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * 附件实体类型注册类。
 */
public class SummonerAttachmentEntityRegister {

    private static final DeferredRegister<AttachmentEntityType<?>> Register = DeferredRegister.create(LyraRegistries.ATTACHMENT_ENTITY_TYPES, Summoner.MODID);

    // ===================== 仆从类型 =====================

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<InfiniteShadow>> INFINITE_SHADOW =
            register("infinite_shadow", InfiniteShadow::new, InfiniteShadowRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<Terraprism>> TERRA_PRISM =
            register("terraprism", Terraprism::new, TerraprismRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<StardustCell>> STARDUST_CELL =
            register("stardust_cell", StardustCell::new, StardustCellRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<EnchantedThrowingKnives>> ENCHANTED_THROWING_KNIVES =
            register("enchanted_throwing_knives", EnchantedThrowingKnives::new, EnchantedThrowingKnivesRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<StardustDragon>> STARDUST_DRAGON =
            register("stardust_dragon", StardustDragon::new, StardustDragonRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<Twins>> TWINS =
            register("twins", Twins::new, TwinsRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<Sharknado>> SHARKNADO =
            register("sharknado", Sharknado::new, SharknadoRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<DeadlySphere>> DEADLY_SPHERE =
            register("deadly_sphere", DeadlySphere::new, null);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<EtherealStellarCore>> ETHEREAL_STELLAR_CORE =
            register("ethereal_stellar_core", EtherealStellarCore::new, EtherealStellarCoreRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<OreScout>> ORE_SCOUT =
            register("survey_drone", OreScout::new, null);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<ScavengerFairy>> SCAVENGER_FAIRY =
            register("scavenger_fairy", ScavengerFairy::new, null);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<ChlorophyteCrystal>> CHLOROPHYTE_CRYSTAL =
            register("chlorophyte_crystal", ChlorophyteCrystal::new, ChlorophyteCrystalRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<MoonPortal>> MOON_PORTAL =
            register("moon_portal", MoonPortal::new, null);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<RainbowCrystal>> RAINBOW_CRYSTAL =
            register("rainbow_crystal", RainbowCrystal::new, null);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<Ballista>> BALLISTA =
            register("ballista", Ballista::new, null);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<SuperPeashooter>> SUPER_PEASHOOTER =
            register("super_peashooter", SuperPeashooter::new, null);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<VoidEater>> VOID_EATER =
            register("void_eater", VoidEater::new, null);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<Cloud>> CLOUD =
            register("cloud", Cloud::new, null);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<PulseTurret>> PULSE_TURRET =
            register("pulse_turret", PulseTurret::new, null);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<Hornet>> HORNET =
            register("hornet", Hornet::new, null);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<Imp>> IMP =
            register("imp", Imp::new, null);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<UFO>> UFO =
            register("ufo", UFO::new, null);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<Cannon>> CANNON =
            register("cannon", Cannon::new, null);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<NecroSpirit>> NECRO_SPIRIT =
            register("necro_spirit", NecroSpirit::new, null);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<LaserMinigun>> LASER_MINIGUN =
            register("laser_minigun", LaserMinigun::new, LaserMinigunRenderer::new);

    // ===================== 射弹类型 =====================

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<MiniStardustCell>> MINI_STARDUST_CELL =
            register("mini_stardust_cell", MiniStardustCell::new, MiniStardustRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<Laser>> LASER =
            register("laser", Laser::new, LaserRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<DemonFlame>> DEMON_FLAME =
            register("demon_flame", DemonFlame::new, DemonFlameRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<SharkDragon>> SHARK_DRAGON =
            register("shark_dragon", SharkDragon::new, SharkDragonRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<ShatteredStellarCore>> SHATTERED_STELLAR_CORE =
            register("shattered_stellar_core", ShatteredStellarCore::new, ShatteredStellarCoreRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<MiniChlorophyteCrystal>> MINI_CHLOROPHYTE_CRYSTAL =
            register("mini_chlorophyte_crystal", MiniChlorophyteCrystal::new, MiniChlorophyteCrystalRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<Zenith>> ZENITH =
            register("zenith", Zenith::new, ZenithRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<CustomLaser>> CUSTOM_LASER =
            register("custom_laser", CustomLaser::new, CustomLaserRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<MiniRainbowCrystal>> MINI_RAINBOW_CRYSTAL =
            register("mini_rainbow_crystal", MiniRainbowCrystal::new, RainbowCrystalRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<CrossbowBolt>> CROSSBOW_BOLT =
            register("crossbow_bolt", CrossbowBolt::new, null);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<GodFlame>> GOD_FLAME =
            register("god_flame", GodFlame::new, GodFlameRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<BlitzBall>> BLITZ_BALL =
            register("blitz_ball", BlitzBall::new, BlitzBallRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<Rain>> RAIN =
            register("rain", Rain::new, RainRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<DestructionBullet>> DESTRUCTION_BULLET =
            register("destruction_bullet", DestructionBullet::new, DestructionBulletRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<HornetStinger>> HORNET_STINGER =
            register("hornet_stinger", HornetStinger::new, null);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<ImpFireball>> IMP_FIREBALL =
            register("imp_fireball", ImpFireball::new, null);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<Corn>> CORN =
            register("corn", Corn::new, null);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<MiniNecroSpirit>> MINI_NECRO_SPIRIT =
            register("mini_necro_spirit", MiniNecroSpirit::new, null);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<ElectricLaser>> ELECTRIC_LASER =
            register("electric_laser", ElectricLaser::new, ElectricLaserRenderer::new);

    private static <T extends AttachmentEntity> DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<T>> register(String name, Supplier<T> supplier, @Nullable Supplier<IAttachmentEntityRenderer<T>> renderer) {
        AttachmentEntityType<T> type = new AttachmentEntityType<>(supplier);
        DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<T>> register = Register.register(name, () -> type);
        Dist dist = FMLLoader.getDist();
        if (dist.isClient()) {
            if (renderer == null) {
                renderer = TextOnlyRenderer::new;
            }
            AttachmentEntityRenderDispatcher.register(type, renderer.get());
        }
        return register;
    }

    public static void register(IEventBus eventBus) {
        Register.register(eventBus);
    }
}