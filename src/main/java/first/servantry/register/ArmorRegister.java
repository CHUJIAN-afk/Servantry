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
     * 神圣头盔 - +1 仆从栏，+7% 仆从伤害
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
     * 神圣胸甲 - +1 仆从栏，+7% 仆从伤害
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
     * 神圣护腿 - +1 仆从栏，+7% 仆从伤害
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
                    .build();    /**
     * 神圣靴子 - +7% 移动速度，+7% 仆从伤害
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

    public static void register() {
    }
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


}
