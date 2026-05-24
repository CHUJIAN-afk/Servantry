package first.servantry.common.event;

import first.servantry.Servantry;
import first.servantry.api.common.attachment.EntityData;
import first.servantry.api.event.ServantIncomingDamageEvent;
import first.servantry.api.servant.Servant;
import first.servantry.api.servant.ServantDamageSource;
import first.servantry.common.dataComponent.ScabbardContainer;
import first.servantry.common.servant.StardustCell;
import first.servantry.mixin.servantry.MobEffectInstanceAccessor;
import first.servantry.register.*;
import first.servantry.utils.ArmorSetUtil;
import first.servantry.utils.AttributeUtils;
import first.servantry.utils.CuriosUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
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
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.ItemStackedOnOtherEvent;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import top.theillusivec4.curios.api.event.CurioChangeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = Servantry.MODID)
public class Event {

    @SubscribeEvent
    public static void onItemStackedOnOther(ItemStackedOnOtherEvent event) {
        if (event.getClickAction() == ClickAction.SECONDARY) {
            ItemStack carried = event.getCarriedItem();
            ItemStack stackedOn = event.getStackedOnItem();
            if (carried.is(ItemRegister.InfiniteScabbard.get())) {
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
            Item[] items = {ItemRegister.ApprenticesScarf.get(), ItemRegister.HuntressesBuckler.get(), ItemRegister.MonksBelt.get(), ItemRegister.SquiresShield.get()};
            for (Item item : items) {
                trades.get(1).add(new BasicItemListing(20, item.getDefaultInstance(), 8, 10));
            }
            // 等级3（老手）添加矮人项链交易：25绿宝石 + 1骷髅头
            trades.get(3).add(new BasicItemListing(new ItemStack(Items.EMERALD, 25), new ItemStack(Items.SKELETON_SKULL), ItemRegister.PygmyNecklace.get().getDefaultInstance(), 1, 30, 1.0f));
            // 等级4（专家）添加大力士甲虫交易：30绿宝石
            trades.get(4).add(new BasicItemListing(30, ItemRegister.HerculesBeetle.get().getDefaultInstance(), 1, 40));
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
        Player owner = event.getSource().getServant().getOwner();
        if (!owner.level().isClientSide() && ArmorSetUtil.hasFullSet(owner, ArmorMaterialRegister.HallowedArmorMaterial)) {
            LivingEntity target = event.getEntity();
            MobEffectInstance instance = target.getEffect(MobEffects.GLOWING);
            if (instance == null) {
                target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 59));
            } else {
                MobEffectInstanceAccessor accessor = (MobEffectInstanceAccessor) instance;
                accessor.setDuration(59);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
        LivingEntity living = event.getEntity();
        if (living instanceof Player player && !player.level().isClientSide()) {
            AttributeUtils.condition(player, AttributeRegister.ServantMaxCount, Servantry.rl("hallowed_set_servant_max_count"), 2, AttributeModifier.Operation.ADD_VALUE, ArmorSetUtil.hasFullSet(player, ArmorMaterialRegister.HallowedArmorMaterial));
            AttributeUtils.condition(player, AttributeRegister.ServantDamage, Servantry.rl("hallowed_set_servant_damage"), 0.15, AttributeModifier.Operation.ADD_VALUE, ArmorSetUtil.hasFullSet(player, ArmorMaterialRegister.HallowedArmorMaterial));
        }
    }

    @SubscribeEvent
    public static void onItemAttributeModifier(ItemAttributeModifierEvent event) {
        Item item = event.getItemStack().getItem();
        // 头部：+2 仆从栏，+25% 仆从伤害
        if (item == ItemRegister.HallowedHelmet.get()) {
            event.addModifier(AttributeRegister.ServantMaxCount, new AttributeModifier(ResourceLocation.fromNamespaceAndPath(Servantry.MODID, "witch_helmet_count"), 2, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.HEAD);
            event.addModifier(AttributeRegister.ServantDamage, new AttributeModifier(ResourceLocation.fromNamespaceAndPath(Servantry.MODID, "witch_helmet_damage"), 0.25, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), EquipmentSlotGroup.HEAD);
        }
        // 胸甲：+3 仆从栏，+25% 仆从伤害
        if (item == ItemRegister.HallowedChestplate.get()) {
            event.addModifier(AttributeRegister.ServantMaxCount, new AttributeModifier(ResourceLocation.fromNamespaceAndPath(Servantry.MODID, "witch_chestplate_count"), 3, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.CHEST);
            event.addModifier(AttributeRegister.ServantDamage, new AttributeModifier(ResourceLocation.fromNamespaceAndPath(Servantry.MODID, "witch_chestplate_damage"), 0.25, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), EquipmentSlotGroup.CHEST);
        }
        // 护腿：+1 仆从栏，+25% 仆从伤害
        if (item == ItemRegister.HallowedLeggings.get()) {
            event.addModifier(AttributeRegister.ServantMaxCount, new AttributeModifier(ResourceLocation.fromNamespaceAndPath(Servantry.MODID, "witch_leggings_count"), 1, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.LEGS);
            event.addModifier(AttributeRegister.ServantDamage, new AttributeModifier(ResourceLocation.fromNamespaceAndPath(Servantry.MODID, "witch_leggings_damage"), 0.25, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), EquipmentSlotGroup.LEGS);
        }
        // 鞋子：+1 仆从栏，+25% 仆从伤害，+15% 移动速度
        if (item == ItemRegister.HallowedBoots.get()) {
            event.addModifier(AttributeRegister.ServantMaxCount, new AttributeModifier(ResourceLocation.fromNamespaceAndPath(Servantry.MODID, "witch_boots_count"), 1, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.FEET);
            event.addModifier(AttributeRegister.ServantDamage, new AttributeModifier(ResourceLocation.fromNamespaceAndPath(Servantry.MODID, "witch_boots_damage"), 0.25, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), EquipmentSlotGroup.FEET);
            event.addModifier(Attributes.MOVEMENT_SPEED, new AttributeModifier(ResourceLocation.fromNamespaceAndPath(Servantry.MODID, "witch_boots_speed"), 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), EquipmentSlotGroup.FEET);
        }
    }

    @SubscribeEvent
    public static void onItemFished(ItemFishedEvent event) {
        Player player = event.getEntity();
        Level level = player.level();
        if (!level.isClientSide() && level.isRaining() && level.getBiome(player.blockPosition()).is(BiomeTags.IS_OCEAN)) {
            if (player.getRandom().nextFloat() < 0.01f) {
                event.getDrops().add(ItemRegister.TempestStaff.get().getDefaultInstance());
            }
        }
    }

    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        if (event.getName().equals(ResourceLocation.withDefaultNamespace("chests/ancient_city"))) {
            event.getTable().addPool(LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(0.05f))
                    .add(LootItem.lootTableItem(ItemRegister.InfiniteScabbard.get()))
                    .build());
        }
        if (event.getName().equals(ResourceLocation.withDefaultNamespace("entities/allay"))) {
            event.getTable().addPool(LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1f))
                    .add(LootItem.lootTableItem(ItemRegister.TerraPrism.get())
                            .when(LootItemRandomChanceCondition.randomChance(0.01f)))
                    .build());
        }
        if (event.getName().equals(ResourceLocation.withDefaultNamespace("entities/evoker"))) {
            event.getTable().addPool(LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1f))
                    .add(LootItem.lootTableItem(ItemRegister.SummonerEmblem.get())
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
    public static void onLivingDamageEventPostFromStardustCell(LivingDamageEvent.Post event) {
        DamageSource damageSource = event.getSource();
        if (damageSource instanceof ServantDamageSource servantDamageSource) {
            if (servantDamageSource.getServant() instanceof StardustCell) {
                return;
            }
        }
        if (damageSource.getEntity() instanceof Player player && !player.level().isClientSide()) {
            LivingEntity target = event.getEntity();
            if (target.isAlive()) {
                EntityData entityData = player.getData(AttachmentRegister.EntityData);
                for (Servant servant : entityData.getServants()) {
                    if (servant instanceof StardustCell cell && cell.getExtraShootCooldown() <= 0 && player.getRandom().nextFloat() < 0.33f) {
                        cell.setExtraShootCooldown(14);
                        cell.shootAtTarget(target);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDamageEventPostFromCurios(LivingDamageEvent.Post event) {
        DamageSource damageSource = event.getSource();
        if (damageSource instanceof ServantDamageSource servantDamageSource) {
            Servant servant = servantDamageSource.getServant();
            if (servant != null) {
                Player owner = servant.getOwner();
                List<Holder<MobEffect>> effects = new ArrayList<>();
                if (CuriosUtil.isEquipped(owner, ItemRegister.PhantasmalRelic.get())) {
                    effects.add(MobEffectRegister.PhantasmalMight);
                    effects.add(MobEffectRegister.PhantasmalBulwark);
                    effects.add(MobEffectRegister.PhantasmalRebirth);
                } else if (CuriosUtil.isEquipped(owner, ItemRegister.HallowedRune.get())) {
                    effects.add(MobEffectRegister.HallowedMight);
                    effects.add(MobEffectRegister.HallowedGrace);
                    effects.add(MobEffectRegister.HallowedRadiance);
                } else if (CuriosUtil.isEquipped(owner, ItemRegister.SoulRelief.get())) {
                    effects.add(MobEffectRegister.SoulMight);
                    effects.add(MobEffectRegister.SoulDefense);
                    effects.add(MobEffectRegister.SoulRecovery);
                }
                if (!effects.isEmpty()) {
                    owner.addEffect(new MobEffectInstance(effects.get(owner.getRandom().nextInt(effects.size())), 60));
                }
            }
        }
    }
}
