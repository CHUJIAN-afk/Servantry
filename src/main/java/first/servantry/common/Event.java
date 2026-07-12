package first.servantry.common;

import first.servantry.Servantry;
import first.servantry.api.ServantryHelper;
import first.servantry.api.common.attachment.EntityData;
import first.servantry.api.servant.ServantDamageSource;
import first.servantry.common.dataComponent.ScabbardContainer;
import first.servantry.common.servant.ChlorophyteCrystal;
import first.servantry.common.servant.StardustCell;
import first.servantry.common.servant.VoidEater;
import first.servantry.register.*;
import first.servantry.utils.CuriosUtil;
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
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import top.theillusivec4.curios.api.event.CurioChangeEvent;

import java.util.List;

@EventBusSubscriber(modid = Servantry.MODID)
public class Event {

    @SubscribeEvent
    public static void onItemStackedOnOther(ItemStackedOnOtherEvent event) {
        if (event.getClickAction() == ClickAction.SECONDARY) {
            ItemStack carried = event.getCarriedItem();
            ItemStack stackedOn = event.getStackedOnItem();
            if (carried.is(ServantryServantWeaponRegister.InfiniteScabbard.get())) {
                Slot slot = event.getSlot();
                Player player = event.getPlayer();
                ScabbardContainer scabbard = carried.getOrDefault(ServantryDataComponentRegister.Scabbard.get(), ScabbardContainer.EMPTY);
                if (!stackedOn.isEmpty() && scabbard.isEmpty()) {
                    carried.set(ServantryDataComponentRegister.Scabbard.get(), new ScabbardContainer(stackedOn.copy()));
                    slot.set(ItemStack.EMPTY);
                    player.playSound(SoundEvents.ARMOR_EQUIP_LEATHER.value(), 1.0F, 1.2F);
                    event.setCanceled(true);
                } else if (stackedOn.isEmpty() && !scabbard.isEmpty()) {
                    slot.set(scabbard.itemStack().copy());
                    carried.set(ServantryDataComponentRegister.Scabbard.get(), ScabbardContainer.EMPTY);
                    player.playSound(SoundEvents.ARMOR_EQUIP_LEATHER.value(), 1.0F, 0.9F);
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRegisterBrewingRecipes(RegisterBrewingRecipesEvent event) {
        PotionBrewing.Builder builder = event.getBuilder();
        builder.addMix(Potions.AWKWARD, Items.WITHER_ROSE, ServantryPotionRegister.Obsession);
        builder.addMix(ServantryPotionRegister.Obsession, Items.REDSTONE, ServantryPotionRegister.LongObsession);
        builder.addMix(ServantryPotionRegister.Obsession, Items.GLOWSTONE_DUST, ServantryPotionRegister.StrongObsession);
    }

    @SubscribeEvent
    public static void onVillagerTrades(VillagerTradesEvent event) {
        if (event.getType() == VillagerProfession.CLERIC) {
            Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = event.getTrades();
            Item[] items = {ServantryCurioRegister.ApprenticesScarf.get(), ServantryCurioRegister.HuntressesBuckler.get(), ServantryCurioRegister.MonksBelt.get(), ServantryCurioRegister.SquiresShield.get()};
            for (Item item : items) {
                trades.get(1).add(new BasicItemListing(20, item.getDefaultInstance(), 8, 10));
            }
            trades.get(3).add(new BasicItemListing(new ItemStack(Items.EMERALD, 25), new ItemStack(Items.SKELETON_SKULL), ServantryCurioRegister.PygmyNecklace.get().getDefaultInstance(), 1, 30, 1.0f));
            trades.get(4).add(new BasicItemListing(30, ServantryCurioRegister.HerculesBeetle.get().getDefaultInstance(), 1, 40));
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
                event.getDrops().add(ServantryServantWeaponRegister.TempestStaff.get().getDefaultInstance());
            }
        }
    }

    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        LootTable table = event.getTable();
        if (event.getName().equals(ResourceLocation.withDefaultNamespace("chests/ancient_city"))) {
            table.addPool(LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(0.05f))
                    .add(LootItem.lootTableItem(ServantryServantWeaponRegister.InfiniteScabbard.get()))
                    .build());
        }
        if (event.getName().equals(ResourceLocation.withDefaultNamespace("entities/allay"))) {
            table.addPool(LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1f))
                    .add(LootItem.lootTableItem(ServantryServantWeaponRegister.TerraPrism.get())
                            .when(LootItemRandomChanceCondition.randomChance(0.01f)))
                    .build());
        }
        if (event.getName().equals(ResourceLocation.withDefaultNamespace("entities/evoker"))) {
            table.addPool(LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1f))
                    .add(LootItem.lootTableItem(ServantryCurioRegister.SummonerEmblem.get())
                            .when(LootItemRandomChanceCondition.randomChance(0.1f)))
                    .build());
        }
        if (event.getName().equals(ResourceLocation.withDefaultNamespace("entities/zombie"))) {
            table.addPool(LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1f))
                    .add(LootItem.lootTableItem(ServantryItemRegister.BlackLens.get())
                            .when(LootItemRandomChanceCondition.randomChance(0.01f)))
                    .build());
        }
    }

    @SubscribeEvent
    public static void onLivingDamageEventPostFromServant(LivingDamageEvent.Post event) {
        DamageSource damageSource = event.getSource();
        LivingEntity target = event.getEntity();
        if (!target.level().isClientSide() && target instanceof Player player) {
            player.addEffect(new MobEffectInstance(ServantryMobEffectRegister.BallistaPanicked, 100));
        }
        if (!target.level().isClientSide() && damageSource.getEntity() instanceof Player player) {
            if (ServantryArmorSetRegister.Hallowed.value().full(player)) {
                target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60));
            }
            List<VoidEater> voidEaters = ServantryHelper.get(player).getEntityData().get(EntityData.Type.Servant, VoidEater.class);
            if (!voidEaters.isEmpty()) {
                target.addEffect(new MobEffectInstance(ServantryMobEffectRegister.GodSlayerInferno, 60));
            }
        }
        if (!target.level().isClientSide() && damageSource.getEntity() instanceof Player player && target.isAlive()) {
            EntityData entityData = player.getData(ServantryAttachmentRegister.EntityData);
            if (!(damageSource instanceof ServantDamageSource servantDamageSource) || !(servantDamageSource.getServant() instanceof ChlorophyteCrystal)) {
                List<ChlorophyteCrystal> crystals = entityData.get(EntityData.Type.ExtraServant, ChlorophyteCrystal.class);
                for (ChlorophyteCrystal crystal : crystals) {
                    if (crystal.getExtraShootCooldown() <= 0) {
                        crystal.setExtraShootCooldown(16);
                        crystal.shootTarget(target);
                    }
                }
            }
            if (!(damageSource instanceof ServantDamageSource servantDamageSource) || !(servantDamageSource.getServant() instanceof StardustCell)) {
                List<StardustCell> stardustCells = entityData.get(EntityData.Type.Servant, StardustCell.class);
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
