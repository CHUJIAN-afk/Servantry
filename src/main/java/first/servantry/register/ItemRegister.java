package first.servantry.register;

import first.servantry.Servantry;
import first.servantry.api.Marker;
import first.servantry.api.item.IWhipWeapon;
import first.servantry.common.item.TerraPrismItem;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
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
            .length(6)
            .damageFalloff(0.6f)
            .penetrateBlocks(false)
            .texture(Servantry.rl("textures/whip/cobweb_whip.png"))
            .swingSound(SoundEvents.COBWEB_PLACE, 10)
            .marker(new Marker(Servantry.rl("cobweb"), 0.3f, 40, 0))
            .onHitEntity((player, target) -> target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0)))
            .sweepTip((player, tipPos) -> {
                if (player.level().isClientSide() && player.getRandom().nextFloat() < 0.3f) {
                    player.level().addParticle(new BlockParticleOption(BLOCK, Blocks.COBWEB.defaultBlockState()), tipPos.x, tipPos.y, tipPos.z, 0.0, -0.05, 0.0);
                }
            })
            .buildItem(new Item.Properties().stacksTo(1))
    );

    public static final DeferredItem<Item> SlimeWhip = Register.register("slime_whip", () -> new IWhipWeapon.Builder()
            .damage(1.2f)
            .useTime(30)
            .length(8)
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
            .length(8)
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
            .length(8)
            .damageFalloff(0.4f)
            .penetrateBlocks(false)
            .texture(Servantry.rl("textures/whip/soulscourge.png"))
            .swingSound(SoundEvents.HONEY_BLOCK_PLACE, 10)
            .marker(new Marker(Servantry.rl("soulscourge"), 0.5f, 40, 0))
            .buildItem(new Item.Properties().stacksTo(1))
    );

    public static void register(IEventBus eventBus) {
        Register.register(eventBus);
    }

}
