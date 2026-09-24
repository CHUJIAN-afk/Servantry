package first.summoner.register;

import first.lyra.client.creativeTab.AnimBanner;
import first.lyra.common.builder.AttributeArmorItemBuilder;
import first.lyra.common.creativeTab.Section;
import first.lyra.register.LyraAttributeRegister;
import first.lyra.register.LyraItemRegistries;
import first.summoner.Summoner;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.registries.DeferredItem;

public class SummonerArmorRegister {

    public static final LyraItemRegistries REGISTRIES = Summoner.REGISTRIES;

    public static final Section ARMOR = new Section(1, Summoner.rl("textures/item/banner/default_banner.png"), new AnimBanner(18, 1, 1), SummonerItemTagsRegister.SectionArmor);
    /**
     * 小雪怪外套
     */
    public static final DeferredItem<ArmorItem> FlinxFurCoat =
            REGISTRIES.build("flinx_fur_coat", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.Flinx, ArmorItem.Type.CHESTPLATE)
                            .modifier(Attributes.ARMOR, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.MinionMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.SummonDamage, 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Flinx Fur Coat", "小雪怪皮毛外套")
                    .itemModel(ItemModelProvider::basicItem)
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ItemModelProvider::basicItem)
                    .build();
    /**
     * 蜜蜂头饰 - +4% 召唤伤害，+1 仆从栏
     */
    public static final DeferredItem<ArmorItem> BeeHeadgear =
            REGISTRIES.build("bee_headgear", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.BeeArmorMaterial, ArmorItem.Type.HELMET)
                            .modifier(Attributes.ARMOR, 2, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.SummonDamage, 0.04, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .modifier(LyraAttributeRegister.MinionMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Bee Headgear", "蜜蜂头饰")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ItemModelProvider::basicItem)
                    .build();
    /**
     * 蜜蜂胸甲 - +4% 召唤伤害，+1 仆从栏
     */
    public static final DeferredItem<ArmorItem> BeeChestplate =
            REGISTRIES.build("bee_chestplate", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.BeeArmorMaterial, ArmorItem.Type.CHESTPLATE)
                            .modifier(Attributes.ARMOR, 2, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.SummonDamage, 0.04, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .modifier(LyraAttributeRegister.MinionMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Bee Breastplate", "蜜蜂胸甲")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ItemModelProvider::basicItem)
                    .build();
    /**
     * 蜜蜂护胫 - +5% 召唤伤害
     */
    public static final DeferredItem<ArmorItem> BeeLeggings =
            REGISTRIES.build("bee_leggings", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.BeeArmorMaterial, ArmorItem.Type.LEGGINGS)
                            .modifier(Attributes.ARMOR, 2, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.SummonDamage, 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Bee Leggings", "蜜蜂护胫")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ItemModelProvider::basicItem)
                    .build();
    /**
     * 蜜蜂靴
     */
    public static final DeferredItem<ArmorItem> BeeBoots =
            REGISTRIES.build("bee_boots", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.BeeArmorMaterial, ArmorItem.Type.BOOTS)
                            .modifier(Attributes.ARMOR, 1, AttributeModifier.Operation.ADD_VALUE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Bee Boots", "蜜蜂靴")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ItemModelProvider::basicItem)
                    .build();
    /**
     * 黑曜石头盔 - +8% 召唤伤害
     */
    public static final DeferredItem<ArmorItem> ObsidianHelmet =
            REGISTRIES.build("obsidian_helmet", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.ObsidianArmorMaterial, ArmorItem.Type.HELMET)
                            .modifier(Attributes.ARMOR, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.SummonDamage, 0.08, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Obsidian Helmet", "黑曜石逃犯帽")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ItemModelProvider::basicItem)
                    .build();
    /**
     * 黑曜石胸甲 - +1 仆从栏
     */
    public static final DeferredItem<ArmorItem> ObsidianChestplate =
            REGISTRIES.build("obsidian_chestplate", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.ObsidianArmorMaterial, ArmorItem.Type.CHESTPLATE)
                            .modifier(Attributes.ARMOR, 2, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.MinionMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Obsidian Chestplate", "黑曜石风衣")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ItemModelProvider::basicItem)
                    .build();
    /**
     * 黑曜石护腿 - +8% 召唤伤害
     */
    public static final DeferredItem<ArmorItem> ObsidianLeggings =
            REGISTRIES.build("obsidian_leggings", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.ObsidianArmorMaterial, ArmorItem.Type.LEGGINGS)
                            .modifier(Attributes.ARMOR, 2, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.SummonDamage, 0.08, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Obsidian Leggings", "黑曜石裤")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ItemModelProvider::basicItem)
                    .build();
    /**
     * 黑曜石靴子 - +8% 移动速度
     */
    public static final DeferredItem<ArmorItem> ObsidianBoots =
            REGISTRIES.build("obsidian_boots", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.ObsidianArmorMaterial, ArmorItem.Type.BOOTS)
                            .modifier(Attributes.ARMOR, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(Attributes.MOVEMENT_SPEED, 0.08, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Obsidian Boots", "黑曜石靴")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ItemModelProvider::basicItem)
                    .build();
    /**
     * 蜘蛛面具 - +5% 召唤伤害，+1 仆从栏
     */
    public static final DeferredItem<ArmorItem> SpiderMask =
            REGISTRIES.build("spider_mask", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.SpiderArmorMaterial, ArmorItem.Type.HELMET)
                            .modifier(Attributes.ARMOR, 2, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.SummonDamage, 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .modifier(LyraAttributeRegister.MinionMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Spider Mask", "蜘蛛面具")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ItemModelProvider::basicItem)
                    .build();
    /**
     * 蜘蛛胸甲 - +5% 召唤伤害，+1 仆从栏
     */
    public static final DeferredItem<ArmorItem> SpiderChestplate =
            REGISTRIES.build("spider_chestplate", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.SpiderArmorMaterial, ArmorItem.Type.CHESTPLATE)
                            .modifier(Attributes.ARMOR, 3, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.SummonDamage, 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .modifier(LyraAttributeRegister.MinionMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Spider Breastplate", "蜘蛛胸甲")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ItemModelProvider::basicItem)
                    .build();
    /**
     * 蜘蛛护胫 - +6% 召唤伤害，+1 仆从栏
     */
    public static final DeferredItem<ArmorItem> SpiderLeggings =
            REGISTRIES.build("spider_leggings", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.SpiderArmorMaterial, ArmorItem.Type.LEGGINGS)
                            .modifier(Attributes.ARMOR, 3, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.SummonDamage, 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .modifier(LyraAttributeRegister.MinionMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Spider Greaves", "蜘蛛护胫")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ItemModelProvider::basicItem)
                    .build();
    /**
     * 蜘蛛靴
     */
    public static final DeferredItem<ArmorItem> SpiderBoots =
            REGISTRIES.build("spider_boots", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.SpiderArmorMaterial, ArmorItem.Type.BOOTS)
                            .modifier(Attributes.ARMOR, 2, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.SummonDamage, 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Spider Boots", "蜘蛛靴")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ItemModelProvider::basicItem)
                    .build();
    /**
     * 禁戒面具 - +15% 召唤伤害
     */
    public static final DeferredItem<ArmorItem> ForbiddenMask =
            REGISTRIES.build("forbidden_mask", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.ForbiddenArmorMaterial, ArmorItem.Type.HELMET)
                            .modifier(Attributes.ARMOR, 2, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.SummonDamage, 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Forbidden Mask", "禁戒面具")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ItemModelProvider::basicItem)
                    .build();
    /**
     * 禁戒长袍 - +10%召唤伤害，+1 仆从栏
     */
    public static final DeferredItem<ArmorItem> ForbiddenRobe =
            REGISTRIES.build("forbidden_robe", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.ForbiddenArmorMaterial, ArmorItem.Type.CHESTPLATE)
                            .modifier(Attributes.ARMOR, 4, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.SummonDamage, 0.10, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .modifier(LyraAttributeRegister.MinionMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Forbidden Robe", "禁戒长袍")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ItemModelProvider::basicItem)
                    .build();
    /**
     * 禁戒裤 - +1 仆从栏
     */
    public static final DeferredItem<ArmorItem> ForbiddenLeggings =
            REGISTRIES.build("forbidden_leggings", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.ForbiddenArmorMaterial, ArmorItem.Type.LEGGINGS)
                            .modifier(Attributes.ARMOR, 3, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.MinionMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Forbidden Leggings", "禁戒裤")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ItemModelProvider::basicItem)
                    .build();
    /**
     * 禁戒靴
     */
    public static final DeferredItem<ArmorItem> ForbiddenBoots =
            REGISTRIES.build("forbidden_boots", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.ForbiddenArmorMaterial, ArmorItem.Type.BOOTS)
                            .modifier(Attributes.ARMOR, 2, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.SummonDamage, 0.10, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Forbidden Boots", "禁戒战靴")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ItemModelProvider::basicItem)
                    .build();
    /**
     * 神圣头盔
     */
    public static final DeferredItem<ArmorItem> HallowedHelmet =
            REGISTRIES.build("hallowed_helmet", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.HallowedArmorMaterial, ArmorItem.Type.HELMET)
                            .modifier(Attributes.ARMOR, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.MinionMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.SummonDamage, 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Hallowed Helmet", "神圣兜帽")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ItemModelProvider::basicItem)
                    .build();
    /**
     * 神圣胸甲 - +14% 召唤伤害
     */
    public static final DeferredItem<ArmorItem> HallowedChestplate =
            REGISTRIES.build("hallowed_chestplate", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.HallowedArmorMaterial, ArmorItem.Type.CHESTPLATE)
                            .modifier(Attributes.ARMOR, 5, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.SummonDamage, 0.14, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Hallowed Chestplate", "神圣板甲")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ItemModelProvider::basicItem)
                    .build();
    /**
     * 神圣护腿 - +7% 召唤伤害
     */
    public static final DeferredItem<ArmorItem> HallowedLeggings =
            REGISTRIES.build("hallowed_leggings", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.HallowedArmorMaterial, ArmorItem.Type.LEGGINGS)
                            .modifier(Attributes.ARMOR, 4, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.SummonDamage, 0.07, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Hallowed Leggings", "神圣护胫")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ItemModelProvider::basicItem)
                    .build();
    /**
     * 神圣靴子 - +8% 移动速度
     */
    public static final DeferredItem<ArmorItem> HallowedBoots =
            REGISTRIES.build("hallowed_boots", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.HallowedArmorMaterial, ArmorItem.Type.BOOTS)
                            .modifier(Attributes.ARMOR, 3, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(Attributes.MOVEMENT_SPEED, 0.08, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Hallowed Boots", "神圣战靴")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ItemModelProvider::basicItem)
                    .build();
    /**
     * 叶绿面具 - +1 召唤栏，+10% 召唤伤害
     */
    public static final DeferredItem<ArmorItem> ChlorophyteHelmet =
            REGISTRIES.build("chlorophyte_helmet", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.ChlorophyteArmorMaterial, ArmorItem.Type.HELMET)
                            .modifier(Attributes.ARMOR, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.MinionMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.SummonDamage, 0.16, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Chlorophyte Mask", "叶绿面具")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ItemModelProvider::basicItem)
                    .build();
    /**
     * 叶绿板甲 - +6% 召唤伤害
     */
    public static final DeferredItem<ArmorItem> ChlorophyteChestplate =
            REGISTRIES.build("chlorophyte_chestplate", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.ChlorophyteArmorMaterial, ArmorItem.Type.CHESTPLATE)
                            .modifier(Attributes.ARMOR, 6, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.SummonDamage, 0.19, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Chlorophyte Breastplate", "叶绿板甲")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ItemModelProvider::basicItem)
                    .build();
    /**
     * 叶绿护胫 - +4% 召唤伤害
     */
    public static final DeferredItem<ArmorItem> ChlorophyteLeggings =
            REGISTRIES.build("chlorophyte_leggings", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.ChlorophyteArmorMaterial, ArmorItem.Type.LEGGINGS)
                            .modifier(Attributes.ARMOR, 5, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.SummonDamage, 0.16, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Chlorophyte Leggings", "叶绿护胫")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ItemModelProvider::basicItem)
                    .build();
    /**
     * 叶绿战靴 - +2% 召唤伤害
     */
    public static final DeferredItem<ArmorItem> ChlorophyteBoots =
            REGISTRIES.build("chlorophyte_boots", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.ChlorophyteArmorMaterial, ArmorItem.Type.BOOTS)
                            .modifier(Attributes.ARMOR, 3, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(Attributes.MOVEMENT_SPEED, 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Chlorophyte Boots", "叶绿战靴")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ItemModelProvider::basicItem)
                    .build();
    /**
     * 英灵殿骑士头盔 - +2 哨兵栏，+10% 召唤伤害，+10% 原版伤害
     */
    public static final DeferredItem<ArmorItem> ValhallaKnightHelmet =
            REGISTRIES.build("valhalla_knight_helmet", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.ValhallaKnightArmorMaterial, ArmorItem.Type.HELMET)
                            .modifier(Attributes.ARMOR, 7, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.SentryMaxCount, 2, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.SummonDamage, 0.10, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .modifier(Attributes.ATTACK_DAMAGE, 0.10, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Valhalla Knight Helmet", "英灵殿骑士头盔")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ItemModelProvider::basicItem)
                    .build();
    /**
     * 英灵殿骑士胸甲 - +30% 召唤伤害，+0.4 生命再生
     */
    public static final DeferredItem<ArmorItem> ValhallaKnightChestplate =
            REGISTRIES.build("valhalla_knight_chestplate", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.ValhallaKnightArmorMaterial, ArmorItem.Type.CHESTPLATE)
                            .modifier(Attributes.ARMOR, 8, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.SummonDamage, 0.30, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .modifier(LyraAttributeRegister.HealthRegen, 0.4, AttributeModifier.Operation.ADD_VALUE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Valhalla Knight Chestplate", "英灵殿骑士胸甲")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ItemModelProvider::basicItem)
                    .build();
    /**
     * 英灵殿骑士护腿 - +20% 召唤伤害，+20% 原版伤害
     */
    public static final DeferredItem<ArmorItem> ValhallaKnightLeggings =
            REGISTRIES.build("valhalla_knight_leggings", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.ValhallaKnightArmorMaterial, ArmorItem.Type.LEGGINGS)
                            .modifier(Attributes.ARMOR, 6, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.SummonDamage, 0.20, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .modifier(Attributes.ATTACK_DAMAGE, 0.20, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Valhalla Knight Leggings", "英灵殿骑士护腿")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ItemModelProvider::basicItem)
                    .build();
    /**
     * 英灵殿骑士战靴 - +20% 移动速度
     */
    public static final DeferredItem<ArmorItem> ValhallaKnightBoots =
            REGISTRIES.build("valhalla_knight_boots", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.ValhallaKnightArmorMaterial, ArmorItem.Type.BOOTS)
                            .modifier(Attributes.ARMOR, 4, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(Attributes.MOVEMENT_SPEED, 0.20, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Valhalla Knight Boots", "英灵殿骑士战靴")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ItemModelProvider::basicItem)
                    .build();
    /**
     * 提基面具 - +1 召唤栏，+10% 召唤伤害
     */
    public static final DeferredItem<ArmorItem> TikiHelmet =
            REGISTRIES.build("tiki_helmet", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.TikiArmorMaterial, ArmorItem.Type.HELMET)
                            .modifier(Attributes.ARMOR, 2, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.MinionMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.SummonDamage, 0.2, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Tiki Mask", "提基面具")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ItemModelProvider::basicItem)
                    .build();
    /**
     * 提基胸甲 - +10% 召唤伤害
     */
    public static final DeferredItem<ArmorItem> TikiChestplate =
            REGISTRIES.build("tiki_chestplate", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.TikiArmorMaterial, ArmorItem.Type.CHESTPLATE)
                            .modifier(Attributes.ARMOR, 6, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.MinionMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.SummonDamage, 0.10, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Tiki Chestplate", "提基胸甲")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ItemModelProvider::basicItem)
                    .build();
    /**
     * 提基护腿 - +7% 召唤伤害
     */
    public static final DeferredItem<ArmorItem> TikiLeggings =
            REGISTRIES.build("tiki_leggings", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.TikiArmorMaterial, ArmorItem.Type.LEGGINGS)
                            .modifier(Attributes.ARMOR, 4, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.MinionMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Tiki Leggings", "提基护腿")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ItemModelProvider::basicItem)
                    .build();
    /**
     * 提基战靴 - +3% 召唤伤害
     */
    public static final DeferredItem<ArmorItem> TikiBoots =
            REGISTRIES.build("tiki_boots", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.TikiArmorMaterial, ArmorItem.Type.BOOTS)
                            .modifier(Attributes.ARMOR, 3, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.SummonDamage, 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Tiki Boots", "提基战靴")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ItemModelProvider::basicItem)
                    .build();
    /**
     * 阴森头盔 - +11% 召唤伤害
     */
    public static final DeferredItem<ArmorItem> SpookyHelmet =
            REGISTRIES.build("spooky_helmet", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.SpookyArmorMaterial, ArmorItem.Type.HELMET)
                            .modifier(Attributes.ARMOR, 3, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.MinionMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.SummonDamage, 0.11, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Spooky Helmet", "阴森头盔")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ItemModelProvider::basicItem)
                    .build();
    /**
     * 阴森胸甲 - +11% 召唤伤害
     */
    public static final DeferredItem<ArmorItem> SpookyChestplate =
            REGISTRIES.build("spooky_chestplate", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.SpookyArmorMaterial, ArmorItem.Type.CHESTPLATE)
                            .modifier(Attributes.ARMOR, 4, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.MinionMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.SummonDamage, 0.11, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Spooky Chestplate", "阴森胸甲")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ItemModelProvider::basicItem)
                    .build();
    /**
     * 阴森护腿 - +8% 召唤伤害，+1 召唤栏
     */
    public static final DeferredItem<ArmorItem> SpookyLeggings =
            REGISTRIES.build("spooky_leggings", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.SpookyArmorMaterial, ArmorItem.Type.LEGGINGS)
                            .modifier(Attributes.ARMOR, 4, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.MinionMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.SummonDamage, 0.11, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Spooky Leggings", "阴森护腿")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ItemModelProvider::basicItem)
                    .build();
    /**
     * 阴森战靴 - +4% 召唤伤害
     */
    public static final DeferredItem<ArmorItem> SpookyBoots =
            REGISTRIES.build("spooky_boots", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.SpookyArmorMaterial, ArmorItem.Type.BOOTS)
                            .modifier(Attributes.ARMOR, 2, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(Attributes.MOVEMENT_SPEED, 0.2, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Spooky Boots", "阴森战靴")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ItemModelProvider::basicItem)
                    .build();
    /**
     * 星尘头盔 - +1 召唤栏，+16% 召唤伤害
     */
    public static final DeferredItem<ArmorItem> StardustHelmet =
            REGISTRIES.build("stardust_helmet", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.StardustArmorMaterial, ArmorItem.Type.HELMET)
                            .modifier(Attributes.ARMOR, 4, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.MinionMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.SentryMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.SummonDamage, 0.22, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .modifier(LyraAttributeRegister.SummonSearchRange, 0.08, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.EPIC))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Stardust Helmet", "星尘头盔")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ItemModelProvider::basicItem)
                    .build();
    /**
     * 星尘板甲 - +22% 召唤伤害
     */
    public static final DeferredItem<ArmorItem> StardustChestplate =
            REGISTRIES.build("stardust_chestplate", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.StardustArmorMaterial, ArmorItem.Type.CHESTPLATE)
                            .modifier(Attributes.ARMOR, 6, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.MinionMaxCount, 2, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.SummonDamage, 0.22, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .modifier(LyraAttributeRegister.SummonSearchRange, 0.08, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.EPIC))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Stardust Chestplate", "星尘板甲")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ItemModelProvider::basicItem)
                    .build();
    /**
     * 星尘护腿 - +15% 召唤伤害
     */
    public static final DeferredItem<ArmorItem> StardustLeggings =
            REGISTRIES.build("stardust_leggings", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.StardustArmorMaterial, ArmorItem.Type.LEGGINGS)
                            .modifier(Attributes.ARMOR, 4, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.MinionMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.SummonDamage, 0.22, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .modifier(LyraAttributeRegister.SummonSearchRange, 0.08, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.EPIC))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Stardust Leggings", "星尘护腿")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ItemModelProvider::basicItem)
                    .build();
    /**
     * 星尘战靴 - +7% 召唤伤害
     */
    public static final DeferredItem<ArmorItem> StardustBoots =
            REGISTRIES.build("stardust_boots", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.StardustArmorMaterial, ArmorItem.Type.BOOTS)
                            .modifier(Attributes.ARMOR, 3, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.MinionMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.SummonSearchRange, 0.08, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.EPIC))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Stardust Boots", "星尘战靴")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(ItemModelProvider::basicItem)
                    .build();

    public static void register() {
    }
}
