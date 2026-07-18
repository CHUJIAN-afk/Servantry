package first.servantry.register;

import first.servantry.Servantry;
import first.servantry.api.client.render.AttachmentEntityRenderDispatcher;
import first.servantry.api.client.render.IAttachmentEntityRenderer;
import first.servantry.api.entity.AttachmentEntity;
import first.servantry.api.entity.AttachmentEntityType;
import first.servantry.api.register.ServantryRegistries;
import first.servantry.client.attachmentEntityRenderer.projectile.*;
import first.servantry.client.attachmentEntityRenderer.servant.*;
import first.servantry.common.projectile.*;
import first.servantry.common.sentryServant.*;
import first.servantry.common.servant.*;
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
public class ServantryAttachmentEntityRegister {

    private static final DeferredRegister<AttachmentEntityType<?>> Register = DeferredRegister.create(ServantryRegistries.ATTACHMENT_ENTITY_TYPES, Servantry.MODID);

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
            register("deadly_sphere", DeadlySphere::new, DeadlySphereRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<EtherealStellarCore>> ETHEREAL_STELLAR_CORE =
            register("ethereal_stellar_core", EtherealStellarCore::new, EtherealStellarCoreRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<OreScout>> ORE_SCOUT =
            register("survey_drone", OreScout::new, OreScoutServantRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<ScavengerFairy>> SCAVENGER_FAIRY =
            register("scavenger_fairy", ScavengerFairy::new, ScavengerFairyRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<ChlorophyteCrystal>> CHLOROPHYTE_CRYSTAL =
            register("chlorophyte_crystal", ChlorophyteCrystal::new, ChlorophyteCrystalRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<MoonPortal>> MOON_PORTAL =
            register("moon_portal", MoonPortal::new, null);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<RainbowCrystal>> RAINBOW_CRYSTAL =
            register("rainbow_crystal", RainbowCrystal::new, null);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<Ballista>> BALLISTA =
            register("ballista", Ballista::new, BallistaRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<SuperPeashooter>> SUPER_PEASHOOTER =
            register("super_peashooter", SuperPeashooter::new, null);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<VoidEater>> VOID_EATER =
            register("void_eater", VoidEater::new, VoidEaterRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<Cloud>> CLOUD =
            register("cloud", Cloud::new, null);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<PulseTurret>> PULSE_TURRET =
            register("pulse_turret", PulseTurret::new, null);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<Hornet>> HORNET =
            register("hornet", Hornet::new, HornetRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<Imp>> IMP =
            register("imp", Imp::new, ImpRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<UFO>> UFO =
            register("ufo", UFO::new, UFORenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<Cannon>> CANNON =
            register("cannon", Cannon::new, CannonRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<first.servantry.common.servant.NecroSpirit>> NECRO_SPIRIT =
            register("necro_spirit", first.servantry.common.servant.NecroSpirit::new, NecroSpiritRenderer::new);

    // ===================== 射弹类型 =====================

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<MiniStardustCell>> MINI_STARDUST_CELL =
            register("miniStardustCell", MiniStardustCell::new, MiniStardustRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<Laser>> LASER =
            register("laser_projectile", Laser::new, LaserRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<DemonFlame>> DEMON_FLAME =
            register("demon_flame_projectile", DemonFlame::new, DemonFlameRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<SharkDragon>> SHARK_DRAGON =
            register("shark_dragon_projectile", SharkDragon::new, SharkDragonRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<ShatteredStellarCore>> SHATTERED_STELLAR_CORE =
            register("eternal_night_laser_projectile", ShatteredStellarCore::new, ShatteredStellarCoreRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<MiniChlorophyteCrystal>> MINI_CHLOROPHYTE_CRYSTAL =
            register("chlorophyte_crystal_projectile", MiniChlorophyteCrystal::new, MiniChlorophyteCrystalRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<Zenith>> ZENITH =
            register("zenith_projectile", Zenith::new, ZenithRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<CustomLaser>> CUSTOM_LASER =
            register("custom_laser_projectile", CustomLaser::new, CustomLaserRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<MiniRainbowCrystal>> MINI_RAINBOW_CRYSTAL =
            register("rainbow_crystal_projectile", MiniRainbowCrystal::new, RainbowCrystalRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<CrossbowBolt>> CROSSBOW_BOLT =
            register("crossbow_bolt_projectile", CrossbowBolt::new, null);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<GodFlame>> GOD_FLAME =
            register("god_flame_projectile", GodFlame::new, GodFlameRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<BlitzBall>> BLITZ_BALL =
            register("blitz_ball", BlitzBall::new, BlitzBallRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<Rain>> RAIN =
            register("rain", Rain::new, RainRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<DestructionBullet>> DESTRUCTION_BULLET =
            register("destruction_bullet", DestructionBullet::new, DestructionBulletRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<HornetStinger>> HORNET_STINGER =
            register("hornet_stinger", HornetStinger::new, HornetStingerRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<ImpFireball>> IMP_FIREBALL =
            register("imp_fireball", ImpFireball::new, ImpFireballRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<Corn>> CORN =
            register("corn_projectile", Corn::new, CornRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<MiniNecroSpirit>> MINI_NECRO_SPIRIT =
            register("necro_spirit_projectile", MiniNecroSpirit::new, NecroSpiritProjectileRenderer::new);

    private static <T extends AttachmentEntity> DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<T>> register(String name, Supplier<T> supplier, @Nullable Supplier<IAttachmentEntityRenderer<T>> renderer) {
        AttachmentEntityType<T> type = new AttachmentEntityType<>(supplier);
        DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<T>> register = Register.register(name, () -> type);
        Dist dist = FMLLoader.getDist();
        if (renderer != null && dist.isClient()) {
            AttachmentEntityRenderDispatcher.register(type, renderer.get());
        }
        return register;
    }

    public static void register(IEventBus eventBus) {
        Register.register(eventBus);
    }
}