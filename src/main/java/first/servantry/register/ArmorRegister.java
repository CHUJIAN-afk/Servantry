package first.servantry.register;

import first.servantry.Servantry;
import first.servantry.client.creativeTab.AnimInfo;
import first.servantry.common.item.AttributeArmorItem;
import first.servantry.common.recipe.MithrilAnvilRecipe;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;

public class ArmorRegister {

    private static final Registers Register = Registers.getInstance();
    public static final TabGroup ARMOR = new TabGroup(1, Servantry.rl("textures/item/banner/default_banner.png"), new AnimInfo(18, 1, 1));
    /**
     * 小雪怪外套
     */
    public static final DeferredItem<Item> FlinxFurCoat =
            Register.register(ARMOR, "flinx_fur_coat", () -> AttributeArmorItem.builder(ArmorMaterialRegister.Flinx, ArmorItem.Type.CHESTPLATE)
                            .modifier(AttributeRegister.ServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(AttributeRegister.ServantDamage, 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ItemRegister.Silk, 6)
                            .ingredient(Items.LEATHER, 6)
                            .result(ArmorRegister.FlinxFurCoat)
                            .save(output))
                    .language("Flinx Fur Coat", "小雪怪皮毛外套")
                    .build();
    /**
     * 蜜蜂头饰 - +4% 仆从伤害，+1 仆从栏
     */
    public static final DeferredItem<Item> BeeHeadgear =
            Register.register(ARMOR, "bee_headgear", () -> AttributeArmorItem.builder(ArmorMaterialRegister.BeeArmorMaterial, ArmorItem.Type.HELMET)
                            .modifier(AttributeRegister.ServantDamage, 0.04, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .modifier(AttributeRegister.ServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ItemRegister.BeeWax, 8)
                            .result(ArmorRegister.BeeHeadgear)
                            .save(output))
                    .language("Bee Headgear", "蜜蜂头饰")
                    .build();
    /**
     * 蜜蜂胸甲 - +4% 仆从伤害，+1 仆从栏
     */
    public static final DeferredItem<Item> BeeChestplate =
            Register.register(ARMOR, "bee_chestplate", () -> AttributeArmorItem.builder(ArmorMaterialRegister.BeeArmorMaterial, ArmorItem.Type.CHESTPLATE)
                            .modifier(AttributeRegister.ServantDamage, 0.04, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .modifier(AttributeRegister.ServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ItemRegister.BeeWax, 12)
                            .result(ArmorRegister.BeeChestplate)
                            .save(output))
                    .language("Bee Breastplate", "蜜蜂胸甲")
                    .build();
    /**
     * 蜜蜂护胫 - +5% 仆从伤害
     */
    public static final DeferredItem<Item> BeeLeggings =
            Register.register(ARMOR, "bee_leggings", () -> AttributeArmorItem.builder(ArmorMaterialRegister.BeeArmorMaterial, ArmorItem.Type.LEGGINGS)
                            .modifier(AttributeRegister.ServantDamage, 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ItemRegister.BeeWax, 10)
                            .result(ArmorRegister.BeeLeggings)
                            .save(output))
                    .language("Bee Leggings", "蜜蜂护胫")
                    .build();
    /**
     * 蜜蜂靴 - 套装奖励部分
     */
    public static final DeferredItem<Item> BeeBoots =
            Register.register(ARMOR, "bee_boots", () -> AttributeArmorItem.builder(ArmorMaterialRegister.BeeArmorMaterial, ArmorItem.Type.BOOTS)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ItemRegister.BeeWax, 6)
                            .result(ArmorRegister.BeeBoots)
                            .save(output))
                    .language("Bee Boots", "蜜蜂靴")
                    .build();
    /**
     * 黑曜石头盔 - +8% 仆从伤害
     */
    public static final DeferredItem<Item> ObsidianHelmet =
            Register.register(ARMOR, "obsidian_helmet", () -> AttributeArmorItem.builder(ArmorMaterialRegister.ObsidianArmorMaterial, ArmorItem.Type.HELMET)
                            .modifier(AttributeRegister.ServantDamage, 0.08, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ItemRegister.Silk, 10)
                            .ingredient(Items.OBSIDIAN, 20)
                            .result(ArmorRegister.ObsidianHelmet)
                            .save(output))
                    .language("Obsidian Helmet", "黑曜石逃犯帽")
                    .build();
    /**
     * 黑曜石胸甲 - +1 仆从栏
     */
    public static final DeferredItem<Item> ObsidianChestplate =
            Register.register(ARMOR, "obsidian_chestplate", () -> AttributeArmorItem.builder(ArmorMaterialRegister.ObsidianArmorMaterial, ArmorItem.Type.CHESTPLATE)
                            .modifier(AttributeRegister.ServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ItemRegister.Silk, 10)
                            .ingredient(Items.OBSIDIAN, 20)
                            .result(ArmorRegister.ObsidianChestplate)
                            .save(output))
                    .language("Obsidian Chestplate", "黑曜石风衣")
                    .build();
    /**
     * 黑曜石护腿 - +8% 仆从伤害
     */
    public static final DeferredItem<Item> ObsidianLeggings =
            Register.register(ARMOR, "obsidian_leggings", () -> AttributeArmorItem.builder(ArmorMaterialRegister.ObsidianArmorMaterial, ArmorItem.Type.LEGGINGS)
                            .modifier(AttributeRegister.ServantDamage, 0.08, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ItemRegister.Silk, 10)
                            .ingredient(Items.OBSIDIAN, 20)
                            .result(ArmorRegister.ObsidianLeggings)
                            .save(output))
                    .language("Obsidian Leggings", "黑曜石裤")
                    .build();
    /**
     * 黑曜石靴子 - +8% 移动速度
     */
    public static final DeferredItem<Item> ObsidianBoots =
            Register.register(ARMOR, "obsidian_boots", () -> AttributeArmorItem.builder(ArmorMaterialRegister.ObsidianArmorMaterial, ArmorItem.Type.BOOTS)
                            .modifier(Attributes.MOVEMENT_SPEED, 0.08, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ItemRegister.Silk, 10)
                            .ingredient(Items.OBSIDIAN, 20)
                            .result(ArmorRegister.ObsidianBoots)
                            .save(output))
                    .language("Obsidian Boots", "黑曜石靴")
                    .build();
    /**
     * 蜘蛛面具 - +5% 仆从伤害，+1 仆从栏
     */
    public static final DeferredItem<Item> SpiderMask =
            Register.register(ARMOR, "spider_mask", () -> AttributeArmorItem.builder(ArmorMaterialRegister.SpiderArmorMaterial, ArmorItem.Type.HELMET)
                            .modifier(AttributeRegister.ServantDamage, 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .modifier(AttributeRegister.ServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ItemRegister.SpiderFang, 8)
                            .result(ArmorRegister.SpiderMask)
                            .save(output))
                    .language("Spider Mask", "蜘蛛面具")
                    .build();
    /**
     * 蜘蛛胸甲 - +5% 仆从伤害，+1 仆从栏
     */
    public static final DeferredItem<Item> SpiderChestplate =
            Register.register(ARMOR, "spider_chestplate", () -> AttributeArmorItem.builder(ArmorMaterialRegister.SpiderArmorMaterial, ArmorItem.Type.CHESTPLATE)
                            .modifier(AttributeRegister.ServantDamage, 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .modifier(AttributeRegister.ServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ItemRegister.SpiderFang, 16)
                            .result(ArmorRegister.SpiderChestplate)
                            .save(output))
                    .language("Spider Breastplate", "蜘蛛胸甲")
                    .build();
    /**
     * 蜘蛛护胫 - +6% 仆从伤害，+1 仆从栏
     */
    public static final DeferredItem<Item> SpiderLeggings =
            Register.register(ARMOR, "spider_leggings", () -> AttributeArmorItem.builder(ArmorMaterialRegister.SpiderArmorMaterial, ArmorItem.Type.LEGGINGS)
                            .modifier(AttributeRegister.ServantDamage, 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .modifier(AttributeRegister.ServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ItemRegister.SpiderFang, 12)
                            .result(ArmorRegister.SpiderLeggings)
                            .save(output))
                    .language("Spider Greaves", "蜘蛛护胫")
                    .build();
    /**
     * 蜘蛛靴
     */
    public static final DeferredItem<Item> SpiderBoots =
            Register.register(ARMOR, "spider_boots", () -> AttributeArmorItem.builder(ArmorMaterialRegister.SpiderArmorMaterial, ArmorItem.Type.BOOTS)
                            .modifier(AttributeRegister.ServantDamage, 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ItemRegister.SpiderFang, 8)
                            .result(ArmorRegister.SpiderBoots)
                            .save(output))
                    .language("Spider Boots", "蜘蛛靴")
                    .build();
    /**
     * 禁戒面具 - +15% 召唤伤害
     */
    public static final DeferredItem<Item> ForbiddenMask =
            Register.register(ARMOR, "forbidden_mask", () -> AttributeArmorItem.builder(ArmorMaterialRegister.ForbiddenArmorMaterial, ArmorItem.Type.HELMET)
                            .modifier(AttributeRegister.ServantDamage, 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ItemRegister.AdamantiteIngot, 10)
                            .ingredient(ItemRegister.ForbiddenFragment, 1)
                            .result(ArmorRegister.ForbiddenMask)
                            .save(output))
                    .language("Forbidden Mask", "禁戒面具")
                    .build();
    /**
     * 禁戒长袍 - +10%召唤伤害，+1 仆从栏
     */
    public static final DeferredItem<Item> ForbiddenRobe =
            Register.register(ARMOR, "forbidden_robe", () -> AttributeArmorItem.builder(ArmorMaterialRegister.ForbiddenArmorMaterial, ArmorItem.Type.CHESTPLATE)
                            .modifier(AttributeRegister.ServantDamage, 0.10, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .modifier(AttributeRegister.ServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ItemRegister.AdamantiteIngot, 20)
                            .ingredient(ItemRegister.ForbiddenFragment, 1)
                            .result(ArmorRegister.ForbiddenRobe)
                            .save(output))
                    .language("Forbidden Robe", "禁戒长袍")
                    .build();
    /**
     * 禁戒裤 - +1 仆从栏
     */
    public static final DeferredItem<Item> ForbiddenLeggings =
            Register.register(ARMOR, "forbidden_leggings", () -> AttributeArmorItem.builder(ArmorMaterialRegister.ForbiddenArmorMaterial, ArmorItem.Type.LEGGINGS)
                            .modifier(AttributeRegister.ServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ItemRegister.AdamantiteIngot, 16)
                            .ingredient(ItemRegister.ForbiddenFragment, 1)
                            .result(ArmorRegister.ForbiddenLeggings)
                            .save(output))
                    .language("Forbidden Leggings", "禁戒裤")
                    .build();
    /**
     * 禁戒靴
     */
    public static final DeferredItem<Item> ForbiddenBoots =
            Register.register(ARMOR, "forbidden_boots", () -> AttributeArmorItem.builder(ArmorMaterialRegister.ForbiddenArmorMaterial, ArmorItem.Type.BOOTS)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .modifier(AttributeRegister.ServantDamage, 0.10, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ItemRegister.AdamantiteIngot, 8)
                            .ingredient(ItemRegister.ForbiddenFragment, 1)
                            .result(ArmorRegister.ForbiddenBoots)
                            .save(output))
                    .language("Forbidden Boots", "禁戒战靴")
                    .build();
    /**
     * 神圣头盔
     */
    public static final DeferredItem<Item> HallowedHelmet =
            Register.register(ARMOR, "hallowed_helmet", () -> AttributeArmorItem.builder(ArmorMaterialRegister.HallowedArmorMaterial, ArmorItem.Type.HELMET)
                            .modifier(AttributeRegister.ServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(AttributeRegister.ServantDamage, 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ItemRegister.HallowedIngot, 12)
                            .result(ArmorRegister.HallowedHelmet)
                            .save(output))
                    .language("Hallowed Helmet", "神圣兜帽")
                    .build();
    /**
     * 神圣胸甲 - +14% 仆从伤害
     */
    public static final DeferredItem<Item> HallowedChestplate =
            Register.register(ARMOR, "hallowed_chestplate", () -> AttributeArmorItem.builder(ArmorMaterialRegister.HallowedArmorMaterial, ArmorItem.Type.CHESTPLATE)
                            .modifier(AttributeRegister.ServantDamage, 0.14, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ItemRegister.HallowedIngot, 24)
                            .result(ArmorRegister.HallowedChestplate)
                            .save(output))
                    .language("Hallowed Chestplate", "神圣板甲")
                    .build();
    /**
     * 神圣护腿 - +7% 仆从伤害
     */
    public static final DeferredItem<Item> HallowedLeggings =
            Register.register(ARMOR, "hallowed_leggings", () -> AttributeArmorItem.builder(ArmorMaterialRegister.HallowedArmorMaterial, ArmorItem.Type.LEGGINGS)
                            .modifier(AttributeRegister.ServantDamage, 0.07, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ItemRegister.HallowedIngot, 12)
                            .result(ArmorRegister.HallowedLeggings)
                            .save(output))
                    .language("Hallowed Leggings", "神圣护胫")
                    .build();
    /**
     * 神圣靴子 - +8% 移动速度
     */
    public static final DeferredItem<Item> HallowedBoots =
            Register.register(ARMOR, "hallowed_boots", () -> AttributeArmorItem.builder(ArmorMaterialRegister.HallowedArmorMaterial, ArmorItem.Type.BOOTS)
                            .modifier(Attributes.MOVEMENT_SPEED, 0.08, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ItemRegister.HallowedIngot, 12)
                            .result(ArmorRegister.HallowedBoots)
                            .save(output))
                    .language("Hallowed Boots", "神圣战靴")
                    .build();
    /**
     * 叶绿面具 - +1 召唤栏，+10% 仆从伤害
     */
    public static final DeferredItem<Item> ChlorophyteHelmet =
            Register.register(ARMOR, "chlorophyte_helmet", () -> AttributeArmorItem.builder(ArmorMaterialRegister.ChlorophyteArmorMaterial, ArmorItem.Type.HELMET)
                            .modifier(AttributeRegister.ServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(AttributeRegister.ServantDamage, 0.16, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ItemRegister.ChlorophyteIngot, 12)
                            .result(ArmorRegister.ChlorophyteHelmet)
                            .save(output))
                    .language("Chlorophyte Mask", "叶绿面具")
                    .build();
    /**
     * 叶绿板甲 - +6% 仆从伤害
     */
    public static final DeferredItem<Item> ChlorophyteChestplate =
            Register.register(ARMOR, "chlorophyte_chestplate", () -> AttributeArmorItem.builder(ArmorMaterialRegister.ChlorophyteArmorMaterial, ArmorItem.Type.CHESTPLATE)
                            .modifier(AttributeRegister.ServantDamage, 0.19, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ItemRegister.ChlorophyteIngot, 24)
                            .result(ArmorRegister.ChlorophyteChestplate)
                            .save(output))
                    .language("Chlorophyte Breastplate", "叶绿板甲")
                    .build();
    /**
     * 叶绿护胫 - +4% 仆从伤害
     */
    public static final DeferredItem<Item> ChlorophyteLeggings =
            Register.register(ARMOR, "chlorophyte_leggings", () -> AttributeArmorItem.builder(ArmorMaterialRegister.ChlorophyteArmorMaterial, ArmorItem.Type.LEGGINGS)
                            .modifier(AttributeRegister.ServantDamage, 0.16, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ItemRegister.ChlorophyteIngot, 12)
                            .result(ArmorRegister.ChlorophyteLeggings)
                            .save(output))
                    .language("Chlorophyte Leggings", "叶绿护胫")
                    .build();
    /**
     * 叶绿战靴 - +2% 仆从伤害
     */
    public static final DeferredItem<Item> ChlorophyteBoots =
            Register.register(ARMOR, "chlorophyte_boots", () -> AttributeArmorItem.builder(ArmorMaterialRegister.ChlorophyteArmorMaterial, ArmorItem.Type.BOOTS)
                            .modifier(Attributes.MOVEMENT_SPEED, 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ItemRegister.ChlorophyteIngot, 12)
                            .result(ArmorRegister.ChlorophyteBoots)
                            .save(output))
                    .language("Chlorophyte Boots", "叶绿战靴")
                    .build();
    /**
     * 英灵殿骑士头盔 - +1 召唤栏，+10% 仆从伤害，+10% 原版伤害
     */
    public static final DeferredItem<Item> ValhallaKnightHelmet =
            Register.register(ARMOR, "valhalla_knight_helmet", () -> AttributeArmorItem.builder(ArmorMaterialRegister.ValhallaKnightArmorMaterial, ArmorItem.Type.HELMET)
                            .modifier(AttributeRegister.ServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(AttributeRegister.ServantDamage, 0.10, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .modifier(Attributes.ATTACK_DAMAGE, 0.10, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .language("Valhalla Knight Helmet", "英灵殿骑士头盔")
                    .build();
    /**
     * 英灵殿骑士胸甲 - +30% 仆从伤害，+0.4 生命再生
     */
    public static final DeferredItem<Item> ValhallaKnightChestplate =
            Register.register(ARMOR, "valhalla_knight_chestplate", () -> AttributeArmorItem.builder(ArmorMaterialRegister.ValhallaKnightArmorMaterial, ArmorItem.Type.CHESTPLATE)
                            .modifier(AttributeRegister.ServantDamage, 0.30, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .modifier(AttributeRegister.HealthRegen, 0.4, AttributeModifier.Operation.ADD_VALUE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .language("Valhalla Knight Chestplate", "英灵殿骑士胸甲")
                    .build();
    /**
     * 英灵殿骑士护腿 - +20% 仆从伤害，+20% 原版伤害
     */
    public static final DeferredItem<Item> ValhallaKnightLeggings =
            Register.register(ARMOR, "valhalla_knight_leggings", () -> AttributeArmorItem.builder(ArmorMaterialRegister.ValhallaKnightArmorMaterial, ArmorItem.Type.LEGGINGS)
                            .modifier(AttributeRegister.ServantDamage, 0.20, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .modifier(Attributes.ATTACK_DAMAGE, 0.20, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .language("Valhalla Knight Leggings", "英灵殿骑士护腿")
                    .build();
    /**
     * 英灵殿骑士战靴 - +20% 移动速度
     */
    public static final DeferredItem<Item> ValhallaKnightBoots =
            Register.register(ARMOR, "valhalla_knight_boots", () -> AttributeArmorItem.builder(ArmorMaterialRegister.ValhallaKnightArmorMaterial, ArmorItem.Type.BOOTS)
                            .modifier(Attributes.MOVEMENT_SPEED, 0.20, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .language("Valhalla Knight Boots", "英灵殿骑士战靴")
                    .build();
    /**
     * 提基面具 - +1 召唤栏，+10% 仆从伤害
     */
    public static final DeferredItem<Item> TikiHelmet =
            Register.register(ARMOR, "tiki_helmet", () -> AttributeArmorItem.builder(ArmorMaterialRegister.TikiArmorMaterial, ArmorItem.Type.HELMET)
                            .modifier(AttributeRegister.ServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(AttributeRegister.ServantDamage, 0.2, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .language("Tiki Mask", "提基面具")
                    .build();
    /**
     * 提基胸甲 - +10% 仆从伤害
     */
    public static final DeferredItem<Item> TikiChestplate =
            Register.register(ARMOR, "tiki_chestplate", () -> AttributeArmorItem.builder(ArmorMaterialRegister.TikiArmorMaterial, ArmorItem.Type.CHESTPLATE)
                            .modifier(AttributeRegister.ServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(AttributeRegister.ServantDamage, 0.10, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .language("Tiki Chestplate", "提基胸甲")
                    .build();
    /**
     * 提基护腿 - +7% 仆从伤害
     */
    public static final DeferredItem<Item> TikiLeggings =
            Register.register(ARMOR, "tiki_leggings", () -> AttributeArmorItem.builder(ArmorMaterialRegister.TikiArmorMaterial, ArmorItem.Type.LEGGINGS)
                            .modifier(AttributeRegister.ServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .language("Tiki Leggings", "提基护腿")
                    .build();
    /**
     * 提基战靴 - +3% 仆从伤害
     */
    public static final DeferredItem<Item> TikiBoots =
            Register.register(ARMOR, "tiki_boots", () -> AttributeArmorItem.builder(ArmorMaterialRegister.TikiArmorMaterial, ArmorItem.Type.BOOTS)
                            .modifier(AttributeRegister.ServantDamage, 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .language("Tiki Boots", "提基战靴")
                    .build();
    /**
     * 阴森头盔 - +11% 仆从伤害
     */
    public static final DeferredItem<Item> SpookyHelmet =
            Register.register(ARMOR, "spooky_helmet", () -> AttributeArmorItem.builder(ArmorMaterialRegister.SpookyArmorMaterial, ArmorItem.Type.HELMET)
                            .modifier(AttributeRegister.ServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(AttributeRegister.ServantDamage, 0.11, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(Items.WARPED_STEM, 200)
                            .ingredient(Items.WARPED_WART_BLOCK, 200)
                            .result(ArmorRegister.SpookyHelmet)
                            .save(output))
                    .language("Spooky Helmet", "阴森头盔")
                    .build();
    /**
     * 阴森胸甲 - +11% 仆从伤害
     */
    public static final DeferredItem<Item> SpookyChestplate =
            Register.register(ARMOR, "spooky_chestplate", () -> AttributeArmorItem.builder(ArmorMaterialRegister.SpookyArmorMaterial, ArmorItem.Type.CHESTPLATE)
                            .modifier(AttributeRegister.ServantMaxCount, 2, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(AttributeRegister.ServantDamage, 0.11, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(Items.WARPED_STEM, 300)
                            .ingredient(Items.WARPED_WART_BLOCK, 300)
                            .result(ArmorRegister.SpookyChestplate)
                            .save(output))
                    .language("Spooky Chestplate", "阴森胸甲")
                    .build();
    /**
     * 阴森护腿 - +8% 仆从伤害，+1 召唤栏
     */
    public static final DeferredItem<Item> SpookyLeggings =
            Register.register(ARMOR, "spooky_leggings", () -> AttributeArmorItem.builder(ArmorMaterialRegister.SpookyArmorMaterial, ArmorItem.Type.LEGGINGS)
                            .modifier(AttributeRegister.ServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(AttributeRegister.ServantDamage, 0.11, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(Items.WARPED_STEM, 250)
                            .ingredient(Items.WARPED_WART_BLOCK, 250)
                            .result(ArmorRegister.SpookyLeggings)
                            .save(output))
                    .language("Spooky Leggings", "阴森护腿")
                    .build();
    /**
     * 阴森战靴 - +4% 仆从伤害
     */
    public static final DeferredItem<Item> SpookyBoots =
            Register.register(ARMOR, "spooky_boots", () -> AttributeArmorItem.builder(ArmorMaterialRegister.SpookyArmorMaterial, ArmorItem.Type.BOOTS)
                            .modifier(Attributes.MOVEMENT_SPEED, 0.2, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(Items.WARPED_STEM, 150)
                            .ingredient(Items.WARPED_WART_BLOCK, 150)
                            .result(ArmorRegister.SpookyBoots)
                            .save(output))
                    .language("Spooky Boots", "阴森战靴")
                    .build();
    /**
     * 星尘头盔 - +1 召唤栏，+16% 仆从伤害
     */
    public static final DeferredItem<Item> StardustHelmet =
            Register.register(ARMOR, "stardust_helmet", () -> AttributeArmorItem.builder(ArmorMaterialRegister.StardustArmorMaterial, ArmorItem.Type.HELMET)
                            .modifier(AttributeRegister.ServantMaxCount, 2, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(AttributeRegister.ServantDamage, 0.22, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.EPIC))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ItemRegister.Stardust, 10)
                            .ingredient(ItemRegister.LuminiteIngot, 8)
                            .result(ArmorRegister.StardustHelmet)
                            .save(output))
                    .language("Stardust Helmet", "星尘头盔")
                    .build();
    /**
     * 星尘板甲 - +22% 仆从伤害
     */
    public static final DeferredItem<Item> StardustChestplate =
            Register.register(ARMOR, "stardust_chestplate", () -> AttributeArmorItem.builder(ArmorMaterialRegister.StardustArmorMaterial, ArmorItem.Type.CHESTPLATE)
                            .modifier(AttributeRegister.ServantMaxCount, 2, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(AttributeRegister.ServantDamage, 0.37, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.EPIC))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ItemRegister.Stardust, 20)
                            .ingredient(ItemRegister.LuminiteIngot, 16)
                            .result(ArmorRegister.StardustChestplate)
                            .save(output))
                    .language("Stardust Chestplate", "星尘板甲")
                    .build();
    /**
     * 星尘护腿 - +15% 仆从伤害
     */
    public static final DeferredItem<Item> StardustLeggings =
            Register.register(ARMOR, "stardust_leggings", () -> AttributeArmorItem.builder(ArmorMaterialRegister.StardustArmorMaterial, ArmorItem.Type.LEGGINGS)
                            .modifier(AttributeRegister.ServantMaxCount, 2, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(AttributeRegister.ServantDamage, 0.37, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.EPIC))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ItemRegister.Stardust, 15)
                            .ingredient(ItemRegister.LuminiteIngot, 12)
                            .result(ArmorRegister.StardustLeggings)
                            .save(output))
                    .language("Stardust Leggings", "星尘护腿")
                    .build();
    /**
     * 星尘战靴 - +7% 仆从伤害
     */
    public static final DeferredItem<Item> StardustBoots =
            Register.register(ARMOR, "stardust_boots", () -> AttributeArmorItem.builder(ArmorMaterialRegister.StardustArmorMaterial, ArmorItem.Type.BOOTS)
                            .modifier(AttributeRegister.ServantMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(AttributeRegister.ServantDamage, 0.22, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.EPIC))
                            .build())
                    .recipe(output -> MithrilAnvilRecipe.builder()
                            .ingredient(ItemRegister.Stardust, 10)
                            .ingredient(ItemRegister.LuminiteIngot, 8)
                            .result(ArmorRegister.StardustBoots)
                            .save(output))
                    .language("Stardust Boots", "星尘战靴")
                    .build();

    public static void register() {
    }
}
