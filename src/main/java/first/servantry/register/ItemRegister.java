package first.servantry.register;

import first.servantry.Servantry;
import first.servantry.api.ParticleUtils;
import first.servantry.api.item.IWhipWeapon;
import first.servantry.common.item.TerraPrismItem;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import static net.minecraft.core.particles.ParticleTypes.BLOCK;

public class ItemRegister {

    private static final DeferredRegister.Items Register = DeferredRegister.createItems(Servantry.MODID);

    public static final DeferredItem<TerraPrismItem> TerraPrism = Register.registerItem("terraprism", TerraPrismItem::new);

    public static final DeferredItem<Item> CobwebWhip =
            Register.register("cobweb_whip", () -> new IWhipWeapon.Builder(MarkerRegister.COBWEB_MARK, SoundRegister.UseWhip.get(), SoundEvents.COBWEB_BREAK)
                    .damage(1.8f)
                    .useTime(11)
                    .length(4.5)
                    .damageFalloff(0.6f)
                    .penetrateBlocks(false)
                    .texture(Servantry.rl("textures/whip/cobweb_whip.png"))
                    .onHitEntity((player, target) -> target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0)))
                    .onTipRender((player, tipPos, movementVector) -> {
                        float progress = player.getData(AttachmentRegister.WhipData).getProgress();
                        if (progress > 0.2 && progress < 0.8) {
                            BlockParticleOption particle = new BlockParticleOption(BLOCK, Blocks.COBWEB.defaultBlockState());
                            ParticleUtils.addParticle(player.level(), particle, tipPos, movementVector);
                        }
                    })
                    .buildItem(new Item.Properties().stacksTo(1))
            );

    public static final DeferredItem<Item> SlimeWhip =
            Register.register("slime_whip", () -> new IWhipWeapon.Builder(MarkerRegister.SLIME_MARK, SoundRegister.UseWhip.get(), SoundEvents.SLIME_ATTACK)
                    .damage(2.4f)
                    .useTime(10)
                    .length(5.5)
                    .damageFalloff(0.6f)
                    .penetrateBlocks(false)
                    .texture(Servantry.rl("textures/whip/slime_whip.png"))
                    .onHitEntity((player, target) -> {
                        Vec3 vec3 = player.getData(AttachmentRegister.WhipData).getTipPosition(player, 0.5f);
                        if (target.distanceToSqr(vec3) < 9)
                            target.setRemainingFireTicks(target.getRandom().nextInt(60, 100));
                    })
                    .onTipRender((player, tipPos, movementVector) -> {
                        float progress = player.getData(AttachmentRegister.WhipData).getProgress();
                        if (progress > 0.2 && progress < 0.8) {
                            BlockParticleOption particle = new BlockParticleOption(BLOCK, Blocks.BLUE_CONCRETE_POWDER.defaultBlockState());
                            ParticleUtils.addParticle(player.level(), particle, tipPos, movementVector);
                        }
                    })
                    .buildItem(new Item.Properties().stacksTo(1))
            );

    public static final DeferredItem<Item> LeatherWhip =
            Register.register("leather_whip", () -> new IWhipWeapon.Builder(MarkerRegister.LEATHER_MARK, SoundRegister.UseWhip.get(), SoundRegister.ShakeWhip.get())
                    .damage(2.8f)
                    .useTime(10)
                    .length(5.5)
                    .damageFalloff(0.5f)
                    .penetrateBlocks(false)
                    .texture(Servantry.rl("textures/whip/leather_whip.png"))
                    .buildItem(new Item.Properties().stacksTo(1))
            );

    public static final DeferredItem<Item> Soulscourge =
            Register.register("soulscourge", () -> new IWhipWeapon.Builder(MarkerRegister.SOULSCOURGE_MARK, SoundRegister.UseWhip.get(), SoundEvents.HONEY_BLOCK_BREAK)
                    .damage(3.4f)
                    .useTime(10)
                    .length(7)
                    .damageFalloff(0.4f)
                    .penetrateBlocks(false)
                    .texture(Servantry.rl("textures/whip/soulscourge.png"))
                    .onTipRender((player, tipPos, movementVector) -> {
                        float progress = player.getData(AttachmentRegister.WhipData).getProgress();
                        if (progress > 0.2 && progress < 0.8) {
                            BlockParticleOption particle = new BlockParticleOption(BLOCK, Blocks.SOUL_SAND.defaultBlockState());
                            ParticleUtils.addParticle(player.level(), particle, tipPos, movementVector);
                        }
                    })
                    .buildItem(new Item.Properties().stacksTo(1))
            );

    public static final DeferredItem<Item> Starcrash =
            Register.register("starcrash", () -> new IWhipWeapon.Builder(MarkerRegister.STARCRASH_MARK, SoundRegister.UseWhip.get(), SoundRegister.ShakeWhip.get())
                    .damage(3.6f)
                    .useTime(10)
                    .length(8.5)
                    .damageFalloff(0.3f)
                    .penetrateBlocks(false)
                    .texture(Servantry.rl("textures/whip/starcrash.png"))
                    .onTipRender((player, tipPos, movementVector) -> {
                        float progress = player.getData(AttachmentRegister.WhipData).getProgress();
                        if (progress > 0.2 && progress < 0.8) {
                            ParticleUtils.addParticle(player.level(), ParticleTypes.SMALL_FLAME, tipPos, movementVector);
                        }
                    })
                    .buildItem(new Item.Properties().stacksTo(1))
            );

    public static final DeferredItem<Item> Vasculash =
            Register.register("vasculash", () -> new IWhipWeapon.Builder(MarkerRegister.VASCULASH_MARK, SoundRegister.UseWhip.get(), SoundEvents.HONEY_BLOCK_BREAK)
                    .damage(3.8f)
                    .useTime(10)
                    .length(6.5)
                    .damageFalloff(0.4f)
                    .penetrateBlocks(false)
                    .texture(Servantry.rl("textures/whip/vasculash.png"))
                    .onTipRender((player, tipPos, movementVector) -> {
                        float progress = player.getData(AttachmentRegister.WhipData).getProgress();
                        if (progress > 0.2 && progress < 0.8) {
                            BlockParticleOption particle = new BlockParticleOption(BLOCK, Blocks.RED_CONCRETE_POWDER.defaultBlockState());
                            ParticleUtils.addParticle(player.level(), particle, tipPos, movementVector);
                        }
                    })
                    .buildItem(new Item.Properties().stacksTo(1))
            );

    public static void register(IEventBus eventBus) { Register.register(eventBus); }
}