package first.summoner.register;

import first.lyra.api.LyraHelper;
import first.lyra.client.creativeTab.AnimBanner;
import first.lyra.common.attachment.AttachmentEntityData;
import first.lyra.common.builder.MinionWeaponItemBuilder;
import first.lyra.common.creativeTab.Section;
import first.lyra.common.dataComponent.LyraRarity;
import first.lyra.common.entity.PathNode;
import first.lyra.common.item.SummonerWeaponItem;
import first.lyra.common.minion.MinionSlotType;
import first.lyra.register.LyraAttributeRegister;
import first.lyra.register.LyraDataComponentRegister;
import first.lyra.register.LyraItemRegistries;
import first.summoner.Summoner;
import first.summoner.common.dataComponent.ScabbardContainer;
import first.summoner.common.item.GildedWandItem;
import first.summoner.common.minion.*;
import first.summoner.common.sentry.*;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.WeatherCheck;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("unused")
public class SummonerWeaponRegister {

    public static final LyraItemRegistries REGISTRIES = Summoner.REGISTRIES;

    public static final Section MINION_WEAPON_TOOL = new Section(-1, Summoner.rl("textures/item/banner/default_banner.png"), new AnimBanner(18, 1, 1), SummonerItemTagsRegister.SectionMinionWeaponTool, provider -> {
        List<ItemStack> list = new ArrayList<>();
        HolderLookup<Enchantment> enchantments = provider.lookupOrThrow(Registries.ENCHANTMENT);
        Collection<DeferredHolder<Enchantment, ? extends Enchantment>> entries = SummonerEnchantmentRegister.Register.getEntries();
        for (DeferredHolder<Enchantment, ? extends Enchantment> entry : entries) {
            ItemStack book = Items.ENCHANTED_BOOK.getDefaultInstance();
            book.enchant(enchantments.getOrThrow(entry.getKey()), 1);
            list.add(book);
        }
        return list;
    });

    public static final Section MINION_WEAPON = new Section(0, Summoner.rl("textures/item/banner/default_banner.png"), new AnimBanner(18, 1, 1), SummonerItemTagsRegister.SectionMinionWeapon);

    public static final DeferredItem<GildedWandItem> GildedWand =
            REGISTRIES.build("gilded_wand", ResourceLocation -> new GildedWandItem(() -> new Item.Properties()
                            .stacksTo(1)
                            .rarity(Rarity.EPIC)
                            .component(LyraDataComponentRegister.RARITY, new LyraRarity(0xA66800))
                            .durability(15 * 60 * 20)
                            .component(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY)
                            .component(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.builder()
                                    .add(LyraAttributeRegister.SummonDamage, new AttributeModifier(ResourceLocation, 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), EquipmentSlotGroup.HAND)
                                    .add(LyraAttributeRegister.HealthRegen, new AttributeModifier(ResourceLocation, 0.2, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.HAND)
                                    .build())
                    ))
                    .itemLanguage("Gilded Wand", "镀金魔杖")
                    .itemLanguageTooltip(1, "", "持续引导标记目标以积累标记能量，使你的召唤物集中攻击标记目标")
                    .itemLanguageTooltip(2, "", "可通过附魔获得额外标记效果")
                    .itemLanguageTooltip(3, "", "潜行右键遣散所有仆从与哨兵")
                    .itemTag(SummonerItemTagsRegister.SectionMinionWeaponTool)
                    .itemTag(ItemTags.DURABILITY_ENCHANTABLE)
                    .itemModel(ItemModelProvider::handheldItem)
                    .recipeWithLookup((lookup, recipeOutput) -> ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, SummonerWeaponRegister.GildedWand)
                            .pattern("  G")
                            .pattern(" S ")
                            .pattern("G  ")
                            .define('G', Items.GOLD_INGOT)
                            .define('S', Items.STICK)
                            .unlockedBy("has_gold_ingot", InventoryChangeTrigger.TriggerInstance.hasItems(Items.GOLD_INGOT))
                            .save(recipeOutput, Summoner.rl("crafting/gilded_wand")))
                    .build();
    /**
     * 黄蜂法杖
     */
    public static final DeferredItem<SummonerWeaponItem<Hornet>> HornetStaff =
            REGISTRIES.build("hornet_staff", ResourceLocation -> new MinionWeaponItemBuilder<>(ResourceLocation, SummonerAttachmentEntityRegister.HORNET)
                            .damage(0.7f)
                            .knockback(0.2f)
                            .sound(SummonerSoundRegister.UseMinionWeapon)
                            .properties(properties -> properties.rarity(Rarity.UNCOMMON).component(LyraDataComponentRegister.RARITY, new LyraRarity(0xFFDA41)))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionMinionWeapon)
                    .itemLanguage("Hornet Staff", "黄蜂法杖")
                    .summonLanguage(SummonerAttachmentEntityRegister.HORNET, "Hornet", "黄蜂")
                    .lootTable(ResourceLocation.withDefaultNamespace("entities/bee"), LootTable -> LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1f))
                            .add(LootItem.lootTableItem(SummonerWeaponRegister.HornetStaff.get())
                                         .when(LootItemRandomChanceCondition.randomChance(0.05f)))
                            .build())
                    .itemModel(ItemModelProvider::handheldItem)
                    .itemTag(SummonerItemTagsRegister.MinionWeapon)
                    .build();
    /**
     * 小鬼法杖
     */
    public static final DeferredItem<SummonerWeaponItem<Imp>> ImpStaff =
            REGISTRIES.build("imp_staff", ResourceLocation -> new MinionWeaponItemBuilder<>(ResourceLocation, SummonerAttachmentEntityRegister.IMP)
                            .damage(1.7f)
                            .knockback(0.2f)
                            .sound(SummonerSoundRegister.UseImpStaff)
                            .properties(properties -> properties.rarity(Rarity.RARE).component(LyraDataComponentRegister.RARITY, new LyraRarity(0xFE7902)))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionMinionWeapon)
                    .itemLanguage("Imp Staff", "小鬼法杖")
                    .summonLanguage(SummonerAttachmentEntityRegister.IMP, "Imp", "小鬼")
                    .lootTable(ResourceLocation.withDefaultNamespace("entities/magma_cube"), LootTable -> {
                        CompoundTag tag = new CompoundTag();
                        tag.putInt("Size", 3);
                        return LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1f))
                                .add(LootItem.lootTableItem(SummonerWeaponRegister.ImpStaff.get())
                                             .when(LootItemRandomChanceCondition.randomChance(0.01f))
                                             .when(LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().nbt(new NbtPredicate(tag)))))
                                .build();
                    })
                    .lootTable(ResourceLocation.withDefaultNamespace("chests/shipwreck_treasure"), LootTable -> LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1f))
                            .add(LootItem.lootTableItem(SummonerWeaponRegister.ImpStaff.get())
                                         .when(LootItemRandomChanceCondition.randomChance(0.05f)))
                            .build())
                    .itemModel(ItemModelProvider::handheldItem)
                    .itemTag(SummonerItemTagsRegister.MinionWeapon)
                    .build();
    /**
     * 无人机
     */
    public static final DeferredItem<SummonerWeaponItem<OreScout>> SurveyDroneRemote =
            REGISTRIES.build("survey_drone_remote", ResourceLocation -> new MinionWeaponItemBuilder<>(ResourceLocation, SummonerAttachmentEntityRegister.ORE_SCOUT)
                            .sound(SummonerSoundRegister.UseMinionWeapon)
                            .summon((weapon, player, itemStack) -> {
                                OreScout minion = weapon.createMinion(player, itemStack);
                                LyraHelper summonerHelper = LyraHelper.get(player);
                                MinionSlotType slotType = weapon.getSlotType(itemStack);
                                AttachmentEntityData entityData = summonerHelper.getEntityData();
                                if (entityData.get(weapon.getEntityType()).isEmpty() && summonerHelper.canSummon(slotType, minion.getSlotCost())) {
                                    minion.init(minion.getRenderNode(1.0f));
                                    summonerHelper.add(minion);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.UNCOMMON).component(LyraDataComponentRegister.RARITY, new LyraRarity(0xAB5D66)))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionMinionWeapon)
                    .itemLanguage("Survey Drone Remote", "矿勘无人机遥控器")
                    .summonLanguage(SummonerAttachmentEntityRegister.ORE_SCOUT, "Survey Drone", "矿勘无人机")
                    .itemLanguageTooltip(1, "Summons up to 1 Survey Drone", "最多召唤1架矿勘无人机")
                    .itemModel(ItemModelProvider::basicItem)
                    .itemTag(SummonerItemTagsRegister.MinionWeapon)
                    .build();
    /**
     * 妖精铃铛
     */
    public static final DeferredItem<SummonerWeaponItem<ScavengerFairy>> FairyBell =
            REGISTRIES.build("fairy_bell", ResourceLocation -> new MinionWeaponItemBuilder<>(ResourceLocation, SummonerAttachmentEntityRegister.SCAVENGER_FAIRY)
                            .sound(SummonerSoundRegister.UseMinionWeapon)
                            .summon((weapon, player, itemStack) -> {
                                ScavengerFairy minion = weapon.createMinion(player, itemStack);
                                LyraHelper summonerHelper = LyraHelper.get(player);
                                MinionSlotType slotType = weapon.getSlotType(itemStack);
                                AttachmentEntityData entityData = summonerHelper.getEntityData();
                                if (entityData.get(weapon.getEntityType()).isEmpty() && summonerHelper.canSummon(slotType, minion.getSlotCost())) {
                                    minion.init(minion.getRenderNode(1.0f));
                                    summonerHelper.add(minion);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.UNCOMMON).component(LyraDataComponentRegister.RARITY, new LyraRarity(0x936F20)))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionMinionWeapon)
                    .itemLanguage("Fairy Bell", "妖精铃铛")
                    .summonLanguage(SummonerAttachmentEntityRegister.SCAVENGER_FAIRY, "Scavenger Fairy", "拾荒妖精")
                    .itemLanguageTooltip(1, "Summons up to 1 Scavenger Fairy", "最多召唤1只拾荒妖精")
                    .lootTable(ResourceLocation.withDefaultNamespace("chests/shipwreck_treasure"), LootTable -> LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1f))
                            .add(LootItem.lootTableItem(SummonerWeaponRegister.FairyBell.get())
                                         .when(LootItemRandomChanceCondition.randomChance(0.05f)))
                            .build())
                    .itemModel(ItemModelProvider::basicItem)
                    .itemTag(SummonerItemTagsRegister.MinionWeapon)
                    .build();
    /**
     * 刃杖 - 召唤附魔飞刀群
     */
    public static final DeferredItem<SummonerWeaponItem<EnchantedThrowingKnives>> BladeStaff =
            REGISTRIES.build("blade_staff", ResourceLocation -> new MinionWeaponItemBuilder<>(ResourceLocation, SummonerAttachmentEntityRegister.ENCHANTED_THROWING_KNIVES)
                            .damage(0.6f)
                            .armorPierce(7.5f)
                            .sound(SummonerSoundRegister.UseMinionWeapon)
                            .summon((weapon, player, itemStack) -> {
                                EnchantedThrowingKnives minion = weapon.createMinion(player, itemStack);
                                LyraHelper summonerHelper = LyraHelper.get(player);
                                MinionSlotType slotType = weapon.getSlotType(itemStack);
                                if (summonerHelper.canSummon(slotType, minion.getSlotCost())) {
                                    PathNode idle = minion.getInterpolatedIdleState(1.0f);
                                    Vec3 center = player.getBoundingBox().getCenter();
                                    minion.init(new PathNode(new Vec3(center.x(), idle.pos().y(), center.z()), idle.yaw(), idle.pitch(), idle.roll()));
                                    summonerHelper.add(minion);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.UNCOMMON).component(LyraDataComponentRegister.RARITY, new LyraRarity(0x636BAF)))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionMinionWeapon)
                    .itemLanguage("Blade Staff", "刃杖")
                    .summonLanguage(SummonerAttachmentEntityRegister.ENCHANTED_THROWING_KNIVES, "Enchanted Throwing Knives", "附魔飞刀")
                    .itemLanguageTooltip(1, "Don't let their small size fool you", "别被它们小小的个头给骗了")
                    .recipeWithLookup((lookup, recipeOutput) -> ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, SummonerWeaponRegister.BladeStaff)
                            .pattern("  I")
                            .pattern(" A ")
                            .pattern("S  ")
                            .define('I', Items.IRON_SWORD)
                            .define('A', Items.AMETHYST_SHARD)
                            .define('S', Items.STICK)
                            .unlockedBy("has_amethyst_shard", InventoryChangeTrigger.TriggerInstance.hasItems(Items.AMETHYST_SHARD))
                            .save(recipeOutput, Summoner.rl("crafting/blade_staff")))
                    .itemModel(ItemModelProvider::handheldItem)
                    .itemTag(SummonerItemTagsRegister.MinionWeapon)
                    .build();
    /**
     * 雨云法杖
     */
    public static final DeferredItem<SummonerWeaponItem<Cloud>> RainCloudStaff =
            REGISTRIES.build("rain_cloud_staff", ResourceLocation -> new MinionWeaponItemBuilder<>(ResourceLocation, SummonerAttachmentEntityRegister.CLOUD)
                            .damage(3f)
                            .slotType(MinionSlotType.Sentry)
                            .sound(SummonerSoundRegister.UseCloudStaff)
                            .summon((weapon, player, itemStack) -> {
                                Cloud cloud = weapon.createMinion(player, itemStack);
                                LyraHelper summonerHelper = LyraHelper.get(player);
                                MinionSlotType slotType = weapon.getSlotType(itemStack);
                                if (summonerHelper.canSummon(slotType, cloud.getSlotCost())) {
                                    cloud.init(new PathNode(player.getBoundingBox().getCenter(), 0, 0, 0));
                                    cloud.setVelocity(player.getLookAngle());
                                    summonerHelper.add(cloud);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC).component(LyraDataComponentRegister.RARITY, new LyraRarity(0xA6670C)))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionMinionWeapon)
                    .itemLanguage("Rain Cloud Staff", "雨云法杖")
                    .summonLanguage(SummonerAttachmentEntityRegister.CLOUD, "Rain Cloud", "雨云")
                    .itemLanguageTooltip(1, "Summons a cloud to rain down on your foes", "召唤云朵来向敌人降下大雨")
                    .lootTable(ResourceLocation.withDefaultNamespace("entities/phantom"), LootTable -> LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1f))
                            .add(LootItem.lootTableItem(SummonerWeaponRegister.RainCloudStaff.get())
                                         .when(LootItemRandomChanceCondition.randomChance(0.05f))
                                         .when(WeatherCheck.weather().setRaining(true)))
                            .build())
                    .itemModel(ItemModelProvider::handheldItem)
                    .itemTag(SummonerItemTagsRegister.MinionWeapon)
                    .build();
    /**
     * 魔眼法杖 - 召唤双子魔眼
     */
    public static final DeferredItem<SummonerWeaponItem<Twins>> OpticStaff =
            REGISTRIES.build("optic_staff", ResourceLocation -> new MinionWeaponItemBuilder<>(ResourceLocation, SummonerAttachmentEntityRegister.TWINS)
                            .damage(2.4f)
                            .knockback(0.2f)
                            .sound(SummonerSoundRegister.UseTerraprism)
                            .summon((weapon, player, itemStack) -> {
                                Twins laserEye = weapon.createMinion(player, itemStack);
                                laserEye.setLaserEye(true);
                                laserEye.setSlotCost(1);
                                LyraHelper summonerHelper = LyraHelper.get(player);
                                MinionSlotType slotType = weapon.getSlotType(itemStack);
                                if (summonerHelper.canSummon(slotType, laserEye.getSlotCost())) {
                                    RandomSource random = player.getRandom();
                                    PathNode pathNode = new PathNode(player.getBoundingBox().getCenter().offsetRandom(random, 2), 0, 0, 0);
                                    laserEye.init(pathNode);
                                    summonerHelper.add(laserEye);

                                    Twins flameEye = weapon.createMinion(player, itemStack);
                                    flameEye.setFlameEye(true);
                                    flameEye.setSlotCost(0);
                                    flameEye.setOther(laserEye);
                                    flameEye.init(pathNode);
                                    summonerHelper.add(flameEye);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.RARE).component(LyraDataComponentRegister.RARITY, new LyraRarity(0xFF4040)))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionMinionWeapon)
                    .itemLanguage("Optic Staff", "魔眼法杖")
                    .summonLanguage(SummonerAttachmentEntityRegister.TWINS, "Twins", "双子魔眼")
                    .lootTable(ResourceLocation.withDefaultNamespace("entities/ghast"), LootTable -> LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1f))
                            .add(LootItem.lootTableItem(SummonerWeaponRegister.OpticStaff.get())
                                         .when(LootItemRandomChanceCondition.randomChance(0.02f)))
                            .build())
                    .itemModel(ItemModelProvider::handheldItem)
                    .itemTag(SummonerItemTagsRegister.MinionWeapon)
                    .build();
    /**
     * 致命球法杖 - 召唤致命球仆从
     */
    public static final DeferredItem<SummonerWeaponItem<DeadlySphere>> DeadlySphereStaff =
            REGISTRIES.build("deadly_sphere_staff", ResourceLocation -> new MinionWeaponItemBuilder<>(ResourceLocation, SummonerAttachmentEntityRegister.DEADLY_SPHERE)
                            .damage(5.5f)
                            .knockback(0.2f)
                            .sound(SummonerSoundRegister.UseMinionWeapon)
                            .properties(properties -> properties.rarity(Rarity.RARE).component(LyraDataComponentRegister.RARITY, new LyraRarity(0x04F6FF)))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionMinionWeapon)
                    .itemLanguage("Deadly Sphere Staff", "致命球法杖")
                    .summonLanguage(SummonerAttachmentEntityRegister.DEADLY_SPHERE, "Deadly Sphere", "致命球")
                    .lootTable(ResourceLocation.withDefaultNamespace("chests/jungle_temple"), LootTable -> LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1f))
                            .add(LootItem.lootTableItem(SummonerWeaponRegister.DeadlySphereStaff.get())
                                         .when(LootItemRandomChanceCondition.randomChance(0.05f)))
                            .build())
                    .itemModel(ItemModelProvider::handheldItem)
                    .itemTag(SummonerItemTagsRegister.MinionWeapon)
                    .build();
    /**
     * 外星法杖
     */
    public static final DeferredItem<SummonerWeaponItem<UFO>> XenoStaff =
            REGISTRIES.build("xeno_staff", ResourceLocation -> new MinionWeaponItemBuilder<>(ResourceLocation, SummonerAttachmentEntityRegister.UFO)
                            .damage(3.6f)
                            .knockback(0.2f)
                            .sound(SummonerSoundRegister.UseMinionWeapon)
                            .properties(properties -> properties.rarity(Rarity.EPIC).component(LyraDataComponentRegister.RARITY, new LyraRarity(0x1079FF)))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionMinionWeapon)
                    .itemLanguage("Xeno Staff", "外星法杖")
                    .summonLanguage(SummonerAttachmentEntityRegister.UFO, "UFO", "UFO")
                    .lootTable(ResourceLocation.withDefaultNamespace("chests/end_city_treasure"), LootTable -> LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1f))
                            .add(LootItem.lootTableItem(SummonerWeaponRegister.XenoStaff.get())
                                         .when(LootItemRandomChanceCondition.randomChance(0.05f)))
                            .build())
                    .itemModel(ItemModelProvider::handheldItem)
                    .itemTag(SummonerItemTagsRegister.MinionWeapon)
                    .build();
    /**
     * 脉冲炮塔遥控装置
     */
    public static final DeferredItem<SummonerWeaponItem<PulseTurret>> PulseTurretRemote =
            REGISTRIES.build("pulse_turret_remote", ResourceLocation -> new MinionWeaponItemBuilder<>(ResourceLocation, SummonerAttachmentEntityRegister.PULSE_TURRET)
                            .damage(2.5f)
                            .slotType(MinionSlotType.Sentry)
                            .sound(SummonerSoundRegister.UseBallistaStaff)
                            .summon((weapon, player, itemStack) -> {
                                PulseTurret minion = weapon.createMinion(player, itemStack);
                                LyraHelper summonerHelper = LyraHelper.get(player);
                                MinionSlotType slotType = weapon.getSlotType(itemStack);
                                AttachmentEntityData entityData = summonerHelper.getEntityData();
                                List<PulseTurret> pulseTurrets = entityData.get(weapon.getEntityType());
                                if (pulseTurrets.isEmpty() && summonerHelper.canSummon(slotType, minion.getSlotCost())) {
                                    minion.init(new PathNode(player.position().add(0, 1, 0), 0, 0, 0));
                                    summonerHelper.add(minion);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC).component(LyraDataComponentRegister.RARITY, new LyraRarity(0xB16500)))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionMinionWeapon)
                    .itemLanguage("Pulse Turret Remote", "脉冲炮塔遥控装置")
                    .summonLanguage(SummonerAttachmentEntityRegister.PULSE_TURRET, "Pulse Turret", "脉冲炮塔")
                    .itemLanguageTooltip(1, "At most one pulse turret can exist at a time", "最多召唤一个脉冲炮塔")
                    .lootTable(ResourceLocation.withDefaultNamespace("chests/abandoned_mineshaft"), LootTable -> LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1f))
                            .add(LootItem.lootTableItem(SummonerWeaponRegister.PulseTurretRemote.get())
                                         .when(LootItemRandomChanceCondition.randomChance(0.05f)))
                            .build())
                    .itemModel(ItemModelProvider::handheldItem)
                    .itemTag(SummonerItemTagsRegister.MinionWeapon)
                    .build();
    /**
     * 暴风雨法杖 - 召唤鲨鱼龙卷
     */
    public static final DeferredItem<SummonerWeaponItem<Sharknado>> TempestStaff =
            REGISTRIES.build("tempest_staff", ResourceLocation -> new MinionWeaponItemBuilder<>(ResourceLocation, SummonerAttachmentEntityRegister.SHARKNADO)
                            .damage(5f)
                            .knockback(0.2f)
                            .sound(SummonerSoundRegister.UseMinionWeapon)
                            .properties(properties -> properties.rarity(Rarity.RARE).component(LyraDataComponentRegister.RARITY, new LyraRarity(0xDC30FF)))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionMinionWeapon)
                    .itemLanguage("Tempest Staff", "暴风雨法杖")
                    .jeiInfo(1, "Occasionally obtained from fishing in the ocean during rain", "在海洋中雨天钓鱼时偶尔获得")
                    .summonLanguage(SummonerAttachmentEntityRegister.SHARKNADO, "Sharknado", "鲨鱼龙卷")
                    .itemModel(ItemModelProvider::handheldItem)
                    .itemTag(SummonerItemTagsRegister.MinionWeapon)
                    .build();
    /**
     * 泰拉棱镜 - 召唤泰拉棱镜仆从
     */
    public static final DeferredItem<SummonerWeaponItem<Terraprism>> TerraPrism =
            REGISTRIES.build("terraprism", ResourceLocation -> new MinionWeaponItemBuilder<>(ResourceLocation, SummonerAttachmentEntityRegister.TERRA_PRISM)
                            .damage(9f)
                            .knockback(0.4f)
                            .sound(SummonerSoundRegister.UseTerraprism)
                            .summon((weapon, player, itemStack) -> {
                                Terraprism minion = weapon.createMinion(player, itemStack);
                                LyraHelper summonerHelper = LyraHelper.get(player);
                                MinionSlotType slotType = weapon.getSlotType(itemStack);
                                if (summonerHelper.canSummon(slotType, minion.getSlotCost())) {
                                    minion.init(minion.getInterpolatedIdleState(1));
                                    summonerHelper.add(minion);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC).component(LyraDataComponentRegister.RARITY, new LyraRarity(0x75A7D1)))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionMinionWeapon)
                    .itemLanguage("Terraprism", "泰拉棱镜")
                    .summonLanguage(SummonerAttachmentEntityRegister.TERRA_PRISM, "Terraprism", "泰拉棱镜")
                    .itemLanguageTooltip(1, "A flawless blade once hailed as the Prism of the Earth", "曾被冠以大地棱彩美名的无暇之剑")
                    .jeiInfo(1, "Occasionally drops from Allays", "悦灵偶尔掉落")
                    .lootTable(ResourceLocation.withDefaultNamespace("entities/allay"), LootTable -> LootPool.lootPool()
                            .add(LootItem.lootTableItem(SummonerWeaponRegister.TerraPrism)
                                         .when(LootItemRandomChanceCondition.randomChance(0.01f))
                            )
                            .build())
                    .itemModel(ItemModelProvider::handheldItem)
                    .itemTag(SummonerItemTagsRegister.MinionWeapon)
                    .build();
    /**
     * 弩车魔杖
     */
    public static final DeferredItem<SummonerWeaponItem<Ballista>> BallistaRod =
            REGISTRIES.build("ballista_rod", ResourceLocation -> new MinionWeaponItemBuilder<>(ResourceLocation, SummonerAttachmentEntityRegister.BALLISTA)
                            .damage(3f)
                            .knockback(0.47f)
                            .slotType(MinionSlotType.Sentry)
                            .sound(SummonerSoundRegister.UseBallistaStaff)
                            .summon((weapon, player, itemStack) -> {
                                Ballista minion = weapon.createMinion(player, itemStack);
                                LyraHelper summonerHelper = LyraHelper.get(player);
                                MinionSlotType slotType = weapon.getSlotType(itemStack);
                                if (summonerHelper.canSummon(slotType, minion.getSlotCost())) {
                                    minion.setSentryLevel(1);
                                    minion.init(new PathNode(player.position().add(0, 1, 0), 0, 1, 0));
                                    summonerHelper.add(minion);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC).component(LyraDataComponentRegister.RARITY, new LyraRarity(0xD25259)))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionMinionWeapon)
                    .itemLanguage("Ballista Rod", "弩车魔杖")
                    .itemLanguageTooltip(1, "A slow but high damage tower that shoots piercing bolts", "速度缓慢但伤害力极高的防御塔，可以射出穿透性箭矢")
                    .lootTable(ResourceLocation.withDefaultNamespace("chests/pillager_outpost"), LootTable -> LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1f))
                            .add(LootItem.lootTableItem(SummonerWeaponRegister.BallistaRod.get())
                                         .when(LootItemRandomChanceCondition.randomChance(0.05f)))
                            .build())
                    .itemModel(ItemModelProvider::handheldItem)
                    .itemTag(SummonerItemTagsRegister.MinionWeapon)
                    .build();
    /**
     * 弩车手杖
     */
    public static final DeferredItem<SummonerWeaponItem<Ballista>> BallistaCane =
            REGISTRIES.build("ballista_cane", ResourceLocation -> new MinionWeaponItemBuilder<>(ResourceLocation, SummonerAttachmentEntityRegister.BALLISTA)
                            .damage(7.4f)
                            .knockback(0.47f)
                            .slotType(MinionSlotType.Sentry)
                            .sound(SummonerSoundRegister.UseBallistaStaff)
                            .summon((weapon, player, itemStack) -> {
                                Ballista minion = weapon.createMinion(player, itemStack);
                                LyraHelper summonerHelper = LyraHelper.get(player);
                                MinionSlotType slotType = weapon.getSlotType(itemStack);
                                if (summonerHelper.canSummon(slotType, minion.getSlotCost())) {
                                    minion.setSentryLevel(2);
                                    minion.init(new PathNode(player.position().add(0, 1, 0), 0, 0, 0));
                                    summonerHelper.add(minion);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC).component(LyraDataComponentRegister.RARITY, new LyraRarity(0xED5C5C)))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionMinionWeapon)
                    .itemLanguage("Ballista Cane", "弩车手杖")
                    .itemLanguageTooltip(1, "A slow but high damage tower that shoots piercing bolts", "速度缓慢但伤害力极高的防御塔，可以射出穿透性箭矢")
                    .lootTable(ResourceLocation.withDefaultNamespace("chests/pillager_outpost"), LootTable -> LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1f))
                            .add(LootItem.lootTableItem(SummonerWeaponRegister.BallistaCane.get())
                                         .when(LootItemRandomChanceCondition.randomChance(0.05f)))
                            .build())
                    .itemModel(ItemModelProvider::handheldItem)
                    .itemTag(SummonerItemTagsRegister.MinionWeapon)
                    .build();
    /**
     * 弩车法杖
     */
    public static final DeferredItem<SummonerWeaponItem<Ballista>> BallistaStaff =
            REGISTRIES.build("ballista_staff", ResourceLocation -> new MinionWeaponItemBuilder<>(ResourceLocation, SummonerAttachmentEntityRegister.BALLISTA)
                            .damage(15.6f)
                            .knockback(0.47f)
                            .slotType(MinionSlotType.Sentry)
                            .sound(SummonerSoundRegister.UseBallistaStaff)
                            .summon((weapon, player, itemStack) -> {
                                Ballista minion = weapon.createMinion(player, itemStack);
                                LyraHelper summonerHelper = LyraHelper.get(player);
                                MinionSlotType slotType = weapon.getSlotType(itemStack);
                                if (summonerHelper.canSummon(slotType, minion.getSlotCost())) {
                                    minion.setSentryLevel(3);
                                    minion.init(new PathNode(player.position().add(0, 1, 0), 0, 1, 0));
                                    summonerHelper.add(minion);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC).component(LyraDataComponentRegister.RARITY, new LyraRarity(0xF3485E)))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionMinionWeapon)
                    .itemLanguage("Ballista Staff", "弩车法杖")
                    .summonLanguage(SummonerAttachmentEntityRegister.BALLISTA, "Ballista", "弩车")
                    .itemLanguageTooltip(1, "A slow but high damage tower that shoots piercing bolts", "速度缓慢但伤害力极高的防御塔，可以射出穿透性箭矢")
                    .lootTable(ResourceLocation.withDefaultNamespace("chests/pillager_outpost"), LootTable -> LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1f))
                            .add(LootItem.lootTableItem(SummonerWeaponRegister.BallistaStaff.get())
                                         .when(LootItemRandomChanceCondition.randomChance(0.05f)))
                            .build())
                    .itemModel(ItemModelProvider::handheldItem)
                    .itemTag(SummonerItemTagsRegister.MinionWeapon)
                    .build();
    /**
     * 闪电光环魔杖
     */
    public static final DeferredItem<SummonerWeaponItem<LightningAura>> LightningAuraRod =
            REGISTRIES.build("lightning_aura_rod", ResourceLocation -> new MinionWeaponItemBuilder<>(ResourceLocation, SummonerAttachmentEntityRegister.LIGHTNING_AURA)
                            .damage(0.4f)
                            .knockback(0.25f)
                            .armorPierce(1000f)
                            .slotType(MinionSlotType.Sentry)
                            .sound(SummonerSoundRegister.UseBallistaStaff)
                            .summon((weapon, player, itemStack) -> {
                                LightningAura minion = weapon.createMinion(player, itemStack);
                                LyraHelper summonerHelper = LyraHelper.get(player);
                                MinionSlotType slotType = weapon.getSlotType(itemStack);
                                if (summonerHelper.canSummon(slotType, minion.getSlotCost())) {
                                    minion.setSentryLevel(1);
                                    minion.init(new PathNode(player.position().add(0, 1, 0), 0, 1, 0));
                                    summonerHelper.add(minion);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC).component(LyraDataComponentRegister.RARITY, new LyraRarity(0xCC5649)))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionMinionWeapon)
                    .itemLanguage("Lightning Aura Rod", "闪电光环魔杖")
                    .lootTable(ResourceLocation.withDefaultNamespace("chests/village/village_weaponsmith"), LootTable -> LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1f))
                            .add(LootItem.lootTableItem(SummonerWeaponRegister.LightningAuraRod.get())
                                         .when(LootItemRandomChanceCondition.randomChance(0.05f)))
                            .build())
                    .itemModel(ItemModelProvider::handheldItem)
                    .itemTag(SummonerItemTagsRegister.MinionWeapon)
                    .build();
    /**
     * 闪电光环手杖
     */
    public static final DeferredItem<SummonerWeaponItem<LightningAura>> LightningAuraCane =
            REGISTRIES.build("lightning_aura_cane", ResourceLocation -> new MinionWeaponItemBuilder<>(ResourceLocation, SummonerAttachmentEntityRegister.LIGHTNING_AURA)
                            .damage(1.1f)
                            .knockback(0.25f)
                            .armorPierce(1000f)
                            .slotType(MinionSlotType.Sentry)
                            .sound(SummonerSoundRegister.UseBallistaStaff)
                            .summon((weapon, player, itemStack) -> {
                                LightningAura minion = weapon.createMinion(player, itemStack);
                                LyraHelper summonerHelper = LyraHelper.get(player);
                                MinionSlotType slotType = weapon.getSlotType(itemStack);
                                if (summonerHelper.canSummon(slotType, minion.getSlotCost())) {
                                    minion.setSentryLevel(2);
                                    minion.init(new PathNode(player.position().add(0, 1, 0), 0, 0, 0));
                                    summonerHelper.add(minion);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC).component(LyraDataComponentRegister.RARITY, new LyraRarity(0xCF5549)))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionMinionWeapon)
                    .itemLanguage("Lightning Aura Cane", "闪电光环手杖")
                    .lootTable(ResourceLocation.withDefaultNamespace("chests/village/village_weaponsmith"), LootTable -> LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1f))
                            .add(LootItem.lootTableItem(SummonerWeaponRegister.LightningAuraCane.get())
                                         .when(LootItemRandomChanceCondition.randomChance(0.05f)))
                            .build())
                    .itemModel(ItemModelProvider::handheldItem)
                    .itemTag(SummonerItemTagsRegister.MinionWeapon)
                    .build();
    /**
     * 闪电光环法杖
     */
    public static final DeferredItem<SummonerWeaponItem<LightningAura>> LightningAuraStaff =
            REGISTRIES.build("lightning_aura_staff", ResourceLocation -> new MinionWeaponItemBuilder<>(ResourceLocation, SummonerAttachmentEntityRegister.LIGHTNING_AURA)
                            .damage(3.4f)
                            .knockback(0.25f)
                            .armorPierce(1000f)
                            .slotType(MinionSlotType.Sentry)
                            .sound(SummonerSoundRegister.UseBallistaStaff)
                            .summon((weapon, player, itemStack) -> {
                                LightningAura minion = weapon.createMinion(player, itemStack);
                                LyraHelper summonerHelper = LyraHelper.get(player);
                                MinionSlotType slotType = weapon.getSlotType(itemStack);
                                if (summonerHelper.canSummon(slotType, minion.getSlotCost())) {
                                    minion.setSentryLevel(3);
                                    minion.init(new PathNode(player.position().add(0, 1, 0), 0, 1, 0));
                                    summonerHelper.add(minion);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC).component(LyraDataComponentRegister.RARITY, new LyraRarity(0xD75346)))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionMinionWeapon)
                    .itemLanguage("Lightning Aura Staff", "闪电光环法杖")
                    .summonLanguage(SummonerAttachmentEntityRegister.LIGHTNING_AURA, "Lightning Aura", "闪电光环")
                    .lootTable(ResourceLocation.withDefaultNamespace("chests/village/village_weaponsmith"), LootTable -> LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1f))
                            .add(LootItem.lootTableItem(SummonerWeaponRegister.LightningAuraStaff.get())
                                         .when(LootItemRandomChanceCondition.randomChance(0.05f)))
                            .build())
                    .itemModel(ItemModelProvider::handheldItem)
                    .itemTag(SummonerItemTagsRegister.MinionWeapon)
                    .build();
    /**
     * 缥缈星核法杖 - 召唤缥缈星核仆从
     */
    public static final DeferredItem<SummonerWeaponItem<EtherealStellarCore>> EtherealStellarCoreStaff =
            REGISTRIES.build("ethereal_stellar_core_staff", ResourceLocation -> new MinionWeaponItemBuilder<>(ResourceLocation, SummonerAttachmentEntityRegister.ETHEREAL_STELLAR_CORE)
                            .damage(5f)
                            .knockback(0.2f)
                            .sound(SummonerSoundRegister.UseMinionWeapon)
                            .summon((weapon, player, itemStack) -> {
                                EtherealStellarCore minion = weapon.createMinion(player, itemStack);
                                LyraHelper summonerHelper = LyraHelper.get(player);
                                MinionSlotType slotType = weapon.getSlotType(itemStack);
                                AttachmentEntityData entityData = summonerHelper.getEntityData();
                                List<EtherealStellarCore> etherealStellarCores = entityData.get(weapon.getEntityType());
                                if (etherealStellarCores.size() < 9 && summonerHelper.canSummon(slotType, minion.getSlotCost())) {
                                    minion.init(minion.getInterpolatedIdleState(1.0f));
                                    summonerHelper.add(minion);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC).component(LyraDataComponentRegister.RARITY, new LyraRarity(0xFCB200)))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionMinionWeapon)
                    .itemLanguage("Ethereal Stellar Core Staff", "缥缈星核法杖")
                    .summonLanguage(SummonerAttachmentEntityRegister.ETHEREAL_STELLAR_CORE, "Ethereal Stellar Core", "缥缈星核")
                    .itemLanguageTooltip(1, "Summons up to 9 Ethereal Stellar Cores", "最多召唤9个缥缈星核")
                    .lootTable(ResourceLocation.withDefaultNamespace("chests/end_city_treasure"), LootTable -> LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1f))
                            .add(LootItem.lootTableItem(SummonerWeaponRegister.EtherealStellarCoreStaff.get())
                                         .when(LootItemRandomChanceCondition.randomChance(0.02f)))
                            .build())
                    .itemModel(ItemModelProvider::handheldItem)
                    .itemTag(SummonerItemTagsRegister.MinionWeapon)
                    .build();
    /**
     * 星尘细胞杖 - 召唤星尘细胞仆从
     */
    public static final DeferredItem<SummonerWeaponItem<StardustCell>> StardustCellStaff =
            REGISTRIES.build("stardust_cell_staff", ResourceLocation -> new MinionWeaponItemBuilder<>(ResourceLocation, SummonerAttachmentEntityRegister.STARDUST_CELL)
                            .damage(6f)
                            .knockback(0.2f)
                            .sound(SummonerSoundRegister.UseMinionWeapon)
                            .properties(properties -> properties.rarity(Rarity.EPIC).component(LyraDataComponentRegister.RARITY, new LyraRarity(0x077AFF)))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionMinionWeapon)
                    .itemLanguage("Stardust Cell Staff", "星尘细胞法杖")
                    .summonLanguage(SummonerAttachmentEntityRegister.STARDUST_CELL, "Stardust Cell", "星尘细胞")
                    .itemLanguageTooltip(1, "Cultivate the most beautiful cellular infection", "培养最美丽的细胞感染")
                    .lootTable(ResourceLocation.withDefaultNamespace("chests/end_city_treasure"), LootTable -> LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1f))
                            .add(LootItem.lootTableItem(SummonerWeaponRegister.StardustCellStaff.get())
                                         .when(LootItemRandomChanceCondition.randomChance(0.02f)))
                            .build())
                    .itemModel(ItemModelProvider::handheldItem)
                    .itemTag(SummonerItemTagsRegister.MinionWeapon)
                    .build();
    /**
     * 星尘龙杖 - 召唤星尘龙（多体节仆从）
     */
    public static final DeferredItem<SummonerWeaponItem<StardustDragon>> StardustDragonStaff =
            REGISTRIES.build("stardust_dragon_staff", ResourceLocation -> new MinionWeaponItemBuilder<>(ResourceLocation, SummonerAttachmentEntityRegister.STARDUST_DRAGON)
                            .damage(4f)
                            .knockback(0.2f)
                            .sound(SummonerSoundRegister.UseMinionWeapon)
                            .summon((weapon, player, itemStack) -> {
                                LyraHelper helper = LyraHelper.get(player);
                                MinionSlotType slotType = weapon.getSlotType(itemStack);
                                if (helper.canSummon(slotType, 1)) {
                                    List<StardustDragon> existing = helper.getEntityData().get(weapon.getEntityType());
                                    if (existing.isEmpty()) {
                                        for (int i = 0; i < 3; i++) {
                                            StardustDragon minion = weapon.createMinion(player, itemStack);
                                            if (i < 2) {
                                                minion.setSlotCost(0);
                                            }
                                            minion.init(new PathNode(player.position().add(0, 3, -i * minion.getSegmentDistance()), 0, 0, 0));
                                            helper.add(minion);
                                        }
                                    } else {
                                        StardustDragon minion = weapon.createMinion(player, itemStack);
                                        StardustDragon last = existing.getLast();
                                        PathNode pathNode = last.getCurrentPathNode();
                                        Vec3 pos = pathNode.pos().add(last.getLookAngle().scale(-minion.getSegmentDistance()));
                                        minion.init(new PathNode(pos, pathNode.yaw(), pathNode.pitch(), pathNode.roll()));
                                        helper.add(minion);
                                    }
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC).component(LyraDataComponentRegister.RARITY, new LyraRarity(0xFFB400)))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionMinionWeapon)
                    .itemLanguage("Stardust Dragon Staff", "星尘之龙法杖")
                    .summonLanguage(SummonerAttachmentEntityRegister.STARDUST_DRAGON, "Stardust Dragon", "星尘之龙")
                    .itemLanguageTooltip(1, "When you have a dragon, who needs a swarm?", "有了一条巨龙后，谁还需要一群仆从呢？")
                    .lootTable(ResourceLocation.withDefaultNamespace("chests/end_city_treasure"), LootTable -> LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1f))
                            .add(LootItem.lootTableItem(SummonerWeaponRegister.StardustDragonStaff.get())
                                         .when(LootItemRandomChanceCondition.randomChance(0.02f)))
                            .build())
                    .itemModel(ItemModelProvider::handheldItem)
                    .itemTag(SummonerItemTagsRegister.MinionWeapon)
                    .build();
    /**
     * 月亮传送门法杖
     */
    public static final DeferredItem<SummonerWeaponItem<MoonPortal>> MoonPortalStaff =
            REGISTRIES.build("moon_portal_staff", ResourceLocation -> new MinionWeaponItemBuilder<>(ResourceLocation, SummonerAttachmentEntityRegister.MOON_PORTAL)
                            .damage(10f)
                            .knockback(0.75f)
                            .slotType(MinionSlotType.Sentry)
                            .sound(SummonerSoundRegister.UseMoonPortalStaff)
                            .summon((weapon, player, itemStack) -> {
                                MoonPortal minion = weapon.createMinion(player, itemStack);
                                LyraHelper summonerHelper = LyraHelper.get(player);
                                MinionSlotType slotType = weapon.getSlotType(itemStack);
                                if (summonerHelper.canSummon(slotType, minion.getSlotCost())) {
                                    minion.init(new PathNode(player.position().add(0, 2, 0), 0, 0, 0));
                                    summonerHelper.add(minion);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC).component(LyraDataComponentRegister.RARITY, new LyraRarity(0x22DD97)))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionMinionWeapon)
                    .itemLanguage("Moon Portal Staff", "月亮传送门法杖")
                    .summonLanguage(SummonerAttachmentEntityRegister.MOON_PORTAL, "Moon Portal", "月亮传送门")
                    .lootTable(ResourceLocation.withDefaultNamespace("entities/ender_dragon"), LootTable -> LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1f))
                            .add(LootItem.lootTableItem(SummonerWeaponRegister.MoonPortalStaff.get())
                                         .when(LootItemRandomChanceCondition.randomChance(0.1f)))
                            .build())
                    .itemModel(ItemModelProvider::handheldItem)
                    .itemTag(SummonerItemTagsRegister.MinionWeapon)
                    .build();
    /**
     * 七彩水晶法杖
     */
    public static final DeferredItem<SummonerWeaponItem<RainbowCrystal>> RainbowCrystalStaff =
            REGISTRIES.build("rainbow_crystal_staff", ResourceLocation -> new MinionWeaponItemBuilder<>(ResourceLocation, SummonerAttachmentEntityRegister.RAINBOW_CRYSTAL)
                            .damage(13f)
                            .knockback(0.75f)
                            .slotType(MinionSlotType.Sentry)
                            .sound(SummonerSoundRegister.UseMoonPortalStaff)
                            .summon((weapon, player, itemStack) -> {
                                RainbowCrystal minion = weapon.createMinion(player, itemStack);
                                LyraHelper summonerHelper = LyraHelper.get(player);
                                MinionSlotType slotType = weapon.getSlotType(itemStack);
                                if (summonerHelper.canSummon(slotType, minion.getSlotCost())) {
                                    minion.init(new PathNode(player.position().add(0, 2, 0), 0, 0, 0));
                                    summonerHelper.add(minion);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC).component(LyraDataComponentRegister.RARITY, new LyraRarity(0x994DFF)))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionMinionWeapon)
                    .itemLanguage("Rainbow Crystal Staff", "七彩水晶法杖")
                    .summonLanguage(SummonerAttachmentEntityRegister.RAINBOW_CRYSTAL, "Rainbow Crystal", "七彩水晶")
                    .itemLanguageTooltip(1, "'The colors, Duke, the colors!'", "“公爵，多么缤纷的颜色！”")
                    .lootTable(ResourceLocation.withDefaultNamespace("entities/ender_dragon"), LootTable -> LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1f))
                            .add(LootItem.lootTableItem(SummonerWeaponRegister.RainbowCrystalStaff.get())
                                         .when(LootItemRandomChanceCondition.randomChance(0.1f)))
                            .build())
                    .itemModel(ItemModelProvider::handheldItem)
                    .itemTag(SummonerItemTagsRegister.MinionWeapon)
                    .build();
    /**
     * 无限剑鞘
     */
    public static final DeferredItem<SummonerWeaponItem<InfiniteShadow>> InfiniteScabbard =
            REGISTRIES.build("infinite_scabbard", ResourceLocation -> new MinionWeaponItemBuilder<>(ResourceLocation, SummonerAttachmentEntityRegister.INFINITE_SHADOW)
                            .sound(SummonerSoundRegister.UseTerraprism)
                            .summon((weapon, player, itemStack) -> {
                                InfiniteShadow minion = weapon.createMinion(player, itemStack);
                                LyraHelper summonerHelper = LyraHelper.get(player);
                                MinionSlotType slotType = weapon.getSlotType(itemStack);
                                ScabbardContainer container = player.getMainHandItem().getComponents().getOrDefault(SummonerDataComponentRegister.SCABBARD.get(), ScabbardContainer.EMPTY);
                                if (summonerHelper.canSummon(slotType, minion.getSlotCost()) && !container.isEmpty()) {
                                    minion.setItemStack(container.itemStack());
                                    minion.init(minion.getInterpolatedIdleState(1));
                                    summonerHelper.add(minion);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC)
                                    .component(SummonerDataComponentRegister.SCABBARD, ScabbardContainer.EMPTY)
                                    .component(LyraDataComponentRegister.RARITY, new LyraRarity(0xA66053)))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionMinionWeapon)
                    .itemLanguage("Infinite Scabbard", "无限剑鞘")
                    .summonLanguage(SummonerAttachmentEntityRegister.INFINITE_SHADOW, "Infinite Shadow", "无限之影")
                    .itemLanguageTooltip(1, "A scabbard that stores a blade of infinite potential", "蕴含无限可能之'剑'的剑鞘")
                    .itemLanguageTooltip(2, "Right-click an item to store, right-click an empty slot to retrieve", "右键物品存入，右键空格子取出")
                    .lootTable(ResourceLocation.withDefaultNamespace("chests/ancient_city"), LootTable -> LootPool.lootPool()
                            .add(LootItem.lootTableItem(SummonerWeaponRegister.InfiniteScabbard)
                                         .when(LootItemRandomChanceCondition.randomChance(0.05f)))
                            .build())
                    .jeiInfo(1, "Occasionally found in Ancient City chests", "偶尔在远古城市宝箱中发现")
                    .itemModel(ItemModelProvider::handheldItem)
                    .itemTag(SummonerItemTagsRegister.MinionWeapon)
                    .build();
    /**
     * 虚空吞噬者傀具
     */
    public static final DeferredItem<SummonerWeaponItem<VoidEater>> VoidEaterMarionette =
            REGISTRIES.build("void_eater_marionette", ResourceLocation -> new MinionWeaponItemBuilder<>(ResourceLocation, SummonerAttachmentEntityRegister.VOID_EATER)
                            .damage(11f)
                            .knockback(0.5f)
                            .sound(SummonerSoundRegister.UseMinionWeapon)
                            .summon((weapon, player, itemStack) -> {
                                LyraHelper helper = LyraHelper.get(player);
                                MinionSlotType slotType = weapon.getSlotType(itemStack);
                                if (helper.canSummon(slotType, 1)) {
                                    List<VoidEater> existing = helper.getEntityData().get(weapon.getEntityType());
                                    if (existing.isEmpty()) {
                                        for (int i = 0; i < 3; i++) {
                                            VoidEater minion = weapon.createMinion(player, itemStack);
                                            if (i < 2) {
                                                minion.setSlotCost(0);
                                            }
                                            minion.init(new PathNode(player.position().add(0, 3, -i * minion.getSegmentDistance()), 0, 0, 0));
                                            helper.add(minion);
                                        }
                                    } else {
                                        VoidEater minion = weapon.createMinion(player, itemStack);
                                        VoidEater last = existing.getLast();
                                        PathNode pathNode = last.getCurrentPathNode();
                                        Vec3 pos = pathNode.pos().add(last.getLookAngle().scale(-minion.getSegmentDistance()));
                                        minion.init(new PathNode(pos, pathNode.yaw(), pathNode.pitch(), pathNode.roll()));
                                        helper.add(minion);
                                    }
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC).component(LyraDataComponentRegister.RARITY, new LyraRarity(0x955CAA)))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionMinionWeapon)
                    .itemLanguage("Void Eater Marionette", "虚空吞噬者傀具")
                    .summonLanguage(SummonerAttachmentEntityRegister.VOID_EATER, "Void Eater", "虚空吞噬者")
                    .itemLanguageTooltip(1, "Contains the power to command miniature devourers", "蕴含掌控小型吞噬者的力量")
                    .itemLanguageTooltip(2, "The devourer uses the God Eater, Holy Incineration and Cosmic Maelstrom to attack", "吞噬者会使用噬神者，焚灭虔信之火和超宇宙狂涡攻击")
                    .itemLanguageTooltip(3, "While the devourer is present, all your attacks unleash God-Slaying Fury", "吞噬者在场时，你的所有攻击都会释放弑神怒焰")
                    .lootTable(ResourceLocation.withDefaultNamespace("chests/end_city_treasure"), LootTable -> LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1f))
                            .add(LootItem.lootTableItem(SummonerWeaponRegister.VoidEaterMarionette.get())
                                         .when(LootItemRandomChanceCondition.randomChance(0.005f)))
                            .build())
                    .itemModel(ItemModelProvider::handheldItem)
                    .itemTag(SummonerItemTagsRegister.MinionWeapon)
                    .build();
    /**
     * 雷云盆栽
     */
    public static final DeferredItem<SummonerWeaponItem<SuperPeashooter>> ThundercloudBonsai =
            REGISTRIES.build("thundercloud_bonsai", ResourceLocation -> new MinionWeaponItemBuilder<>(ResourceLocation, SummonerAttachmentEntityRegister.SUPER_PEASHOOTER)
                            .damage(12f)
                            .slotType(MinionSlotType.Sentry)
                            .sound(SummonerSoundRegister.UseMoonPortalStaff)
                            .summon((weapon, player, itemStack) -> {
                                SuperPeashooter minion = weapon.createMinion(player, itemStack);
                                LyraHelper summonerHelper = LyraHelper.get(player);
                                MinionSlotType slotType = weapon.getSlotType(itemStack);
                                if (summonerHelper.canSummon(slotType, minion.getSlotCost())) {
                                    minion.init(new PathNode(player.position().add(0, 1, 0), 0, 1, 0));
                                    summonerHelper.add(minion);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC).component(LyraDataComponentRegister.RARITY, new LyraRarity(0x2978D3)))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionMinionWeapon)
                    .itemLanguage("Thundercloud Bonsai", "雷云盆栽")
                    .summonLanguage(SummonerAttachmentEntityRegister.SUPER_PEASHOOTER, "Super Peashooter", "超级电能豌豆射手")
                    .itemLanguageTooltip(1, "Fires seven lightning balls at once, with a chance to fire a large number of scattered lightning balls at once", "一次发射七颗雷电球，有概率一次发射大量散射雷电球")
                    .lootTable(ResourceLocation.withDefaultNamespace("entities/creeper"), LootTable -> {
                        CompoundTag tag = new CompoundTag();
                        tag.putBoolean("powered", true);
                        return LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1f))
                                .add(LootItem.lootTableItem(SummonerWeaponRegister.ThundercloudBonsai.get())
                                             .when(LootItemRandomChanceCondition.randomChance(0.01f))
                                             .when(LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().nbt(new NbtPredicate(tag)))))
                                .build();
                    })
                    .itemModel(ItemModelProvider::handheldItem)
                    .itemTag(SummonerItemTagsRegister.MinionWeapon)
                    .build();
    /**
     * 玉米加农炮
     */
    public static final DeferredItem<SummonerWeaponItem<Cannon>> CornCannon =
            REGISTRIES.build("corn_cannon", ResourceLocation -> new MinionWeaponItemBuilder<>(ResourceLocation, SummonerAttachmentEntityRegister.CANNON)
                            .damage(6400f)
                            .armorPierce(3600f)
                            .slotType(MinionSlotType.Sentry)
                            .sound(SummonerSoundRegister.UseBallistaStaff)
                            .summon((weapon, player, itemStack) -> {
                                Cannon minion = weapon.createMinion(player, itemStack);
                                LyraHelper summonerHelper = LyraHelper.get(player);
                                MinionSlotType slotType = weapon.getSlotType(itemStack);
                                minion.setSlotCost(2);
                                if (summonerHelper.canSummon(slotType, minion.getSlotCost())) {
                                    minion.init(new PathNode(player.position().add(0, 1, 0), 0, 0, 0));
                                    summonerHelper.add(minion);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC).component(LyraDataComponentRegister.RARITY, new LyraRarity(0x07BA19)))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionMinionWeapon)
                    .itemLanguage("Corn Cannon", "玉米加农炮")
                    .summonLanguage(SummonerAttachmentEntityRegister.CANNON, "Cannon", "玉米加农炮")
                    .itemLanguageTooltip(1, "Uses 2 Sentry slots", "占用 2 哨兵栏")
                    .itemModel(ItemModelProvider::handheldItem)
                    .itemTag(SummonerItemTagsRegister.MinionWeapon)
                    .build();
    /**
     * 死魂灵巫术单元
     */
    public static final DeferredItem<SummonerWeaponItem<NecroSpirit>> NecroSpiritStaff =
            REGISTRIES.build("necro_spirit_staff", ResourceLocation -> new MinionWeaponItemBuilder<>(ResourceLocation, SummonerAttachmentEntityRegister.NECRO_SPIRIT)
                            .damage(700f)
                            .sound(SummonerSoundRegister.UseMinionWeapon)
                            .summon((weapon, player, itemStack) -> {
                                NecroSpirit minion = weapon.createMinion(player, itemStack);
                                LyraHelper summonerHelper = LyraHelper.get(player);
                                MinionSlotType slotType = weapon.getSlotType(itemStack);
                                AttachmentEntityData entityData = summonerHelper.getEntityData();
                                if (entityData.get(weapon.getEntityType()).isEmpty() && summonerHelper.canSummon(slotType, minion.getSlotCost())) {
                                    minion.init(minion.getInterpolatedIdleState(1.0f));
                                    summonerHelper.add(minion);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC).component(LyraDataComponentRegister.RARITY, new LyraRarity(0xDA5152)))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionMinionWeapon)
                    .itemLanguage("Necro Spirit Staff", "死魂灵巫术单元")
                    .summonLanguage(SummonerAttachmentEntityRegister.NECRO_SPIRIT, "Necro Spirit", "死魂灵")
                    .itemLanguageTooltip(1, "Summons up to 1 Necro Spirit", "最多召唤1个死魂灵")
                    .itemModel(ItemModelProvider::handheldItem)
                    .itemTag(SummonerItemTagsRegister.MinionWeapon)
                    .build();
    /**
     * 激光机枪 - 召唤激光机枪仆从（最多1个）
     */
    public static final DeferredItem<SummonerWeaponItem<LaserMinigun>> LaserMinigunStaff =
            REGISTRIES.build("laser_minigun", ResourceLocation -> new MinionWeaponItemBuilder<>(ResourceLocation, SummonerAttachmentEntityRegister.LASER_MINIGUN)
                            .damage(6f)
                            .knockback(0.2f)
                            .sound(SummonerSoundRegister.UseMinionWeapon)
                            .summon((weapon, player, itemStack) -> {
                                LaserMinigun minion = weapon.createMinion(player, itemStack);
                                LyraHelper summonerHelper = LyraHelper.get(player);
                                MinionSlotType slotType = weapon.getSlotType(itemStack);
                                AttachmentEntityData entityData = summonerHelper.getEntityData();
                                if (entityData.get(weapon.getEntityType()).isEmpty() && summonerHelper.canSummon(slotType, minion.getSlotCost())) {
                                    minion.init(minion.getInterpolatedIdleState(1.0f));
                                    summonerHelper.add(minion);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.RARE).component(LyraDataComponentRegister.RARITY, new LyraRarity(0x0084E6)))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionMinionWeapon)
                    .itemLanguage("Laser Minigun", "激光机枪")
                    .summonLanguage(SummonerAttachmentEntityRegister.LASER_MINIGUN, "Laser Minigun", "激光机枪")
                    .lootTable(ResourceLocation.withDefaultNamespace("entities/warden"), LootTable -> LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1f))
                            .add(LootItem.lootTableItem(SummonerWeaponRegister.LaserMinigunStaff.get())
                                         .when(LootItemRandomChanceCondition.randomChance(0.01f)))
                            .build())
                    .itemModel(ItemModelProvider::handheldItem)
                    .itemTag(SummonerItemTagsRegister.MinionWeapon)
                    .build();
    /**
     * 藤壶法杖
     */
    public static final DeferredItem<SummonerWeaponItem<Barnacle>> BarnacleStaff =
            REGISTRIES.build("barnacle_staff", ResourceLocation -> new MinionWeaponItemBuilder<>(ResourceLocation, SummonerAttachmentEntityRegister.BARNACLE)
                            .damage(3.3f)
                            .knockback(0.75f)
                            .slotType(MinionSlotType.Sentry)
                            .sound(SummonerSoundRegister.UseMinionWeapon)
                            .summon((weapon, player, itemStack) -> {
                                Barnacle minion = weapon.createMinion(player, itemStack);
                                LyraHelper summonerHelper = LyraHelper.get(player);
                                MinionSlotType slotType = weapon.getSlotType(itemStack);
                                if (summonerHelper.canSummon(slotType, minion.getSlotCost())) {
                                    minion.init(new PathNode(player.position().add(0, 1, 0), 0, 0, 0));
                                    summonerHelper.add(minion);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.UNCOMMON).component(LyraDataComponentRegister.RARITY, new LyraRarity(0x367A9C)))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionMinionWeapon)
                    .itemLanguage("Barnacle Staff", "藤壶法杖")
                    .summonLanguage(SummonerAttachmentEntityRegister.BARNACLE, "Barnacle", "藤壶")
                    .lootTable(ResourceLocation.withDefaultNamespace("entities/drowned"), LootTable -> LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1f))
                            .add(LootItem.lootTableItem(SummonerWeaponRegister.BarnacleStaff.get())
                                         .when(LootItemRandomChanceCondition.randomChance(0.01f)))
                            .build())
                    .itemModel(ItemModelProvider::handheldItem)
                    .itemTag(SummonerItemTagsRegister.MinionWeapon)
                    .build();
    /**
     * 眼球激光塔
     */
    public static final DeferredItem<SummonerWeaponItem<EyeLaserTurret>> EyeLaserTurretStaff =
            REGISTRIES.build("eye_laser_turret_staff", ResourceLocation -> new MinionWeaponItemBuilder<>(ResourceLocation, SummonerAttachmentEntityRegister.EYE_LASER_TURRET)
                            .damage(1.2f)
                            .knockback(0.25f)
                            .slotType(MinionSlotType.Sentry)
                            .sound(SummonerSoundRegister.UseMinionWeapon)
                            .summon((weapon, player, itemStack) -> {
                                EyeLaserTurret minion = weapon.createMinion(player, itemStack);
                                LyraHelper summonerHelper = LyraHelper.get(player);
                                MinionSlotType slotType = weapon.getSlotType(itemStack);
                                if (summonerHelper.canSummon(slotType, minion.getSlotCost())) {
                                    minion.init(new PathNode(player.position().add(0, 1, 0), 0, 0, 0));
                                    summonerHelper.add(minion);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.UNCOMMON).component(LyraDataComponentRegister.RARITY, new LyraRarity(0xA26632)))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionMinionWeapon)
                    .itemLanguage("Eye Laser Turret Staff", "眼球激光塔魔杖")
                    .summonLanguage(SummonerAttachmentEntityRegister.EYE_LASER_TURRET, "Eye Laser Turret", "眼球激光塔")
                    .lootTable(ResourceLocation.withDefaultNamespace("entities/elder_guardian"), LootTable -> LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1f))
                            .add(LootItem.lootTableItem(SummonerWeaponRegister.EyeLaserTurretStaff.get())
                                         .when(LootItemRandomChanceCondition.randomChance(0.02f)))
                            .build())
                    .itemModel(ItemModelProvider::handheldItem)
                    .itemTag(SummonerItemTagsRegister.MinionWeapon)
                    .build();
    /**
     * 寒霜九头蛇法杖
     */
    public static final DeferredItem<SummonerWeaponItem<Hydra>> HydraStaff =
            REGISTRIES.build("hydra_staff", ResourceLocation -> new MinionWeaponItemBuilder<>(ResourceLocation, SummonerAttachmentEntityRegister.HYDRA)
                            .damage(10f)
                            .knockback(0.75f)
                            .slotType(MinionSlotType.Sentry)
                            .sound(SummonerSoundRegister.UseMinionWeapon)
                            .summon((weapon, player, itemStack) -> {
                                Hydra minion = weapon.createMinion(player, itemStack);
                                LyraHelper summonerHelper = LyraHelper.get(player);
                                MinionSlotType slotType = weapon.getSlotType(itemStack);
                                if (summonerHelper.canSummon(slotType, minion.getSlotCost())) {
                                    minion.init(new PathNode(player.position().add(0, 1, 0), 0, 0, 0));
                                    summonerHelper.add(minion);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.RARE).component(LyraDataComponentRegister.RARITY, new LyraRarity(0x0F98EA)))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionMinionWeapon)
                    .itemLanguage("Hydra Staff", "寒霜九头蛇法杖")
                    .summonLanguage(SummonerAttachmentEntityRegister.HYDRA, "Hydra", "寒霜九头蛇")
                    .lootTable(ResourceLocation.withDefaultNamespace("entities/stray"), LootTable -> LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1f))
                            .add(LootItem.lootTableItem(SummonerWeaponRegister.HydraStaff.get())
                                         .when(LootItemRandomChanceCondition.randomChance(0.01f)))
                            .build())
                    .itemModel(ItemModelProvider::handheldItem)
                    .itemTag(SummonerItemTagsRegister.MinionWeapon)
                    .build();
    /**
     * 爆炸烈焰魔杖
     */
    public static final DeferredItem<SummonerWeaponItem<Inferno>> InfernoRod =
            REGISTRIES.build("inferno_rod", ResourceLocation -> new MinionWeaponItemBuilder<>(ResourceLocation, SummonerAttachmentEntityRegister.INFERNO)
                            .damage(1.7f)
                            .knockback(0.3f)
                            .slotType(MinionSlotType.Sentry)
                            .sound(SummonerSoundRegister.UseBallistaStaff)
                            .summon((weapon, player, itemStack) -> {
                                Inferno minion = weapon.createMinion(player, itemStack);
                                LyraHelper summonerHelper = LyraHelper.get(player);
                                MinionSlotType slotType = weapon.getSlotType(itemStack);
                                if (summonerHelper.canSummon(slotType, minion.getSlotCost())) {
                                    minion.setSentryLevel(1);
                                    minion.init(new PathNode(player.position().add(0, 1, 0), 0, 1, 0));
                                    summonerHelper.add(minion);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.RARE).component(LyraDataComponentRegister.RARITY, new LyraRarity(0xB94BEB)))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionMinionWeapon)
                    .itemLanguage("Inferno Rod", "爆炸烈焰魔杖")
                    .lootTable(ResourceLocation.withDefaultNamespace("chests/bastion_treasure"), LootTable -> LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1f))
                            .add(LootItem.lootTableItem(SummonerWeaponRegister.InfernoRod.get())
                                         .when(LootItemRandomChanceCondition.randomChance(0.02f)))
                            .build())
                    .itemModel(ItemModelProvider::handheldItem)
                    .itemTag(SummonerItemTagsRegister.MinionWeapon)
                    .build();
    /**
     * 爆炸烈焰手杖
     */
    public static final DeferredItem<SummonerWeaponItem<Inferno>> InfernoCane =
            REGISTRIES.build("inferno_cane", ResourceLocation -> new MinionWeaponItemBuilder<>(ResourceLocation, SummonerAttachmentEntityRegister.INFERNO)
                            .damage(4.2f)
                            .knockback(0.3f)
                            .slotType(MinionSlotType.Sentry)
                            .sound(SummonerSoundRegister.UseBallistaStaff)
                            .summon((weapon, player, itemStack) -> {
                                Inferno minion = weapon.createMinion(player, itemStack);
                                LyraHelper summonerHelper = LyraHelper.get(player);
                                MinionSlotType slotType = weapon.getSlotType(itemStack);
                                if (summonerHelper.canSummon(slotType, minion.getSlotCost())) {
                                    minion.setSentryLevel(2);
                                    minion.init(new PathNode(player.position().add(0, 1, 0), 0, 0, 0));
                                    summonerHelper.add(minion);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.RARE).component(LyraDataComponentRegister.RARITY, new LyraRarity(0xB94BEB)))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionMinionWeapon)
                    .itemLanguage("Inferno Cane", "爆炸烈焰手杖")
                    .lootTable(ResourceLocation.withDefaultNamespace("chests/bastion_treasure"), LootTable -> LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1f))
                            .add(LootItem.lootTableItem(SummonerWeaponRegister.InfernoCane.get())
                                         .when(LootItemRandomChanceCondition.randomChance(0.02f)))
                            .build())
                    .itemModel(ItemModelProvider::handheldItem)
                    .itemTag(SummonerItemTagsRegister.MinionWeapon)
                    .build();
    /**
     * 爆炸烈焰法杖
     */
    public static final DeferredItem<SummonerWeaponItem<Inferno>> InfernoStaff =
            REGISTRIES.build("inferno_staff", ResourceLocation -> new MinionWeaponItemBuilder<>(ResourceLocation, SummonerAttachmentEntityRegister.INFERNO)
                            .damage(8.8f)
                            .knockback(0.3f)
                            .slotType(MinionSlotType.Sentry)
                            .sound(SummonerSoundRegister.UseBallistaStaff)
                            .summon((weapon, player, itemStack) -> {
                                Inferno minion = weapon.createMinion(player, itemStack);
                                LyraHelper summonerHelper = LyraHelper.get(player);
                                MinionSlotType slotType = weapon.getSlotType(itemStack);
                                if (summonerHelper.canSummon(slotType, minion.getSlotCost())) {
                                    minion.setSentryLevel(3);
                                    minion.init(new PathNode(player.position().add(0, 1, 0), 0, 1, 0));
                                    summonerHelper.add(minion);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.RARE).component(LyraDataComponentRegister.RARITY, new LyraRarity(0xFFA91A)))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionMinionWeapon)
                    .itemLanguage("Inferno Staff", "爆炸烈焰法杖")
                    .summonLanguage(SummonerAttachmentEntityRegister.INFERNO, "Inferno", "爆炸烈焰")
                    .lootTable(ResourceLocation.withDefaultNamespace("chests/bastion_treasure"), LootTable -> LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1f))
                            .add(LootItem.lootTableItem(SummonerWeaponRegister.InfernoStaff.get())
                                         .when(LootItemRandomChanceCondition.randomChance(0.02f)))
                            .build())
                    .itemModel(ItemModelProvider::handheldItem)
                    .itemTag(SummonerItemTagsRegister.MinionWeapon)
                    .build();
    /**
     * 血红法杖 - 召唤血蝙蝠仆从
     */
    public static final DeferredItem<SummonerWeaponItem<BloodBat>> SanguineStaff =
            REGISTRIES.build("sanguine_staff", ResourceLocation -> new MinionWeaponItemBuilder<>(ResourceLocation, SummonerAttachmentEntityRegister.BLOOD_BAT)
                            .damage(3.5f)
                            .knockback(0.3f)
                            .sound(SummonerSoundRegister.UseMinionWeapon)
                            .summon((weapon, player, itemStack) -> {
                                BloodBat minion = weapon.createMinion(player, itemStack);
                                LyraHelper summonerHelper = LyraHelper.get(player);
                                MinionSlotType slotType = weapon.getSlotType(itemStack);
                                if (summonerHelper.canSummon(slotType, minion.getSlotCost())) {
                                    minion.init(new PathNode(player.getBoundingBox().getCenter().offsetRandom(player.getRandom(), 2), 0, 0, 0));
                                    summonerHelper.add(minion);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.UNCOMMON).component(LyraDataComponentRegister.RARITY, new LyraRarity(0xFF4242)))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionMinionWeapon)
                    .itemLanguage("Sanguine Staff", "血红法杖")
                    .summonLanguage(SummonerAttachmentEntityRegister.BLOOD_BAT, "Blood Bat", "血蝙蝠")
                    .lootTable(ResourceLocation.withDefaultNamespace("entities/bat"), LootTable -> LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1f))
                            .add(LootItem.lootTableItem(SummonerWeaponRegister.SanguineStaff.get())
                                         .when(LootItemRandomChanceCondition.randomChance(0.01f)))
                            .build())
                    .itemModel(ItemModelProvider::handheldItem)
                    .itemTag(SummonerItemTagsRegister.MinionWeapon)
                    .build();
    /**
     * 毁灭法杖 - 召唤禁戒遗迹仆从
     */
    public static final DeferredItem<SummonerWeaponItem<RuinRelic>> RuinStaff =
            REGISTRIES.build("ruin_staff", ResourceLocation -> new MinionWeaponItemBuilder<>(ResourceLocation, SummonerAttachmentEntityRegister.RUIN_RELIC)
                            .damage(4f)
                            .knockback(0.4f)
                            .sound(SummonerSoundRegister.UseMinionWeapon)
                            .summon((weapon, player, itemStack) -> {
                                RuinRelic minion = weapon.createMinion(player, itemStack);
                                LyraHelper summonerHelper = LyraHelper.get(player);
                                MinionSlotType slotType = weapon.getSlotType(itemStack);
                                if (summonerHelper.canSummon(slotType, minion.getSlotCost())) {
                                    minion.init(new PathNode(player.getBoundingBox().getCenter().offsetRandom(player.getRandom(), 2), 0, 0, 0));
                                    summonerHelper.add(minion);
                                }
                            })
                            .properties(properties -> properties.rarity(Rarity.EPIC).component(LyraDataComponentRegister.RARITY, new LyraRarity(0x9D6A0F)))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionMinionWeapon)
                    .itemLanguage("Ruin Staff", "毁灭法杖")
                    .summonLanguage(SummonerAttachmentEntityRegister.RUIN_RELIC, "Ruin Relic", "禁戒遗迹")
                    .lootTable(ResourceLocation.withDefaultNamespace("chests/desert_pyramid"), LootTable -> LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1f))
                            .add(LootItem.lootTableItem(SummonerWeaponRegister.RuinStaff.get())
                                         .when(LootItemRandomChanceCondition.randomChance(0.05f)))
                            .build())
                    .itemModel(ItemModelProvider::handheldItem)
                    .itemTag(SummonerItemTagsRegister.MinionWeapon)
                    .build();

    public static void register() {
    }
}
