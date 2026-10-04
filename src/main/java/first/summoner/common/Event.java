package first.summoner.common;


import first.lyra.api.LyraHelper;
import first.lyra.common.attachment.AttachmentEntityData;
import first.lyra.common.attachmentEntity.AttachmentEntityDamageSource;
import first.lyra.register.LyraAttachmentRegister;
import first.summoner.Summoner;
import first.summoner.common.attachment.ZenithData;
import first.summoner.common.dataComponent.ScabbardContainer;
import first.summoner.common.minion.minion.ChlorophyteCrystal;
import first.summoner.common.minion.minion.StardustCell;
import first.summoner.common.minion.minion.VoidEater;
import first.summoner.network.ZenithPacket;
import first.summoner.register.*;
import first.summoner.utils.CuriosUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ItemStackedOnOtherEvent;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import top.theillusivec4.curios.api.event.CurioChangeEvent;

import java.util.List;

@EventBusSubscriber(modid = Summoner.MODID)
public class Event {

    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        ZenithData data = player.getData(SummonerAttachmentRegister.ZenithData);
        if (!player.level().isClientSide()) {
            data.tick();
        } else {
            if (Minecraft.getInstance().options.keyUse.isDown() && data.swing()) {
                PacketDistributor.sendToServer(new ZenithPacket());
            }
        }
    }

    @SubscribeEvent
    public static void onItemStackedOnOther(ItemStackedOnOtherEvent event) {
        if (event.getClickAction() == ClickAction.SECONDARY) {
            ItemStack carried = event.getCarriedItem();
            ItemStack stackedOn = event.getStackedOnItem();
            if (carried.is(SummonerWeaponRegister.InfiniteScabbard.get())) {
                Slot slot = event.getSlot();
                Player player = event.getPlayer();
                ScabbardContainer scabbard = carried.getOrDefault(SummonerDataComponentRegister.SCABBARD.get(), ScabbardContainer.EMPTY);
                if (!stackedOn.isEmpty() && scabbard.isEmpty()) {
                    carried.set(SummonerDataComponentRegister.SCABBARD.get(), new ScabbardContainer(stackedOn.copy()));
                    slot.set(ItemStack.EMPTY);
                    player.playSound(SoundEvents.ARMOR_EQUIP_LEATHER.value(), 1.0F, 1.2F);
                    event.setCanceled(true);
                } else if (stackedOn.isEmpty() && !scabbard.isEmpty()) {
                    slot.set(scabbard.itemStack().copy());
                    carried.set(SummonerDataComponentRegister.SCABBARD.get(), ScabbardContainer.EMPTY);
                    player.playSound(SoundEvents.ARMOR_EQUIP_LEATHER.value(), 1.0F, 0.9F);
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRegisterBrewingRecipes(RegisterBrewingRecipesEvent event) {
        PotionBrewing.Builder builder = event.getBuilder();
        builder.addMix(Potions.AWKWARD, Items.WITHER_ROSE, SummonerPotionRegister.Obsession);
        builder.addMix(SummonerPotionRegister.Obsession, Items.REDSTONE, SummonerPotionRegister.LongObsession);
        builder.addMix(SummonerPotionRegister.Obsession, Items.GLOWSTONE_DUST, SummonerPotionRegister.StrongObsession);
    }

    @SubscribeEvent
    public static void onCurioChange(CurioChangeEvent event) {
        CuriosUtil.handler(event);
    }

    @SubscribeEvent
    public static void onItemFished(ItemFishedEvent event) {
        Player player = event.getEntity();
        Level level = player.level();
        if (!level.isClientSide() && level.isRaining() && level.getBiome(player.blockPosition()).is(BiomeTags.IS_OCEAN)) {
            if (player.getRandom().nextFloat() < 0.01f) {
                event.getDrops().add(SummonerWeaponRegister.TempestStaff.get().getDefaultInstance());
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDamageEventPostFromMinion(LivingDamageEvent.Post event) {
        DamageSource damageSource = event.getSource();
        LivingEntity target = event.getEntity();
        if (!target.level().isClientSide() && damageSource.getEntity() instanceof Player player) {
            List<VoidEater> voidEaters = LyraHelper.get(player).getEntityData().get(SummonerAttachmentEntityRegister.VOID_EATER.get());
            if (!voidEaters.isEmpty()) {
                target.addEffect(new MobEffectInstance(SummonerMobEffectRegister.GodSlayerInferno, 60));
            }
        }
        if (!target.level().isClientSide() && damageSource.getEntity() instanceof Player player && target.isAlive()) {
            AttachmentEntityData entityData = player.getData(LyraAttachmentRegister.EntityData);
            if (!(damageSource instanceof AttachmentEntityDamageSource attachmentDamageSource) || !(attachmentDamageSource.getAttachmentEntity() instanceof ChlorophyteCrystal)) {
                List<ChlorophyteCrystal> crystals = entityData.get(SummonerAttachmentEntityRegister.CHLOROPHYTE_CRYSTAL.get());
                for (ChlorophyteCrystal crystal : crystals) {
                    if (crystal.getExtraShootCooldown() <= 0) {
                        crystal.setExtraShootCooldown(16);
                        crystal.shootTarget(target);
                    }
                }
            }
            if (!(damageSource instanceof AttachmentEntityDamageSource attachmentDamageSource) || !(attachmentDamageSource.getAttachmentEntity() instanceof StardustCell)) {
                List<StardustCell> stardustCells = entityData.get(SummonerAttachmentEntityRegister.STARDUST_CELL.get());
                for (StardustCell cell : stardustCells) {
                    if (cell.getExtraShootCooldown() <= 0 && cell.getRandom().nextFloat() < 0.33f) {
                        cell.setExtraShootCooldown(14);
                        cell.shootExtraAtTarget(target);
                    }
                }
            }
        }
    }
}
