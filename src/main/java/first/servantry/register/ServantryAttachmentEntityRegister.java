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

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<InfiniteShadow>> InfiniteShadow =
            register("infinite_shadow", InfiniteShadow::new, InfiniteShadowRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<Terraprism>> TerraPrism =
            register("terraprism", Terraprism::new, TerraprismRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<StardustCell>> StardustCell =
            register("stardust_cell", StardustCell::new, StardustCellRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<EnchantedThrowingKnives>> EnchantedThrowingKnives =
            register("enchanted_throwing_knives", EnchantedThrowingKnives::new, EnchantedThrowingKnivesRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<StardustDragon>> StardustDragon =
            register("stardust_dragon", StardustDragon::new, StardustDragonRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<Twins>> Twins =
            register("twins", Twins::new, TwinsRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<Sharknado>> Sharknado =
            register("sharknado", Sharknado::new, SharknadoRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<DeadlySphere>> DeadlySphere =
            register("deadly_sphere", DeadlySphere::new, DeadlySphereRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<EtherealStellarCore>> EtherealStellarCore =
            register("ethereal_stellar_core", EtherealStellarCore::new, EtherealStellarCoreRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<OreScout>> OreScout =
            register("survey_drone", OreScout::new, OreScoutServantRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<ScavengerFairy>> ScavengerFairy =
            register("scavenger_fairy", ScavengerFairy::new, ScavengerFairyRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<ChlorophyteCrystal>> ChlorophyteCrystal =
            register("chlorophyte_crystal", ChlorophyteCrystal::new, ChlorophyteCrystalRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<MoonPortal>> MoonPortal =
            register("moon_portal", MoonPortal::new, null);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<RainbowCrystal>> RainbowCrystal =
            register("rainbow_crystal", RainbowCrystal::new, null);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<Ballista>> Ballista =
            register("ballista", Ballista::new, BallistaRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<SuperPeashooter>> SuperPeashooter =
            register("super_peashooter", SuperPeashooter::new, null);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<VoidEater>> VoidEater =
            register("void_eater", VoidEater::new, VoidEaterRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<Cloud>> Cloud =
            register("cloud", Cloud::new, null);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<PulseTurret>> PulseTurret =
            register("pulse_turret", PulseTurret::new, null);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<Hornet>> Hornet =
            register("hornet", Hornet::new, HornetRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<Imp>> Imp =
            register("imp", Imp::new, ImpRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<UFO>> UFO =
            register("ufo", UFO::new, UFORenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<Cannon>> Cannon =
            register("cannon", Cannon::new, CannonRenderer::new);

    // ===================== 射弹类型 =====================

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<MiniStardustCell>> StardustProjectile =
            register("stardust_projectile", MiniStardustCell::new, MiniStardustRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<Laser>> LaserProjectile =
            register("laser_projectile", Laser::new, LaserRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<DemonFlame>> DemonFlameProjectile =
            register("demon_flame_projectile", DemonFlame::new, DemonFlameRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<SharkDragon>> SharkDragonProjectile =
            register("shark_dragon_projectile", SharkDragon::new, SharkDragonRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<ShatteredStellarCore>> EternalNightLaserProjectile =
            register("eternal_night_laser_projectile", ShatteredStellarCore::new, ShatteredStellarCoreRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<MiniChlorophyteCrystal>> MiniChlorophyteCrystalProjectile =
            register("chlorophyte_crystal_projectile", MiniChlorophyteCrystal::new, MiniChlorophyteCrystalRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<Zenith>> ZenithProjectile =
            register("zenith_projectile", Zenith::new, ZenithRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<CustomLaser>> CustomLaserProjectile =
            register("custom_laser_projectile", CustomLaser::new, CustomLaserRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<MiniRainbowCrystal>> MiniRainbowCrystalProjectile =
            register("rainbow_crystal_projectile", MiniRainbowCrystal::new, RainbowCrystalRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<CrossbowBolt>> CrossbowBoltProjectile =
            register("crossbow_bolt_projectile", CrossbowBolt::new, null);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<GodFlame>> GodFlameProjectile =
            register("god_flame_projectile", GodFlame::new, GodFlameRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<BlitzBall>> BlitzBall =
            register("blitz_ball", BlitzBall::new, BlitzBallRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<Rain>> Rain =
            register("rain", Rain::new, RainRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<DestructionBullet>> DestructionBullet =
            register("destruction_bullet", DestructionBullet::new, DestructionBulletRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<HornetStinger>> HornetStinger =
            register("hornet_stinger", HornetStinger::new, HornetStingerRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<ImpFireball>> ImpFireball =
            register("imp_fireball", ImpFireball::new, ImpFireballRenderer::new);

    public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<Corn>> CornProjectile =
            register("corn_projectile", Corn::new, CornRenderer::new);

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