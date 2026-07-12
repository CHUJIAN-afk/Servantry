package first.servantry.register;

import first.servantry.Servantry;
import first.servantry.api.builder.AttributeArmorItemBuilder;
import first.servantry.client.creativeTab.AnimInfo;
import first.servantry.common.recipe.MithrilAnvilRecipe;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;

public class ServantryArmorRegister {

    private static final ServantryRegisters Register = ServantryRegisters.getInstance();
    public static final TabGroup ARMOR = new TabGroup(1, Servantry.rl("textures/item/banner/default_banner.png"), new AnimInfo(18, 1, 1));
    /**
     * 小雪怪外套
     */
    public static final DeferredItem<Item> FlinxFurCoat =
            Register.register(ARMOR, "flinx_fur_coat", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.Flinx, ArmorItem.Type.CHESTPLATE)
                            .modifier(Attributes.ARMOR, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantDamage, 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ServantryItemRegister.Silk, 6)
                            .ingredient(Items.LEATHER, 6)
                            .result(ServantryArmorRegister.FlinxFurCoat)
                            .save(output))
                    .language("Flinx Fur Coat", "小雪怪皮毛外套")
                    .build();
    /**
     * 蜜蜂头饰 - +4% 仆从伤害，+1 仆从栏
     */
    public static final DeferredItem<Item> BeeHeadgear =
            Register.register(ARMOR, "bee_headgear", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.BeeArmorMaterial, ArmorItem.Type.HELMET)
                            .modifier(Attributes.ARMOR, 2, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantDamage, 0.04, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .modifier(ServantryAttributeRegister.ServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ServantryItemRegister.BeeWax, 8)
                            .result(ServantryArmorRegister.BeeHeadgear)
                            .save(output))
                    .language("Bee Headgear", "蜜蜂头饰")
                    .build();
    /**
     * 蜜蜂胸甲 - +4% 仆从伤害，+1 仆从栏
     */
    public static final DeferredItem<Item> BeeChestplate =
            Register.register(ARMOR, "bee_chestplate", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.BeeArmorMaterial, ArmorItem.Type.CHESTPLATE)
                            .modifier(Attributes.ARMOR, 2, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantDamage, 0.04, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .modifier(ServantryAttributeRegister.ServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ServantryItemRegister.BeeWax, 12)
                            .result(ServantryArmorRegister.BeeChestplate)
                            .save(output))
                    .language("Bee Breastplate", "蜜蜂胸甲")
                    .build();
    /**
     * 蜜蜂护胫 - +5% 仆从伤害
     */
    public static final DeferredItem<Item> BeeLeggings =
            Register.register(ARMOR, "bee_leggings", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.BeeArmorMaterial, ArmorItem.Type.LEGGINGS)
                            .modifier(Attributes.ARMOR, 2, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantDamage, 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ServantryItemRegister.BeeWax, 10)
                            .result(ServantryArmorRegister.BeeLeggings)
                            .save(output))
                    .language("Bee Leggings", "蜜蜂护胫")
                    .build();
    /**
     * 蜜蜂靴 - 套装奖励部分
     */
    public static final DeferredItem<Item> BeeBoots =
            Register.register(ARMOR, "bee_boots", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.BeeArmorMaterial, ArmorItem.Type.BOOTS)
                            .modifier(Attributes.ARMOR, 1, AttributeModifier.Operation.ADD_VALUE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ServantryItemRegister.BeeWax, 6)
                            .result(ServantryArmorRegister.BeeBoots)
                            .save(output))
                    .language("Bee Boots", "蜜蜂靴")
                    .build();
    /**
     * 黑曜石头盔 - +8% 仆从伤害
     */
    public static final DeferredItem<Item> ObsidianHelmet =
            Register.register(ARMOR, "obsidian_helmet", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.ObsidianArmorMaterial, ArmorItem.Type.HELMET)
                            .modifier(Attributes.ARMOR, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantDamage, 0.08, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ServantryItemRegister.Silk, 10)
                            .ingredient(Items.OBSIDIAN, 20)
                            .result(ServantryArmorRegister.ObsidianHelmet)
                            .save(output))
                    .language("Obsidian Helmet", "黑曜石逃犯帽")
                    .build();
    /**
     * 黑曜石胸甲 - +1 仆从栏
     */
    public static final DeferredItem<Item> ObsidianChestplate =
            Register.register(ARMOR, "obsidian_chestplate", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.ObsidianArmorMaterial, ArmorItem.Type.CHESTPLATE)
                            .modifier(Attributes.ARMOR, 2, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ServantryItemRegister.Silk, 10)
                            .ingredient(Items.OBSIDIAN, 20)
                            .result(ServantryArmorRegister.ObsidianChestplate)
                            .save(output))
                    .language("Obsidian Chestplate", "黑曜石风衣")
                    .build();
    /**
     * 黑曜石护腿 - +8% 仆从伤害
     */
    public static final DeferredItem<Item> ObsidianLeggings =
            Register.register(ARMOR, "obsidian_leggings", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.ObsidianArmorMaterial, ArmorItem.Type.LEGGINGS)
                            .modifier(Attributes.ARMOR, 2, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantDamage, 0.08, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ServantryItemRegister.Silk, 10)
                            .ingredient(Items.OBSIDIAN, 20)
                            .result(ServantryArmorRegister.ObsidianLeggings)
                            .save(output))
                    .language("Obsidian Leggings", "黑曜石裤")
                    .build();
    /**
     * 黑曜石靴子 - +8% 移动速度
     */
    public static final DeferredItem<Item> ObsidianBoots =
            Register.register(ARMOR, "obsidian_boots", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.ObsidianArmorMaterial, ArmorItem.Type.BOOTS)
                            .modifier(Attributes.ARMOR, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(Attributes.MOVEMENT_SPEED, 0.08, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ServantryItemRegister.Silk, 10)
                            .ingredient(Items.OBSIDIAN, 20)
                            .result(ServantryArmorRegister.ObsidianBoots)
                            .save(output))
                    .language("Obsidian Boots", "黑曜石靴")
                    .build();
    /**
     * 蜘蛛面具 - +5% 仆从伤害，+1 仆从栏
     */
    public static final DeferredItem<Item> SpiderMask =
            Register.register(ARMOR, "spider_mask", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.SpiderArmorMaterial, ArmorItem.Type.HELMET)
                            .modifier(Attributes.ARMOR, 2, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantDamage, 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .modifier(ServantryAttributeRegister.ServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ServantryItemRegister.SpiderFang, 8)
                            .result(ServantryArmorRegister.SpiderMask)
                            .save(output))
                    .language("Spider Mask", "蜘蛛面具")
                    .build();
    /**
     * 蜘蛛胸甲 - +5% 仆从伤害，+1 仆从栏
     */
    public static final DeferredItem<Item> SpiderChestplate =
            Register.register(ARMOR, "spider_chestplate", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.SpiderArmorMaterial, ArmorItem.Type.CHESTPLATE)
                            .modifier(Attributes.ARMOR, 3, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantDamage, 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .modifier(ServantryAttributeRegister.ServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ServantryItemRegister.SpiderFang, 16)
                            .result(ServantryArmorRegister.SpiderChestplate)
                            .save(output))
                    .language("Spider Breastplate", "蜘蛛胸甲")
                    .build();
    /**
     * 蜘蛛护胫 - +6% 仆从伤害，+1 仆从栏
     */
    public static final DeferredItem<Item> SpiderLeggings =
            Register.register(ARMOR, "spider_leggings", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.SpiderArmorMaterial, ArmorItem.Type.LEGGINGS)
                            .modifier(Attributes.ARMOR, 3, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantDamage, 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .modifier(ServantryAttributeRegister.ServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ServantryItemRegister.SpiderFang, 12)
                            .result(ServantryArmorRegister.SpiderLeggings)
                            .save(output))
                    .language("Spider Greaves", "蜘蛛护胫")
                    .build();
    /**
     * 蜘蛛靴
     */
    public static final DeferredItem<Item> SpiderBoots =
            Register.register(ARMOR, "spider_boots", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.SpiderArmorMaterial, ArmorItem.Type.BOOTS)
                            .modifier(Attributes.ARMOR, 2, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantDamage, 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ServantryItemRegister.SpiderFang, 8)
                            .result(ServantryArmorRegister.SpiderBoots)
                            .save(output))
                    .language("Spider Boots", "蜘蛛靴")
                    .build();
    /**
     * 禁戒面具 - +15% 召唤伤害
     */
    public static final DeferredItem<Item> ForbiddenMask =
            Register.register(ARMOR, "forbidden_mask", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.ForbiddenArmorMaterial, ArmorItem.Type.HELMET)
                            .modifier(Attributes.ARMOR, 2, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantDamage, 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ServantryItemRegister.AdamantiteIngot, 10)
                            .ingredient(ServantryItemRegister.ForbiddenFragment, 1)
                            .result(ServantryArmorRegister.ForbiddenMask)
                            .save(output))
                    .language("Forbidden Mask", "禁戒面具")
                    .build();
    /**
     * 禁戒长袍 - +10%召唤伤害，+1 仆从栏
     */
    public static final DeferredItem<Item> ForbiddenRobe =
            Register.register(ARMOR, "forbidden_robe", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.ForbiddenArmorMaterial, ArmorItem.Type.CHESTPLATE)
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
                    .language("Forbidden Robe", "禁戒长袍")
                    .build();
    /**
     * 禁戒裤 - +1 仆从栏
     */
    public static final DeferredItem<Item> ForbiddenLeggings =
            Register.register(ARMOR, "forbidden_leggings", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.ForbiddenArmorMaterial, ArmorItem.Type.LEGGINGS)
                            .modifier(Attributes.ARMOR, 3, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ServantryItemRegister.AdamantiteIngot, 16)
                            .ingredient(ServantryItemRegister.ForbiddenFragment, 1)
                            .result(ServantryArmorRegister.ForbiddenLeggings)
                            .save(output))
                    .language("Forbidden Leggings", "禁戒裤")
                    .build();
    /**
     * 禁戒靴
     */
    public static final DeferredItem<Item> ForbiddenBoots =
            Register.register(ARMOR, "forbidden_boots", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.ForbiddenArmorMaterial, ArmorItem.Type.BOOTS)
                            .modifier(Attributes.ARMOR, 2, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantDamage, 0.10, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ServantryItemRegister.AdamantiteIngot, 8)
                            .ingredient(ServantryItemRegister.ForbiddenFragment, 1)
                            .result(ServantryArmorRegister.ForbiddenBoots)
                            .save(output))
                    .language("Forbidden Boots", "禁戒战靴")
                    .build();
    /**
     * 神圣头盔
     */
    public static final DeferredItem<Item> HallowedHelmet =
            Register.register(ARMOR, "hallowed_helmet", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.HallowedArmorMaterial, ArmorItem.Type.HELMET)
                            .modifier(Attributes.ARMOR, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantDamage, 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ServantryItemRegister.HallowedIngot, 12)
                            .result(ServantryArmorRegister.HallowedHelmet)
                            .save(output))
                    .language("Hallowed Helmet", "神圣兜帽")
                    .build();
    /**
     * 神圣胸甲 - +14% 仆从伤害
     */
    public static final DeferredItem<Item> HallowedChestplate =
            Register.register(ARMOR, "hallowed_chestplate", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.HallowedArmorMaterial, ArmorItem.Type.CHESTPLATE)
                            .modifier(Attributes.ARMOR, 5, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantDamage, 0.14, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ServantryItemRegister.HallowedIngot, 24)
                            .result(ServantryArmorRegister.HallowedChestplate)
                            .save(output))
                    .language("Hallowed Chestplate", "神圣板甲")
                    .build();
    /**
     * 神圣护腿 - +7% 仆从伤害
     */
    public static final DeferredItem<Item> HallowedLeggings =
            Register.register(ARMOR, "hallowed_leggings", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.HallowedArmorMaterial, ArmorItem.Type.LEGGINGS)
                            .modifier(Attributes.ARMOR, 4, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantDamage, 0.07, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ServantryItemRegister.HallowedIngot, 12)
                            .result(ServantryArmorRegister.HallowedLeggings)
                            .save(output))
                    .language("Hallowed Leggings", "神圣护胫")
                    .build();
    /**
     * 神圣靴子 - +8% 移动速度
     */
    public static final DeferredItem<Item> HallowedBoots =
            Register.register(ARMOR, "hallowed_boots", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.HallowedArmorMaterial, ArmorItem.Type.BOOTS)
                            .modifier(Attributes.ARMOR, 3, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(Attributes.MOVEMENT_SPEED, 0.08, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ServantryItemRegister.HallowedIngot, 12)
                            .result(ServantryArmorRegister.HallowedBoots)
                            .save(output))
                    .language("Hallowed Boots", "神圣战靴")
                    .build();
    /**
     * 叶绿面具 - +1 召唤栏，+10% 仆从伤害
     */
    public static final DeferredItem<Item> ChlorophyteHelmet =
            Register.register(ARMOR, "chlorophyte_helmet", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.ChlorophyteArmorMaterial, ArmorItem.Type.HELMET)
                            .modifier(Attributes.ARMOR, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantDamage, 0.16, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ServantryItemRegister.ChlorophyteIngot, 12)
                            .result(ServantryArmorRegister.ChlorophyteHelmet)
                            .save(output))
                    .language("Chlorophyte Mask", "叶绿面具")
                    .build();
    /**
     * 叶绿板甲 - +6% 仆从伤害
     */
    public static final DeferredItem<Item> ChlorophyteChestplate =
            Register.register(ARMOR, "chlorophyte_chestplate", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.ChlorophyteArmorMaterial, ArmorItem.Type.CHESTPLATE)
                            .modifier(Attributes.ARMOR, 6, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantDamage, 0.19, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ServantryItemRegister.ChlorophyteIngot, 24)
                            .result(ServantryArmorRegister.ChlorophyteChestplate)
                            .save(output))
                    .language("Chlorophyte Breastplate", "叶绿板甲")
                    .build();
    /**
     * 叶绿护胫 - +4% 仆从伤害
     */
    public static final DeferredItem<Item> ChlorophyteLeggings =
            Register.register(ARMOR, "chlorophyte_leggings", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.ChlorophyteArmorMaterial, ArmorItem.Type.LEGGINGS)
                            .modifier(Attributes.ARMOR, 5, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantDamage, 0.16, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ServantryItemRegister.ChlorophyteIngot, 12)
                            .result(ServantryArmorRegister.ChlorophyteLeggings)
                            .save(output))
                    .language("Chlorophyte Leggings", "叶绿护胫")
                    .build();
    /**
     * 叶绿战靴 - +2% 仆从伤害
     */
    public static final DeferredItem<Item> ChlorophyteBoots =
            Register.register(ARMOR, "chlorophyte_boots", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.ChlorophyteArmorMaterial, ArmorItem.Type.BOOTS)
                            .modifier(Attributes.ARMOR, 3, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(Attributes.MOVEMENT_SPEED, 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ServantryItemRegister.ChlorophyteIngot, 12)
                            .result(ServantryArmorRegister.ChlorophyteBoots)
                            .save(output))
                    .language("Chlorophyte Boots", "叶绿战靴")
                    .build();
    /**
     * 英灵殿骑士头盔 - +2 哨兵栏，+10% 仆从伤害，+10% 原版伤害
     */
    public static final DeferredItem<Item> ValhallaKnightHelmet =
            Register.register(ARMOR, "valhalla_knight_helmet", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.ValhallaKnightArmorMaterial, ArmorItem.Type.HELMET)
                            .modifier(Attributes.ARMOR, 7, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.SentryServantMaxCount, 2, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantDamage, 0.10, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .modifier(Attributes.ATTACK_DAMAGE, 0.10, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .language("Valhalla Knight Helmet", "英灵殿骑士头盔")
                    .build();
    /**
     * 英灵殿骑士胸甲 - +30% 仆从伤害，+0.4 生命再生
     */
    public static final DeferredItem<Item> ValhallaKnightChestplate =
            Register.register(ARMOR, "valhalla_knight_chestplate", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.ValhallaKnightArmorMaterial, ArmorItem.Type.CHESTPLATE)
                            .modifier(Attributes.ARMOR, 8, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantDamage, 0.30, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .modifier(ServantryAttributeRegister.HealthRegen, 0.4, AttributeModifier.Operation.ADD_VALUE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .language("Valhalla Knight Chestplate", "英灵殿骑士胸甲")
                    .build();
    /**
     * 英灵殿骑士护腿 - +20% 仆从伤害，+20% 原版伤害
     */
    public static final DeferredItem<Item> ValhallaKnightLeggings =
            Register.register(ARMOR, "valhalla_knight_leggings", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.ValhallaKnightArmorMaterial, ArmorItem.Type.LEGGINGS)
                            .modifier(Attributes.ARMOR, 6, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantDamage, 0.20, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .modifier(Attributes.ATTACK_DAMAGE, 0.20, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .language("Valhalla Knight Leggings", "英灵殿骑士护腿")
                    .build();
    /**
     * 英灵殿骑士战靴 - +20% 移动速度
     */
    public static final DeferredItem<Item> ValhallaKnightBoots =
            Register.register(ARMOR, "valhalla_knight_boots", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.ValhallaKnightArmorMaterial, ArmorItem.Type.BOOTS)
                            .modifier(Attributes.ARMOR, 4, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(Attributes.MOVEMENT_SPEED, 0.20, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .language("Valhalla Knight Boots", "英灵殿骑士战靴")
                    .build();
    /**
     * 提基面具 - +1 召唤栏，+10% 仆从伤害
     */
    public static final DeferredItem<Item> TikiHelmet =
            Register.register(ARMOR, "tiki_helmet", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.TikiArmorMaterial, ArmorItem.Type.HELMET)
                            .modifier(Attributes.ARMOR, 2, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantDamage, 0.2, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .language("Tiki Mask", "提基面具")
                    .build();
    /**
     * 提基胸甲 - +10% 仆从伤害
     */
    public static final DeferredItem<Item> TikiChestplate =
            Register.register(ARMOR, "tiki_chestplate", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.TikiArmorMaterial, ArmorItem.Type.CHESTPLATE)
                            .modifier(Attributes.ARMOR, 6, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantDamage, 0.10, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .language("Tiki Chestplate", "提基胸甲")
                    .build();
    /**
     * 提基护腿 - +7% 仆从伤害
     */
    public static final DeferredItem<Item> TikiLeggings =
            Register.register(ARMOR, "tiki_leggings", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.TikiArmorMaterial, ArmorItem.Type.LEGGINGS)
                            .modifier(Attributes.ARMOR, 4, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .language("Tiki Leggings", "提基护腿")
                    .build();
    /**
     * 提基战靴 - +3% 仆从伤害
     */
    public static final DeferredItem<Item> TikiBoots =
            Register.register(ARMOR, "tiki_boots", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.TikiArmorMaterial, ArmorItem.Type.BOOTS)
                            .modifier(Attributes.ARMOR, 3, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(ServantryAttributeRegister.ServantDamage, 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .language("Tiki Boots", "提基战靴")
                    .build();
    /**
     * 阴森头盔 - +11% 仆从伤害
     */
    public static final DeferredItem<Item> SpookyHelmet =
            Register.register(ARMOR, "spooky_helmet", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.SpookyArmorMaterial, ArmorItem.Type.HELMET)
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
                    .language("Spooky Helmet", "阴森头盔")
                    .build();
    /**
     * 阴森胸甲 - +11% 仆从伤害
     */
    public static final DeferredItem<Item> SpookyChestplate =
            Register.register(ARMOR, "spooky_chestplate", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.SpookyArmorMaterial, ArmorItem.Type.CHESTPLATE)
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
                    .language("Spooky Chestplate", "阴森胸甲")
                    .build();
    /**
     * 阴森护腿 - +8% 仆从伤害，+1 召唤栏
     */
    public static final DeferredItem<Item> SpookyLeggings =
            Register.register(ARMOR, "spooky_leggings", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.SpookyArmorMaterial, ArmorItem.Type.LEGGINGS)
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
                    .language("Spooky Leggings", "阴森护腿")
                    .build();
    /**
     * 阴森战靴 - +4% 仆从伤害
     */
    public static final DeferredItem<Item> SpookyBoots =
            Register.register(ARMOR, "spooky_boots", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.SpookyArmorMaterial, ArmorItem.Type.BOOTS)
                            .modifier(Attributes.ARMOR, 2, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(Attributes.MOVEMENT_SPEED, 0.2, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(Items.WARPED_STEM, 150)
                            .ingredient(Items.WARPED_WART_BLOCK, 150)
                            .result(ServantryArmorRegister.SpookyBoots)
                            .save(output))
                    .language("Spooky Boots", "阴森战靴")
                    .build();
    /**
     * 星尘头盔 - +1 召唤栏，+16% 仆从伤害
     */
    public static final DeferredItem<Item> StardustHelmet =
            Register.register(ARMOR, "stardust_helmet", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.StardustArmorMaterial, ArmorItem.Type.HELMET)
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
                    .language("Stardust Helmet", "星尘头盔")
                    .build();
    /**
     * 星尘板甲 - +22% 仆从伤害
     */
    public static final DeferredItem<Item> StardustChestplate =
            Register.register(ARMOR, "stardust_chestplate", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.StardustArmorMaterial, ArmorItem.Type.CHESTPLATE)
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
                    .language("Stardust Chestplate", "星尘板甲")
                    .build();
    /**
     * 星尘护腿 - +15% 仆从伤害
     */
    public static final DeferredItem<Item> StardustLeggings =
            Register.register(ARMOR, "stardust_leggings", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.StardustArmorMaterial, ArmorItem.Type.LEGGINGS)
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
                    .language("Stardust Leggings", "星尘护腿")
                    .build();
    /**
     * 星尘战靴 - +7% 仆从伤害
     */
    public static final DeferredItem<Item> StardustBoots =
            Register.register(ARMOR, "stardust_boots", () -> AttributeArmorItemBuilder.builder(ServantryArmorMaterialRegister.StardustArmorMaterial, ArmorItem.Type.BOOTS)
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
                    .language("Stardust Boots", "星尘战靴")
                    .build();

    public static void register() {
    }
}
