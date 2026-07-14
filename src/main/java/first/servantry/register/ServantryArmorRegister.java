package first.servantry.register;

import first.servantry.Servantry;
import first.servantry.api.builder.AttributeArmorItemBuilder;
import first.servantry.client.creativeTab.AnimInfo;
import first.servantry.common.recipe.MithrilAnvilRecipe;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.registries.DeferredItem;

public class ServantryArmorRegister {

    public static final TabGroup ARMOR = new TabGroup(1, Servantry.rl("textures/item/banner/default_banner.png"), new AnimInfo(18, 1, 1));
    /**
     * 小雪怪外套
     */
    public static final DeferredItem<ArmorItem> FlinxFurCoat =
            ServantryItemRegisterBuilder.build(ARMOR, "flinx_fur_coat", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.Flinx, ArmorItem.Type.CHESTPLATE)
                            .modifier(Attributes.ARMOR, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantDamage, 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ServantryItemRegister.Silk, 6)
                            .ingredient(Items.LEATHER, 6)
                            .result(ServantryArmorRegister.FlinxFurCoat)
                            .save(output))
                    .itemLanguage("Flinx Fur Coat", "小雪怪皮毛外套")
                    .itemModel(ServantryItemRegisterBuilder::basicModel)
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ServantryItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 蜜蜂头饰 - +4% 仆从伤害，+1 仆从栏
     */
    public static final DeferredItem<ArmorItem> BeeHeadgear =
            ServantryItemRegisterBuilder.build(ARMOR, "bee_headgear", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.BeeArmorMaterial, ArmorItem.Type.HELMET)
                            .modifier(Attributes.ARMOR, 2, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantDamage, 0.04, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .modifier(ServantryAttributeRegister.ServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ServantryItemRegister.BeeWax, 8)
                            .result(ServantryArmorRegister.BeeHeadgear)
                            .save(output))
                    .itemLanguage("Bee Headgear", "蜜蜂头饰")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ServantryItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 蜜蜂胸甲 - +4% 仆从伤害，+1 仆从栏
     */
    public static final DeferredItem<ArmorItem> BeeChestplate =
            ServantryItemRegisterBuilder.build(ARMOR, "bee_chestplate", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.BeeArmorMaterial, ArmorItem.Type.CHESTPLATE)
                            .modifier(Attributes.ARMOR, 2, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantDamage, 0.04, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .modifier(ServantryAttributeRegister.ServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ServantryItemRegister.BeeWax, 12)
                            .result(ServantryArmorRegister.BeeChestplate)
                            .save(output))
                    .itemLanguage("Bee Breastplate", "蜜蜂胸甲")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ServantryItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 蜜蜂护胫 - +5% 仆从伤害
     */
    public static final DeferredItem<ArmorItem> BeeLeggings =
            ServantryItemRegisterBuilder.build(ARMOR, "bee_leggings", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.BeeArmorMaterial, ArmorItem.Type.LEGGINGS)
                            .modifier(Attributes.ARMOR, 2, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantDamage, 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ServantryItemRegister.BeeWax, 10)
                            .result(ServantryArmorRegister.BeeLeggings)
                            .save(output))
                    .itemLanguage("Bee Leggings", "蜜蜂护胫")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ServantryItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 蜜蜂靴 - 套装奖励部分
     */
    public static final DeferredItem<ArmorItem> BeeBoots =
            ServantryItemRegisterBuilder.build(ARMOR, "bee_boots", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.BeeArmorMaterial, ArmorItem.Type.BOOTS)
                            .modifier(Attributes.ARMOR, 1, AttributeModifier.Operation.ADD_VALUE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ServantryItemRegister.BeeWax, 6)
                            .result(ServantryArmorRegister.BeeBoots)
                            .save(output))
                    .itemLanguage("Bee Boots", "蜜蜂靴")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ServantryItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 黑曜石头盔 - +8% 仆从伤害
     */
    public static final DeferredItem<ArmorItem> ObsidianHelmet =
            ServantryItemRegisterBuilder.build(ARMOR, "obsidian_helmet", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.ObsidianArmorMaterial, ArmorItem.Type.HELMET)
                            .modifier(Attributes.ARMOR, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantDamage, 0.08, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ServantryItemRegister.Silk, 10)
                            .ingredient(Items.OBSIDIAN, 20)
                            .result(ServantryArmorRegister.ObsidianHelmet)
                            .save(output))
                    .itemLanguage("Obsidian Helmet", "黑曜石逃犯帽")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ServantryItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 黑曜石胸甲 - +1 仆从栏
     */
    public static final DeferredItem<ArmorItem> ObsidianChestplate =
            ServantryItemRegisterBuilder.build(ARMOR, "obsidian_chestplate", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.ObsidianArmorMaterial, ArmorItem.Type.CHESTPLATE)
                            .modifier(Attributes.ARMOR, 2, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ServantryItemRegister.Silk, 10)
                            .ingredient(Items.OBSIDIAN, 20)
                            .result(ServantryArmorRegister.ObsidianChestplate)
                            .save(output))
                    .itemLanguage("Obsidian Chestplate", "黑曜石风衣")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ServantryItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 黑曜石护腿 - +8% 仆从伤害
     */
    public static final DeferredItem<ArmorItem> ObsidianLeggings =
            ServantryItemRegisterBuilder.build(ARMOR, "obsidian_leggings", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.ObsidianArmorMaterial, ArmorItem.Type.LEGGINGS)
                            .modifier(Attributes.ARMOR, 2, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantDamage, 0.08, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ServantryItemRegister.Silk, 10)
                            .ingredient(Items.OBSIDIAN, 20)
                            .result(ServantryArmorRegister.ObsidianLeggings)
                            .save(output))
                    .itemLanguage("Obsidian Leggings", "黑曜石裤")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ServantryItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 黑曜石靴子 - +8% 移动速度
     */
    public static final DeferredItem<ArmorItem> ObsidianBoots =
            ServantryItemRegisterBuilder.build(ARMOR, "obsidian_boots", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.ObsidianArmorMaterial, ArmorItem.Type.BOOTS)
                            .modifier(Attributes.ARMOR, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(Attributes.MOVEMENT_SPEED, 0.08, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ServantryItemRegister.Silk, 10)
                            .ingredient(Items.OBSIDIAN, 20)
                            .result(ServantryArmorRegister.ObsidianBoots)
                            .save(output))
                    .itemLanguage("Obsidian Boots", "黑曜石靴")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ServantryItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 蜘蛛面具 - +5% 仆从伤害，+1 仆从栏
     */
    public static final DeferredItem<ArmorItem> SpiderMask =
            ServantryItemRegisterBuilder.build(ARMOR, "spider_mask", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.SpiderArmorMaterial, ArmorItem.Type.HELMET)
                            .modifier(Attributes.ARMOR, 2, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantDamage, 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .modifier(ServantryAttributeRegister.ServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ServantryItemRegister.SpiderFang, 8)
                            .result(ServantryArmorRegister.SpiderMask)
                            .save(output))
                    .itemLanguage("Spider Mask", "蜘蛛面具")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ServantryItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 蜘蛛胸甲 - +5% 仆从伤害，+1 仆从栏
     */
    public static final DeferredItem<ArmorItem> SpiderChestplate =
            ServantryItemRegisterBuilder.build(ARMOR, "spider_chestplate", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.SpiderArmorMaterial, ArmorItem.Type.CHESTPLATE)
                            .modifier(Attributes.ARMOR, 3, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantDamage, 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .modifier(ServantryAttributeRegister.ServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ServantryItemRegister.SpiderFang, 16)
                            .result(ServantryArmorRegister.SpiderChestplate)
                            .save(output))
                    .itemLanguage("Spider Breastplate", "蜘蛛胸甲")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ServantryItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 蜘蛛护胫 - +6% 仆从伤害，+1 仆从栏
     */
    public static final DeferredItem<ArmorItem> SpiderLeggings =
            ServantryItemRegisterBuilder.build(ARMOR, "spider_leggings", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.SpiderArmorMaterial, ArmorItem.Type.LEGGINGS)
                            .modifier(Attributes.ARMOR, 3, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantDamage, 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .modifier(ServantryAttributeRegister.ServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ServantryItemRegister.SpiderFang, 12)
                            .result(ServantryArmorRegister.SpiderLeggings)
                            .save(output))
                    .itemLanguage("Spider Greaves", "蜘蛛护胫")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ServantryItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 蜘蛛靴
     */
    public static final DeferredItem<ArmorItem> SpiderBoots =
            ServantryItemRegisterBuilder.build(ARMOR, "spider_boots", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.SpiderArmorMaterial, ArmorItem.Type.BOOTS)
                            .modifier(Attributes.ARMOR, 2, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantDamage, 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ServantryItemRegister.SpiderFang, 8)
                            .result(ServantryArmorRegister.SpiderBoots)
                            .save(output))
                    .itemLanguage("Spider Boots", "蜘蛛靴")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ServantryItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 禁戒面具 - +15% 召唤伤害
     */
    public static final DeferredItem<ArmorItem> ForbiddenMask =
            ServantryItemRegisterBuilder.build(ARMOR, "forbidden_mask", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.ForbiddenArmorMaterial, ArmorItem.Type.HELMET)
                            .modifier(Attributes.ARMOR, 2, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantDamage, 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ServantryItemRegister.AdamantiteIngot, 10)
                            .ingredient(ServantryItemRegister.ForbiddenFragment, 1)
                            .result(ServantryArmorRegister.ForbiddenMask)
                            .save(output))
                    .itemLanguage("Forbidden Mask", "禁戒面具")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ServantryItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 禁戒长袍 - +10%召唤伤害，+1 仆从栏
     */
    public static final DeferredItem<ArmorItem> ForbiddenRobe =
            ServantryItemRegisterBuilder.build(ARMOR, "forbidden_robe", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.ForbiddenArmorMaterial, ArmorItem.Type.CHESTPLATE)
                            .modifier(Attributes.ARMOR, 4, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantDamage, 0.10, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .modifier(ServantryAttributeRegister.ServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ServantryItemRegister.AdamantiteIngot, 20)
                            .ingredient(ServantryItemRegister.ForbiddenFragment, 1)
                            .result(ServantryArmorRegister.ForbiddenRobe)
                            .save(output))
                    .itemLanguage("Forbidden Robe", "禁戒长袍")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ServantryItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 禁戒裤 - +1 仆从栏
     */
    public static final DeferredItem<ArmorItem> ForbiddenLeggings =
            ServantryItemRegisterBuilder.build(ARMOR, "forbidden_leggings", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.ForbiddenArmorMaterial, ArmorItem.Type.LEGGINGS)
                            .modifier(Attributes.ARMOR, 3, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ServantryItemRegister.AdamantiteIngot, 16)
                            .ingredient(ServantryItemRegister.ForbiddenFragment, 1)
                            .result(ServantryArmorRegister.ForbiddenLeggings)
                            .save(output))
                    .itemLanguage("Forbidden Leggings", "禁戒裤")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ServantryItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 禁戒靴
     */
    public static final DeferredItem<ArmorItem> ForbiddenBoots =
            ServantryItemRegisterBuilder.build(ARMOR, "forbidden_boots", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.ForbiddenArmorMaterial, ArmorItem.Type.BOOTS)
                            .modifier(Attributes.ARMOR, 2, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantDamage, 0.10, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ServantryItemRegister.AdamantiteIngot, 8)
                            .ingredient(ServantryItemRegister.ForbiddenFragment, 1)
                            .result(ServantryArmorRegister.ForbiddenBoots)
                            .save(output))
                    .itemLanguage("Forbidden Boots", "禁戒战靴")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ServantryItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 神圣头盔
     */
    public static final DeferredItem<ArmorItem> HallowedHelmet =
            ServantryItemRegisterBuilder.build(ARMOR, "hallowed_helmet", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.HallowedArmorMaterial, ArmorItem.Type.HELMET)
                            .modifier(Attributes.ARMOR, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantDamage, 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ServantryItemRegister.HallowedIngot, 12)
                            .result(ServantryArmorRegister.HallowedHelmet)
                            .save(output))
                    .itemLanguage("Hallowed Helmet", "神圣兜帽")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ServantryItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 神圣胸甲 - +14% 仆从伤害
     */
    public static final DeferredItem<ArmorItem> HallowedChestplate =
            ServantryItemRegisterBuilder.build(ARMOR, "hallowed_chestplate", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.HallowedArmorMaterial, ArmorItem.Type.CHESTPLATE)
                            .modifier(Attributes.ARMOR, 5, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantDamage, 0.14, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ServantryItemRegister.HallowedIngot, 24)
                            .result(ServantryArmorRegister.HallowedChestplate)
                            .save(output))
                    .itemLanguage("Hallowed Chestplate", "神圣板甲")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ServantryItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 神圣护腿 - +7% 仆从伤害
     */
    public static final DeferredItem<ArmorItem> HallowedLeggings =
            ServantryItemRegisterBuilder.build(ARMOR, "hallowed_leggings", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.HallowedArmorMaterial, ArmorItem.Type.LEGGINGS)
                            .modifier(Attributes.ARMOR, 4, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantDamage, 0.07, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ServantryItemRegister.HallowedIngot, 12)
                            .result(ServantryArmorRegister.HallowedLeggings)
                            .save(output))
                    .itemLanguage("Hallowed Leggings", "神圣护胫")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ServantryItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 神圣靴子 - +8% 移动速度
     */
    public static final DeferredItem<ArmorItem> HallowedBoots =
            ServantryItemRegisterBuilder.build(ARMOR, "hallowed_boots", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.HallowedArmorMaterial, ArmorItem.Type.BOOTS)
                            .modifier(Attributes.ARMOR, 3, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(Attributes.MOVEMENT_SPEED, 0.08, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ServantryItemRegister.HallowedIngot, 12)
                            .result(ServantryArmorRegister.HallowedBoots)
                            .save(output))
                    .itemLanguage("Hallowed Boots", "神圣战靴")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ServantryItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 叶绿面具 - +1 召唤栏，+10% 仆从伤害
     */
    public static final DeferredItem<ArmorItem> ChlorophyteHelmet =
            ServantryItemRegisterBuilder.build(ARMOR, "chlorophyte_helmet", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.ChlorophyteArmorMaterial, ArmorItem.Type.HELMET)
                            .modifier(Attributes.ARMOR, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantDamage, 0.16, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ServantryItemRegister.ChlorophyteIngot, 12)
                            .result(ServantryArmorRegister.ChlorophyteHelmet)
                            .save(output))
                    .itemLanguage("Chlorophyte Mask", "叶绿面具")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ServantryItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 叶绿板甲 - +6% 仆从伤害
     */
    public static final DeferredItem<ArmorItem> ChlorophyteChestplate =
            ServantryItemRegisterBuilder.build(ARMOR, "chlorophyte_chestplate", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.ChlorophyteArmorMaterial, ArmorItem.Type.CHESTPLATE)
                            .modifier(Attributes.ARMOR, 6, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantDamage, 0.19, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ServantryItemRegister.ChlorophyteIngot, 24)
                            .result(ServantryArmorRegister.ChlorophyteChestplate)
                            .save(output))
                    .itemLanguage("Chlorophyte Breastplate", "叶绿板甲")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ServantryItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 叶绿护胫 - +4% 仆从伤害
     */
    public static final DeferredItem<ArmorItem> ChlorophyteLeggings =
            ServantryItemRegisterBuilder.build(ARMOR, "chlorophyte_leggings", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.ChlorophyteArmorMaterial, ArmorItem.Type.LEGGINGS)
                            .modifier(Attributes.ARMOR, 5, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantDamage, 0.16, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ServantryItemRegister.ChlorophyteIngot, 12)
                            .result(ServantryArmorRegister.ChlorophyteLeggings)
                            .save(output))
                    .itemLanguage("Chlorophyte Leggings", "叶绿护胫")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ServantryItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 叶绿战靴 - +2% 仆从伤害
     */
    public static final DeferredItem<ArmorItem> ChlorophyteBoots =
            ServantryItemRegisterBuilder.build(ARMOR, "chlorophyte_boots", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.ChlorophyteArmorMaterial, ArmorItem.Type.BOOTS)
                            .modifier(Attributes.ARMOR, 3, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(Attributes.MOVEMENT_SPEED, 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ServantryItemRegister.ChlorophyteIngot, 12)
                            .result(ServantryArmorRegister.ChlorophyteBoots)
                            .save(output))
                    .itemLanguage("Chlorophyte Boots", "叶绿战靴")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ServantryItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 英灵殿骑士头盔 - +2 哨兵栏，+10% 仆从伤害，+10% 原版伤害
     */
    public static final DeferredItem<ArmorItem> ValhallaKnightHelmet =
            ServantryItemRegisterBuilder.build(ARMOR, "valhalla_knight_helmet", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.ValhallaKnightArmorMaterial, ArmorItem.Type.HELMET)
                            .modifier(Attributes.ARMOR, 7, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.SentryServantMaxCount, 2, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantDamage, 0.10, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .modifier(Attributes.ATTACK_DAMAGE, 0.10, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .itemLanguage("Valhalla Knight Helmet", "英灵殿骑士头盔")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ServantryItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 英灵殿骑士胸甲 - +30% 仆从伤害，+0.4 生命再生
     */
    public static final DeferredItem<ArmorItem> ValhallaKnightChestplate =
            ServantryItemRegisterBuilder.build(ARMOR, "valhalla_knight_chestplate", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.ValhallaKnightArmorMaterial, ArmorItem.Type.CHESTPLATE)
                            .modifier(Attributes.ARMOR, 8, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantDamage, 0.30, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .modifier(ServantryAttributeRegister.HealthRegen, 0.4, AttributeModifier.Operation.ADD_VALUE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .itemLanguage("Valhalla Knight Chestplate", "英灵殿骑士胸甲")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ServantryItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 英灵殿骑士护腿 - +20% 仆从伤害，+20% 原版伤害
     */
    public static final DeferredItem<ArmorItem> ValhallaKnightLeggings =
            ServantryItemRegisterBuilder.build(ARMOR, "valhalla_knight_leggings", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.ValhallaKnightArmorMaterial, ArmorItem.Type.LEGGINGS)
                            .modifier(Attributes.ARMOR, 6, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantDamage, 0.20, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .modifier(Attributes.ATTACK_DAMAGE, 0.20, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .itemLanguage("Valhalla Knight Leggings", "英灵殿骑士护腿")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ServantryItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 英灵殿骑士战靴 - +20% 移动速度
     */
    public static final DeferredItem<ArmorItem> ValhallaKnightBoots =
            ServantryItemRegisterBuilder.build(ARMOR, "valhalla_knight_boots", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.ValhallaKnightArmorMaterial, ArmorItem.Type.BOOTS)
                            .modifier(Attributes.ARMOR, 4, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(Attributes.MOVEMENT_SPEED, 0.20, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .itemLanguage("Valhalla Knight Boots", "英灵殿骑士战靴")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ServantryItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 提基面具 - +1 召唤栏，+10% 仆从伤害
     */
    public static final DeferredItem<ArmorItem> TikiHelmet =
            ServantryItemRegisterBuilder.build(ARMOR, "tiki_helmet", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.TikiArmorMaterial, ArmorItem.Type.HELMET)
                            .modifier(Attributes.ARMOR, 2, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantDamage, 0.2, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .itemLanguage("Tiki Mask", "提基面具")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ServantryItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 提基胸甲 - +10% 仆从伤害
     */
    public static final DeferredItem<ArmorItem> TikiChestplate =
            ServantryItemRegisterBuilder.build(ARMOR, "tiki_chestplate", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.TikiArmorMaterial, ArmorItem.Type.CHESTPLATE)
                            .modifier(Attributes.ARMOR, 6, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantDamage, 0.10, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .itemLanguage("Tiki Chestplate", "提基胸甲")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ServantryItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 提基护腿 - +7% 仆从伤害
     */
    public static final DeferredItem<ArmorItem> TikiLeggings =
            ServantryItemRegisterBuilder.build(ARMOR, "tiki_leggings", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.TikiArmorMaterial, ArmorItem.Type.LEGGINGS)
                            .modifier(Attributes.ARMOR, 4, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .itemLanguage("Tiki Leggings", "提基护腿")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ServantryItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 提基战靴 - +3% 仆从伤害
     */
    public static final DeferredItem<ArmorItem> TikiBoots =
            ServantryItemRegisterBuilder.build(ARMOR, "tiki_boots", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.TikiArmorMaterial, ArmorItem.Type.BOOTS)
                            .modifier(Attributes.ARMOR, 3, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantDamage, 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .itemLanguage("Tiki Boots", "提基战靴")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ServantryItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 阴森头盔 - +11% 仆从伤害
     */
    public static final DeferredItem<ArmorItem> SpookyHelmet =
            ServantryItemRegisterBuilder.build(ARMOR, "spooky_helmet", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.SpookyArmorMaterial, ArmorItem.Type.HELMET)
                            .modifier(Attributes.ARMOR, 3, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantDamage, 0.11, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(Items.WARPED_STEM, 200)
                            .ingredient(Items.WARPED_WART_BLOCK, 200)
                            .result(ServantryArmorRegister.SpookyHelmet)
                            .save(output))
                    .itemLanguage("Spooky Helmet", "阴森头盔")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ServantryItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 阴森胸甲 - +11% 仆从伤害
     */
    public static final DeferredItem<ArmorItem> SpookyChestplate =
            ServantryItemRegisterBuilder.build(ARMOR, "spooky_chestplate", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.SpookyArmorMaterial, ArmorItem.Type.CHESTPLATE)
                            .modifier(Attributes.ARMOR, 4, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantDamage, 0.11, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(Items.WARPED_STEM, 300)
                            .ingredient(Items.WARPED_WART_BLOCK, 300)
                            .result(ServantryArmorRegister.SpookyChestplate)
                            .save(output))
                    .itemLanguage("Spooky Chestplate", "阴森胸甲")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ServantryItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 阴森护腿 - +8% 仆从伤害，+1 召唤栏
     */
    public static final DeferredItem<ArmorItem> SpookyLeggings =
            ServantryItemRegisterBuilder.build(ARMOR, "spooky_leggings", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.SpookyArmorMaterial, ArmorItem.Type.LEGGINGS)
                            .modifier(Attributes.ARMOR, 4, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantDamage, 0.11, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(Items.WARPED_STEM, 250)
                            .ingredient(Items.WARPED_WART_BLOCK, 250)
                            .result(ServantryArmorRegister.SpookyLeggings)
                            .save(output))
                    .itemLanguage("Spooky Leggings", "阴森护腿")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ServantryItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 阴森战靴 - +4% 仆从伤害
     */
    public static final DeferredItem<ArmorItem> SpookyBoots =
            ServantryItemRegisterBuilder.build(ARMOR, "spooky_boots", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.SpookyArmorMaterial, ArmorItem.Type.BOOTS)
                            .modifier(Attributes.ARMOR, 2, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(Attributes.MOVEMENT_SPEED, 0.2, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(Items.WARPED_STEM, 150)
                            .ingredient(Items.WARPED_WART_BLOCK, 150)
                            .result(ServantryArmorRegister.SpookyBoots)
                            .save(output))
                    .itemLanguage("Spooky Boots", "阴森战靴")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ServantryItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 星尘头盔 - +1 召唤栏，+16% 仆从伤害
     */
    public static final DeferredItem<ArmorItem> StardustHelmet =
            ServantryItemRegisterBuilder.build(ARMOR, "stardust_helmet", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.StardustArmorMaterial, ArmorItem.Type.HELMET)
                            .modifier(Attributes.ARMOR, 4, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.SentryServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantDamage, 0.22, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .modifier(ServantryAttributeRegister.ServantSearchRange, 0.08, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.EPIC))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ServantryItemRegister.Stardust, 10)
                            .ingredient(ServantryItemRegister.LuminiteIngot, 8)
                            .result(ServantryArmorRegister.StardustHelmet)
                            .save(output))
                    .itemLanguage("MiniStardustCell Helmet", "星尘头盔")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ServantryItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 星尘板甲 - +22% 仆从伤害
     */
    public static final DeferredItem<ArmorItem> StardustChestplate =
            ServantryItemRegisterBuilder.build(ARMOR, "stardust_chestplate", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.StardustArmorMaterial, ArmorItem.Type.CHESTPLATE)
                            .modifier(Attributes.ARMOR, 6, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantMaxCount, 2, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantDamage, 0.22, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .modifier(ServantryAttributeRegister.ServantSearchRange, 0.08, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.EPIC))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ServantryItemRegister.Stardust, 20)
                            .ingredient(ServantryItemRegister.LuminiteIngot, 16)
                            .result(ServantryArmorRegister.StardustChestplate)
                            .save(output))
                    .itemLanguage("MiniStardustCell Chestplate", "星尘板甲")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ServantryItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 星尘护腿 - +15% 仆从伤害
     */
    public static final DeferredItem<ArmorItem> StardustLeggings =
            ServantryItemRegisterBuilder.build(ARMOR, "stardust_leggings", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.StardustArmorMaterial, ArmorItem.Type.LEGGINGS)
                            .modifier(Attributes.ARMOR, 4, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantDamage, 0.22, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .modifier(ServantryAttributeRegister.ServantSearchRange, 0.08, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.EPIC))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ServantryItemRegister.Stardust, 15)
                            .ingredient(ServantryItemRegister.LuminiteIngot, 12)
                            .result(ServantryArmorRegister.StardustLeggings)
                            .save(output))
                    .itemLanguage("MiniStardustCell Leggings", "星尘护腿")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ServantryItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 星尘战靴 - +7% 仆从伤害
     */
    public static final DeferredItem<ArmorItem> StardustBoots =
            ServantryItemRegisterBuilder.build(ARMOR, "stardust_boots", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.StardustArmorMaterial, ArmorItem.Type.BOOTS)
                            .modifier(Attributes.ARMOR, 3, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantSearchRange, 0.08, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.EPIC))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ServantryItemRegister.Stardust, 10)
                            .ingredient(ServantryItemRegister.LuminiteIngot, 8)
                            .result(ServantryArmorRegister.StardustBoots)
                            .save(output))
                    .itemLanguage("MiniStardustCell Boots", "星尘战靴")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ServantryItemRegisterBuilder::basicModel)
                    .build();

    public static void register() {
    }
}
