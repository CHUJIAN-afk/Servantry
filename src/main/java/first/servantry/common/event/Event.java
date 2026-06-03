package first.servantry.common.event;

import first.servantry.Servantry;
import first.servantry.api.common.attachment.EntityData;
import first.servantry.api.event.ServantIncomingDamageEvent;
import first.servantry.api.servant.Servant;
import first.servantry.api.servant.ServantDamageSource;
import first.servantry.common.dataComponent.ScabbardContainer;
import first.servantry.common.servant.ChlorophyteCrystal;
import first.servantry.common.servant.StardustCell;
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
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootPool;
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
import java.util.Map;

@EventBusSubscriber(modid = Servantry.MODID)
public class Event {

    @SubscribeEvent
    public static void onItemStackedOnOther(ItemStackedOnOtherEvent event) {
        if (event.getClickAction() == ClickAction.SECONDARY) {
            ItemStack carried = event.getCarriedItem();
            ItemStack stackedOn = event.getStackedOnItem();
            if (carried.is(ServantWeaponRegister.InfiniteScabbard.get())) {
                Slot slot = event.getSlot();
                Player player = event.getPlayer();
                ScabbardContainer scabbard = carried.getOrDefault(DataComponentRegister.Scabbard.get(), ScabbardContainer.EMPTY);
                if (!stackedOn.isEmpty() && scabbard.isEmpty()) {
                    carried.set(DataComponentRegister.Scabbard.get(), new ScabbardContainer(stackedOn.copy()));
                    slot.set(ItemStack.EMPTY);
                    player.playSound(SoundEvents.ARMOR_EQUIP_LEATHER.value(), 1.0F, 1.2F);
                    event.setCanceled(true);
                } else if (stackedOn.isEmpty() && !scabbard.isEmpty()) {
                    slot.set(scabbard.itemStack().copy());
                    carried.set(DataComponentRegister.Scabbard.get(), ScabbardContainer.EMPTY);
                    player.playSound(SoundEvents.ARMOR_EQUIP_LEATHER.value(), 1.0F, 0.9F);
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRegisterBrewingRecipes(RegisterBrewingRecipesEvent event) {
        // 着魔药水：粗制的药水 + 凋零玫瑰
        event.getBuilder().addMix(Potions.AWKWARD, Items.WITHER_ROSE, PotionRegister.Obsession);
        // 长效着魔药水：着魔药水 + 红石
        event.getBuilder().addMix(PotionRegister.Obsession, Items.REDSTONE, PotionRegister.LongObsession);
        // 强效着魔药水：着魔药水 + 荧石
        event.getBuilder().addMix(PotionRegister.Obsession, Items.GLOWSTONE_DUST, PotionRegister.StrongObsession);
    }

    @SubscribeEvent
    public static void onVillagerTrades(VillagerTradesEvent event) {
        if (event.getType() == VillagerProfession.CLERIC) {
            Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = event.getTrades();
            // 等级1（新手）添加四件基础召唤饰品交易：20绿宝石
            Item[] items = {CurioRegister.ApprenticesScarf.get(), CurioRegister.HuntressesBuckler.get(), CurioRegister.MonksBelt.get(), CurioRegister.SquiresShield.get()};
            for (Item item : items) {
                trades.get(1).add(new BasicItemListing(20, item.getDefaultInstance(), 8, 10));
            }
            // 等级3（老手）添加矮人项链交易：25绿宝石 + 1骷髅头
            trades.get(3).add(new BasicItemListing(new ItemStack(Items.EMERALD, 25), new ItemStack(Items.SKELETON_SKULL), CurioRegister.PygmyNecklace.get().getDefaultInstance(), 1, 30, 1.0f));
            // 等级4（专家）添加大力士甲虫交易：30绿宝石
            trades.get(4).add(new BasicItemListing(30, CurioRegister.HerculesBeetle.get().getDefaultInstance(), 1, 40));
        }
    }

    @SubscribeEvent
    public static void onCurioChange(CurioChangeEvent event) {
        Map<Item, Boolean> cache = CuriosUtil.CACHE.get(event.getEntity().getUUID());
        if (cache != null && !cache.isEmpty()) {
            cache.clear();
        }
    }

    @SubscribeEvent
    public static void onServantIncomingDamage(ServantIncomingDamageEvent event) {
        Servant servant = event.getSource().getServant();
        if (servant != null) {
            Player owner = servant.getOwner();
            if (!owner.level().isClientSide()) {
                LivingEntity target = event.getEntity();
                if (ArmorSetRegister.Hallowed.value().full(owner)) {
                    target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onItemFished(ItemFishedEvent event) {
        Player player = event.getEntity();
        Level level = player.level();
        if (!level.isClientSide() && level.isRaining() && level.getBiome(player.blockPosition()).is(BiomeTags.IS_OCEAN)) {
            if (player.getRandom().nextFloat() < 0.01f) {
                event.getDrops().add(ServantWeaponRegister.TempestStaff.get().getDefaultInstance());
            }
        }
    }

    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        if (event.getName().equals(ResourceLocation.withDefaultNamespace("chests/ancient_city"))) {
            event.getTable().addPool(LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(0.05f))
                    .add(LootItem.lootTableItem(ServantWeaponRegister.InfiniteScabbard.get()))
                    .build());
        }
        if (event.getName().equals(ResourceLocation.withDefaultNamespace("entities/allay"))) {
            event.getTable().addPool(LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1f))
                    .add(LootItem.lootTableItem(ServantWeaponRegister.TerraPrism.get())
                            .when(LootItemRandomChanceCondition.randomChance(0.01f)))
                    .build());
        }
        if (event.getName().equals(ResourceLocation.withDefaultNamespace("entities/evoker"))) {
            event.getTable().addPool(LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1f))
                    .add(LootItem.lootTableItem(CurioRegister.SummonerEmblem.get())
                            .when(LootItemRandomChanceCondition.randomChance(0.1f)))
                    .build());
        }
        if (event.getName().equals(ResourceLocation.withDefaultNamespace("entities/zombie"))) {
            event.getTable().addPool(LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1f))
                    .add(LootItem.lootTableItem(ItemRegister.BlackLens.get())
                            .when(LootItemRandomChanceCondition.randomChance(0.01f)))
                    .build());
        }
    }

    @SubscribeEvent
    public static void onLivingDamageEventPostFromServant(LivingDamageEvent.Post event) {
        DamageSource damageSource = event.getSource();
        LivingEntity target = event.getEntity();
        if (!target.level().isClientSide() && damageSource.getEntity() instanceof Player player && target.isAlive()) {
            EntityData entityData = player.getData(AttachmentRegister.EntityData);
            if (!(damageSource instanceof ServantDamageSource servantDamageSource) || !(servantDamageSource.getServant() instanceof ChlorophyteCrystal)) {
                entityData.getExtraServants().stream()
                        .filter(servant -> servant instanceof ChlorophyteCrystal)
                        .map(servant -> (ChlorophyteCrystal) servant)
                        .forEach(crystal -> {
                            if (crystal.getExtraShootCooldown() <= 0) {
                                crystal.setExtraShootCooldown(16);
                                crystal.shootTarget(target);
                            }
                        });
            }
            if (!(damageSource instanceof ServantDamageSource servantDamageSource) || !(servantDamageSource.getServant() instanceof StardustCell)) {
                entityData.getServants().stream()
                        .filter(servant -> servant instanceof StardustCell)
                        .map(servant -> (StardustCell) servant)
                        .forEach(cell -> {
                            if (cell.getExtraShootCooldown() <= 0 && player.getRandom().nextFloat() < 0.33f) {
                                cell.setExtraShootCooldown(14);
                                cell.shootAtTarget(target);
                            }
                        });
            }
        }
    }
}
