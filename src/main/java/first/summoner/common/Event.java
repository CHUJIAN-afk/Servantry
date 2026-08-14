package first.summoner.common;

import first.lyra.api.LyraHelper;
import first.lyra.common.attachment.AttachmentEntityData;
import first.lyra.common.minion.MinionDamageSource;
import first.lyra.register.LyraAttachmentRegister;
import first.summoner.Summoner;
import first.summoner.common.dataComponent.ScabbardContainer;
import first.summoner.common.minion.ChlorophyteCrystal;
import first.summoner.common.minion.StardustCell;
import first.summoner.common.minion.VoidEater;
import first.summoner.register.*;
import first.summoner.utils.CuriosUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.BasicItemListing;
import net.neoforged.neoforge.event.ItemStackedOnOtherEvent;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import top.theillusivec4.curios.api.event.CurioChangeEvent;

import java.util.List;

@EventBusSubscriber(modid = Summoner.MODID)
public class Event {

    @SubscribeEvent
    public static void login(PlayerEvent.PlayerLoggedInEvent event) {

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
    public static void onVillagerTrades(VillagerTradesEvent event) {
        if (event.getType() == VillagerProfession.CLERIC) {
            Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = event.getTrades();
            Item[] items = {SummonerCurioRegister.ApprenticesScarf.get(), SummonerCurioRegister.HuntressesBuckler.get(), SummonerCurioRegister.MonksBelt.get(), SummonerCurioRegister.SquiresShield.get()};
            for (Item item : items) {
                trades.get(1).add(new BasicItemListing(20, item.getDefaultInstance(), 8, 10));
            }
            trades.get(3).add(new BasicItemListing(new ItemStack(Items.EMERALD, 25), new ItemStack(Items.SKELETON_SKULL), SummonerCurioRegister.PygmyNecklace.get().getDefaultInstance(), 1, 30, 1.0f));
            trades.get(4).add(new BasicItemListing(30, SummonerCurioRegister.HerculesBeetle.get().getDefaultInstance(), 1, 40));
        }
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
    public static void onLootTableLoad(LootTableLoadEvent event) {
        LootTable table = event.getTable();
        if (event.getName().equals(ResourceLocation.withDefaultNamespace("chests/ancient_city"))) {
            table.addPool(LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(0.05f))
                    .add(LootItem.lootTableItem(SummonerWeaponRegister.InfiniteScabbard.get()))
                    .build());
        }
        if (event.getName().equals(ResourceLocation.withDefaultNamespace("entities/allay"))) {
            table.addPool(LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1f))
                    .add(LootItem.lootTableItem(SummonerWeaponRegister.TerraPrism.get())
                            .when(LootItemRandomChanceCondition.randomChance(0.01f)))
                    .build());
        }
        if (event.getName().equals(ResourceLocation.withDefaultNamespace("entities/evoker"))) {
            table.addPool(LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1f))
                    .add(LootItem.lootTableItem(SummonerCurioRegister.SummonerEmblem.get())
                            .when(LootItemRandomChanceCondition.randomChance(0.1f)))
                    .build());
        }
        if (event.getName().equals(ResourceLocation.withDefaultNamespace("entities/zombie"))) {
            table.addPool(LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1f))
                    .add(LootItem.lootTableItem(SummonerItemRegister.BlackLens.get())
                            .when(LootItemRandomChanceCondition.randomChance(0.01f)))
                    .build());
        }
    }

    @SubscribeEvent
    public static void onLivingDamageEventPostFromMinion(LivingDamageEvent.Post event) {
        DamageSource damageSource = event.getSource();
        LivingEntity target = event.getEntity();
        if (!target.level().isClientSide() && target instanceof Player player) {
            if (SummonerArmorSetRegister.ValhallaKnight.get().full(player)) {
                player.addEffect(new MobEffectInstance(SummonerMobEffectRegister.BallistaPanicked, 100));
            }
        }
        if (!target.level().isClientSide() && damageSource.getEntity() instanceof Player player) {
            if (SummonerArmorSetRegister.Hallowed.get().full(player)) {
                target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60));
            }
            List<VoidEater> voidEaters = LyraHelper.get(player).getEntityData().get(AttachmentEntityData.Type.Minion, SummonerAttachmentEntityRegister.VOID_EATER.get());
            if (!voidEaters.isEmpty()) {
                target.addEffect(new MobEffectInstance(SummonerMobEffectRegister.GodSlayerInferno, 60));
            }
        }
        if (!target.level().isClientSide() && damageSource.getEntity() instanceof Player player && target.isAlive()) {
            AttachmentEntityData entityData = player.getData(LyraAttachmentRegister.EntityData);
            if (!(damageSource instanceof MinionDamageSource minionDamageSource) || !(minionDamageSource.getMinion() instanceof ChlorophyteCrystal)) {
                List<ChlorophyteCrystal> crystals = entityData.get(AttachmentEntityData.Type.ExtraMinion, SummonerAttachmentEntityRegister.CHLOROPHYTE_CRYSTAL.get());
                for (ChlorophyteCrystal crystal : crystals) {
                    if (crystal.getExtraShootCooldown() <= 0) {
                        crystal.setExtraShootCooldown(16);
                        crystal.shootTarget(target);
                    }
                }
            }
            if (!(damageSource instanceof MinionDamageSource minionDamageSource) || !(minionDamageSource.getMinion() instanceof StardustCell)) {
                List<StardustCell> stardustCells = entityData.get(AttachmentEntityData.Type.Minion, SummonerAttachmentEntityRegister.STARDUST_CELL.get());
                for (StardustCell cell : stardustCells) {
                    if (cell.getExtraShootCooldown() <= 0 && player.getRandom().nextFloat() < 0.33f) {
                        cell.setExtraShootCooldown(14);
                        cell.shootExtraAtTarget(target);
                    }
                }
            }
        }
    }
}
