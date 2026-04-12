package first.servantry.register;

import first.servantry.Servantry;
import first.servantry.api.Marker;
import first.servantry.api.ParticleUtils;
import first.servantry.api.ServantryUtil;
import first.servantry.api.item.IWhipWeapon;
import first.servantry.common.item.TerraPrismItem;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

import static net.minecraft.core.particles.ParticleTypes.BLOCK;

public class ItemRegister {

    private static final DeferredRegister.Items Register = DeferredRegister.createItems(Servantry.MODID);

    public static final DeferredItem<TerraPrismItem> TerraPrism = Register.registerItem("terraprism", TerraPrismItem::new);

    public static final DeferredItem<Item> CobwebWhip = Register.register("cobweb_whip", () -> new IWhipWeapon.Builder()
            .damage(0.9f)
            .useTime(35)
            .length(4.5)
            .damageFalloff(0.6f)
            .penetrateBlocks(false)
            .texture(Servantry.rl("textures/whip/cobweb_whip.png"))
            .swingSound(SoundEvents.COBWEB_PLACE, 10)
            .marker(new Marker(Servantry.rl("cobweb"), 0.3f, 40, 0))
            .onHitEntity((player, target) -> target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0)))
            .tipTick((player, tipPos) -> {
                float progress = player.getData(AttachmentRegister.WhipData).getProgress();
                if (progress > 0.3 && progress < 0.7 && player.level().isClientSide()) {
                    player.level().addParticle(new BlockParticleOption(BLOCK, Blocks.COBWEB.defaultBlockState()), tipPos.x, tipPos.y, tipPos.z, 0, 0, 0);
                }
            })
            .buildItem(new Item.Properties().stacksTo(1))
    );

    public static final DeferredItem<Item> SlimeWhip = Register.register("slime_whip", () -> new IWhipWeapon.Builder()
            .damage(1.2f)
            .useTime(30)
            .length(5.5)
            .damageFalloff(0.6f)
            .penetrateBlocks(false)
            .texture(Servantry.rl("textures/whip/slime_whip.png"))
            .swingSound(SoundEvents.SLIME_ATTACK, 10)
            .marker(new Marker(Servantry.rl("slime_whip"), 0.3f, 40, 0))
            .onHitEntity((player, target) -> {
                Vec3 vec3 = player.getData(AttachmentRegister.WhipData).getTipPosition(player, 0.5f);
                if (target.distanceToSqr(vec3) < 9) {
                    target.setRemainingFireTicks(target.getRandom().nextInt(60, 100));
                }
            })
            .buildItem(new Item.Properties().stacksTo(1))
    );

    public static final DeferredItem<Item> LeatherWhip = Register.register("leather_whip", () -> new IWhipWeapon.Builder()
            .damage(1.4f)
            .useTime(30)
            .length(5.5)
            .damageFalloff(0.5f)
            .penetrateBlocks(false)
            .texture(Servantry.rl("textures/whip/leather_whip.png"))
            .swingSound(null, 10)
            .marker(new Marker(Servantry.rl("leather_whip"), 0.4f, 40, 0))
            .buildItem(new Item.Properties().stacksTo(1))
    );

    public static final DeferredItem<Item> Soulscourge = Register.register("soulscourge", () -> new IWhipWeapon.Builder()
            .damage(1.7f)
            .useTime(30)
            .length(7)
            .damageFalloff(0.4f)
            .penetrateBlocks(false)
            .texture(Servantry.rl("textures/whip/soulscourge.png"))
            .swingSound(SoundEvents.HONEY_BLOCK_PLACE, 10)
            .marker(new Marker(Servantry.rl("soulscourge"), 0.5f, 40, 0))
            .buildItem(new Item.Properties().stacksTo(1))
    );

    public static final DeferredItem<Item> Starcrash = Register.register("starcrash", () -> new IWhipWeapon.Builder()
            .damage(1.8f)
            .useTime(30)
            .length(8.5)
            .damageFalloff(0.3f)
            .penetrateBlocks(false)
            .texture(Servantry.rl("textures/whip/starcrash.png"))
            .swingSound(SoundEvents.HONEY_BLOCK_BREAK, 12)
            .marker(new Marker(Servantry.rl("starcrash"), 0.2f, 40, 0))
            .onHitMarker((target, servant, marker, amount) -> {
                if (!marker.isHited()) {
                    marker.setHited(true);
                    Level level = target.level();
                    List<LivingEntity> list = level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(1), living -> servant.isTarget(living));
                    for (LivingEntity living : list) {
                        int invulnerableTime = living.invulnerableTime;
                        living.invulnerableTime = 0;
                        living.hurt(servant.getDamageSource(), amount * 1.33f);
                        living.invulnerableTime = invulnerableTime;
                    }
                    ParticleUtils.spawnParticleSphere((ServerLevel) level, target.getX(), target.getBoundingBox().getCenter().y(), target.getZ(), ParticleTypes.EXPLOSION, 1f, 10, 0.2f);
                    ServantryUtil.playSound(level, target.position(), SoundEvents.GENERIC_EXPLODE.value(), target.getSoundSource());
                }
            })
            .tipTick((player, tipPos) -> {
                if (player.level().isClientSide()) {
                    player.level().addParticle(ParticleTypes.SMALL_FLAME, tipPos.x, tipPos.y, tipPos.z, 0, 0, 0);
                }
            })
            .buildItem(new Item.Properties().stacksTo(1))
    );

    public static final DeferredItem<Item> Vasculash = Register.register("vasculash", () -> new IWhipWeapon.Builder()
            .damage(1.9f)
            .useTime(30)
            .length(6.5)
            .damageFalloff(0.4f)
            .penetrateBlocks(false)
            .texture(Servantry.rl("textures/whip/vasculash.png"))
            .swingSound(SoundEvents.HONEY_BLOCK_BREAK, 12)
            .marker(new Marker(Servantry.rl("vasculash"), 0.5f, 40, 0))
            .tipTick((player, tipPos) -> {
                if (player.level().isClientSide()) {
                    player.level().addParticle(new BlockParticleOption(BLOCK, Blocks.RED_CONCRETE_POWDER.defaultBlockState()), tipPos.x, tipPos.y, tipPos.z, 0, 0, 0);
                }
            })
            .buildItem(new Item.Properties().stacksTo(1))
    );

    public static void register(IEventBus eventBus) {
        Register.register(eventBus);
    }

}
