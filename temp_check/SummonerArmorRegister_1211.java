package first.summoner.register;

import first.lyra.client.creativeTab.AnimBanner;
import first.lyra.common.builder.AttributeArmorItemBuilder;
import first.lyra.common.creativeTab.Section;
import first.lyra.register.LyraAttributeRegister;
import first.lyra.register.LyraItemRegisterBuilder;
import first.summoner.Summoner;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.registries.DeferredItem;

import static first.summoner.register.SummonerItemRegister.ITEMS;

public class SummonerArmorRegister {

    public static final Section ARMOR = new Section(1, Summoner.rl("textures/item/banner/default_banner.png"), new AnimBanner(18, 1, 1), SummonerItemTagsRegister.SectionArmor);
    /**
     * 灏忛洩鎬濂?     */
    public static final DeferredItem<ArmorItem> FlinxFurCoat =
            LyraItemRegisterBuilder.build(ITEMS, "flinx_fur_coat", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.Flinx, ArmorItem.Type.CHESTPLATE)
                            .modifier(Attributes.ARMOR, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.MinionMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.SummonDamage, 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Flinx Fur Coat", "灏忛洩鎬毊姣涘濂?)
                    .itemModel(LyraItemRegisterBuilder::basicModel)
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(LyraItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 铚滆渹澶撮グ - +4% 鍙敜浼ゅ锛?1 浠嗕粠鏍?     */
    public static final DeferredItem<ArmorItem> BeeHeadgear =
            LyraItemRegisterBuilder.build(ITEMS, "bee_headgear", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.BeeArmorMaterial, ArmorItem.Type.HELMET)
                            .modifier(Attributes.ARMOR, 2, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.SummonDamage, 0.04, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .modifier(LyraAttributeRegister.MinionMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Bee Headgear", "铚滆渹澶撮グ")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(LyraItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 铚滆渹鑳哥敳 - +4% 鍙敜浼ゅ锛?1 浠嗕粠鏍?     */
    public static final DeferredItem<ArmorItem> BeeChestplate =
            LyraItemRegisterBuilder.build(ITEMS, "bee_chestplate", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.BeeArmorMaterial, ArmorItem.Type.CHESTPLATE)
                            .modifier(Attributes.ARMOR, 2, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.SummonDamage, 0.04, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .modifier(LyraAttributeRegister.MinionMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Bee Breastplate", "铚滆渹鑳哥敳")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(LyraItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 铚滆渹鎶よ儷 - +5% 鍙敜浼ゅ
     */
    public static final DeferredItem<ArmorItem> BeeLeggings =
            LyraItemRegisterBuilder.build(ITEMS, "bee_leggings", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.BeeArmorMaterial, ArmorItem.Type.LEGGINGS)
                            .modifier(Attributes.ARMOR, 2, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.SummonDamage, 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Bee Leggings", "铚滆渹鎶よ儷")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(LyraItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 铚滆渹闈?- 濂楄濂栧姳閮ㄥ垎
     */
    public static final DeferredItem<ArmorItem> BeeBoots =
            LyraItemRegisterBuilder.build(ITEMS, "bee_boots", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.BeeArmorMaterial, ArmorItem.Type.BOOTS)
                            .modifier(Attributes.ARMOR, 1, AttributeModifier.Operation.ADD_VALUE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Bee Boots", "铚滆渹闈?)
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(LyraItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 榛戞洔鐭冲ご鐩?- +8% 鍙敜浼ゅ
     */
    public static final DeferredItem<ArmorItem> ObsidianHelmet =
            LyraItemRegisterBuilder.build(ITEMS, "obsidian_helmet", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.ObsidianArmorMaterial, ArmorItem.Type.HELMET)
                            .modifier(Attributes.ARMOR, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.SummonDamage, 0.08, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Obsidian Helmet", "榛戞洔鐭抽€冪姱甯?)
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(LyraItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 榛戞洔鐭宠兏鐢?- +1 浠嗕粠鏍?     */
    public static final DeferredItem<ArmorItem> ObsidianChestplate =
            LyraItemRegisterBuilder.build(ITEMS, "obsidian_chestplate", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.ObsidianArmorMaterial, ArmorItem.Type.CHESTPLATE)
                            .modifier(Attributes.ARMOR, 2, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.MinionMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Obsidian Chestplate", "榛戞洔鐭抽琛?)
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(LyraItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 榛戞洔鐭虫姢鑵?- +8% 鍙敜浼ゅ
     */
    public static final DeferredItem<ArmorItem> ObsidianLeggings =
            LyraItemRegisterBuilder.build(ITEMS, "obsidian_leggings", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.ObsidianArmorMaterial, ArmorItem.Type.LEGGINGS)
                            .modifier(Attributes.ARMOR, 2, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.SummonDamage, 0.08, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Obsidian Leggings", "榛戞洔鐭宠￥")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(LyraItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 榛戞洔鐭抽澊瀛?- +8% 绉诲姩閫熷害
     */
    public static final DeferredItem<ArmorItem> ObsidianBoots =
            LyraItemRegisterBuilder.build(ITEMS, "obsidian_boots", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.ObsidianArmorMaterial, ArmorItem.Type.BOOTS)
                            .modifier(Attributes.ARMOR, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(Attributes.MOVEMENT_SPEED, 0.08, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Obsidian Boots", "榛戞洔鐭抽澊")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(LyraItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 铚樿洓闈㈠叿 - +5% 鍙敜浼ゅ锛?1 浠嗕粠鏍?     */
    public static final DeferredItem<ArmorItem> SpiderMask =
            LyraItemRegisterBuilder.build(ITEMS, "spider_mask", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.SpiderArmorMaterial, ArmorItem.Type.HELMET)
                            .modifier(Attributes.ARMOR, 2, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.SummonDamage, 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .modifier(LyraAttributeRegister.MinionMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Spider Mask", "铚樿洓闈㈠叿")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(LyraItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 铚樿洓鑳哥敳 - +5% 鍙敜浼ゅ锛?1 浠嗕粠鏍?     */
    public static final DeferredItem<ArmorItem> SpiderChestplate =
            LyraItemRegisterBuilder.build(ITEMS, "spider_chestplate", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.SpiderArmorMaterial, ArmorItem.Type.CHESTPLATE)
                            .modifier(Attributes.ARMOR, 3, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.SummonDamage, 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .modifier(LyraAttributeRegister.MinionMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Spider Breastplate", "铚樿洓鑳哥敳")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(LyraItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 铚樿洓鎶よ儷 - +6% 鍙敜浼ゅ锛?1 浠嗕粠鏍?     */
    public static final DeferredItem<ArmorItem> SpiderLeggings =
            LyraItemRegisterBuilder.build(ITEMS, "spider_leggings", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.SpiderArmorMaterial, ArmorItem.Type.LEGGINGS)
                            .modifier(Attributes.ARMOR, 3, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.SummonDamage, 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .modifier(LyraAttributeRegister.MinionMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Spider Greaves", "铚樿洓鎶よ儷")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(LyraItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 铚樿洓闈?     */
    public static final DeferredItem<ArmorItem> SpiderBoots =
            LyraItemRegisterBuilder.build(ITEMS, "spider_boots", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.SpiderArmorMaterial, ArmorItem.Type.BOOTS)
                            .modifier(Attributes.ARMOR, 2, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.SummonDamage, 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Spider Boots", "铚樿洓闈?)
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(LyraItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 绂佹垝闈㈠叿 - +15% 鍙敜浼ゅ
     */
    public static final DeferredItem<ArmorItem> ForbiddenMask =
            LyraItemRegisterBuilder.build(ITEMS, "forbidden_mask", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.ForbiddenArmorMaterial, ArmorItem.Type.HELMET)
                            .modifier(Attributes.ARMOR, 2, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.SummonDamage, 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Forbidden Mask", "绂佹垝闈㈠叿")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(LyraItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 绂佹垝闀胯 - +10%鍙敜浼ゅ锛?1 浠嗕粠鏍?     */
    public static final DeferredItem<ArmorItem> ForbiddenRobe =
            LyraItemRegisterBuilder.build(ITEMS, "forbidden_robe", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.ForbiddenArmorMaterial, ArmorItem.Type.CHESTPLATE)
                            .modifier(Attributes.ARMOR, 4, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.SummonDamage, 0.10, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .modifier(LyraAttributeRegister.MinionMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Forbidden Robe", "绂佹垝闀胯")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(LyraItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 绂佹垝瑁?- +1 浠嗕粠鏍?     */
    public static final DeferredItem<ArmorItem> ForbiddenLeggings =
            LyraItemRegisterBuilder.build(ITEMS, "forbidden_leggings", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.ForbiddenArmorMaterial, ArmorItem.Type.LEGGINGS)
                            .modifier(Attributes.ARMOR, 3, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.MinionMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Forbidden Leggings", "绂佹垝瑁?)
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(LyraItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 绂佹垝闈?     */
    public static final DeferredItem<ArmorItem> ForbiddenBoots =
            LyraItemRegisterBuilder.build(ITEMS, "forbidden_boots", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.ForbiddenArmorMaterial, ArmorItem.Type.BOOTS)
                            .modifier(Attributes.ARMOR, 2, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.SummonDamage, 0.10, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Forbidden Boots", "绂佹垝鎴橀澊")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(LyraItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 绁炲湥澶寸洈
     */
    public static final DeferredItem<ArmorItem> HallowedHelmet =
            LyraItemRegisterBuilder.build(ITEMS, "hallowed_helmet", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.HallowedArmorMaterial, ArmorItem.Type.HELMET)
                            .modifier(Attributes.ARMOR, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.MinionMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.SummonDamage, 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Hallowed Helmet", "绁炲湥鍏滃附")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(LyraItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 绁炲湥鑳哥敳 - +14% 鍙敜浼ゅ
     */
    public static final DeferredItem<ArmorItem> HallowedChestplate =
            LyraItemRegisterBuilder.build(ITEMS, "hallowed_chestplate", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.HallowedArmorMaterial, ArmorItem.Type.CHESTPLATE)
                            .modifier(Attributes.ARMOR, 5, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.SummonDamage, 0.14, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Hallowed Chestplate", "绁炲湥鏉跨敳")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(LyraItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 绁炲湥鎶よ吙 - +7% 鍙敜浼ゅ
     */
    public static final DeferredItem<ArmorItem> HallowedLeggings =
            LyraItemRegisterBuilder.build(ITEMS, "hallowed_leggings", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.HallowedArmorMaterial, ArmorItem.Type.LEGGINGS)
                            .modifier(Attributes.ARMOR, 4, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.SummonDamage, 0.07, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Hallowed Leggings", "绁炲湥鎶よ儷")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(LyraItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 绁炲湥闈村瓙 - +8% 绉诲姩閫熷害
     */
    public static final DeferredItem<ArmorItem> HallowedBoots =
            LyraItemRegisterBuilder.build(ITEMS, "hallowed_boots", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.HallowedArmorMaterial, ArmorItem.Type.BOOTS)
                            .modifier(Attributes.ARMOR, 3, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(Attributes.MOVEMENT_SPEED, 0.08, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Hallowed Boots", "绁炲湥鎴橀澊")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(LyraItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 鍙剁豢闈㈠叿 - +1 鍙敜鏍忥紝+10% 鍙敜浼ゅ
     */
    public static final DeferredItem<ArmorItem> ChlorophyteHelmet =
            LyraItemRegisterBuilder.build(ITEMS, "chlorophyte_helmet", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.ChlorophyteArmorMaterial, ArmorItem.Type.HELMET)
                            .modifier(Attributes.ARMOR, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.MinionMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.SummonDamage, 0.16, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Chlorophyte Mask", "鍙剁豢闈㈠叿")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(LyraItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 鍙剁豢鏉跨敳 - +6% 鍙敜浼ゅ
     */
    public static final DeferredItem<ArmorItem> ChlorophyteChestplate =
            LyraItemRegisterBuilder.build(ITEMS, "chlorophyte_chestplate", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.ChlorophyteArmorMaterial, ArmorItem.Type.CHESTPLATE)
                            .modifier(Attributes.ARMOR, 6, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.SummonDamage, 0.19, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Chlorophyte Breastplate", "鍙剁豢鏉跨敳")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(LyraItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 鍙剁豢鎶よ儷 - +4% 鍙敜浼ゅ
     */
    public static final DeferredItem<ArmorItem> ChlorophyteLeggings =
            LyraItemRegisterBuilder.build(ITEMS, "chlorophyte_leggings", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.ChlorophyteArmorMaterial, ArmorItem.Type.LEGGINGS)
                            .modifier(Attributes.ARMOR, 5, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.SummonDamage, 0.16, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Chlorophyte Leggings", "鍙剁豢鎶よ儷")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(LyraItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 鍙剁豢鎴橀澊 - +2% 鍙敜浼ゅ
     */
    public static final DeferredItem<ArmorItem> ChlorophyteBoots =
            LyraItemRegisterBuilder.build(ITEMS, "chlorophyte_boots", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.ChlorophyteArmorMaterial, ArmorItem.Type.BOOTS)
                            .modifier(Attributes.ARMOR, 3, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(Attributes.MOVEMENT_SPEED, 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.UNCOMMON))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Chlorophyte Boots", "鍙剁豢鎴橀澊")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(LyraItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 鑻辩伒娈块獞澹ご鐩?- +2 鍝ㄥ叺鏍忥紝+10% 鍙敜浼ゅ锛?10% 鍘熺増浼ゅ
     */
    public static final DeferredItem<ArmorItem> ValhallaKnightHelmet =
            LyraItemRegisterBuilder.build(ITEMS, "valhalla_knight_helmet", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.ValhallaKnightArmorMaterial, ArmorItem.Type.HELMET)
                            .modifier(Attributes.ARMOR, 7, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.SentryMaxCount, 2, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.SummonDamage, 0.10, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .modifier(Attributes.ATTACK_DAMAGE, 0.10, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Valhalla Knight Helmet", "鑻辩伒娈块獞澹ご鐩?)
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(LyraItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 鑻辩伒娈块獞澹兏鐢?- +30% 鍙敜浼ゅ锛?0.4 鐢熷懡鍐嶇敓
     */
    public static final DeferredItem<ArmorItem> ValhallaKnightChestplate =
            LyraItemRegisterBuilder.build(ITEMS, "valhalla_knight_chestplate", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.ValhallaKnightArmorMaterial, ArmorItem.Type.CHESTPLATE)
                            .modifier(Attributes.ARMOR, 8, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.SummonDamage, 0.30, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .modifier(LyraAttributeRegister.HealthRegen, 0.4, AttributeModifier.Operation.ADD_VALUE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Valhalla Knight Chestplate", "鑻辩伒娈块獞澹兏鐢?)
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(LyraItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 鑻辩伒娈块獞澹姢鑵?- +20% 鍙敜浼ゅ锛?20% 鍘熺増浼ゅ
     */
    public static final DeferredItem<ArmorItem> ValhallaKnightLeggings =
            LyraItemRegisterBuilder.build(ITEMS, "valhalla_knight_leggings", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.ValhallaKnightArmorMaterial, ArmorItem.Type.LEGGINGS)
                            .modifier(Attributes.ARMOR, 6, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.SummonDamage, 0.20, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .modifier(Attributes.ATTACK_DAMAGE, 0.20, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Valhalla Knight Leggings", "鑻辩伒娈块獞澹姢鑵?)
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(LyraItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 鑻辩伒娈块獞澹垬闈?- +20% 绉诲姩閫熷害
     */
    public static final DeferredItem<ArmorItem> ValhallaKnightBoots =
            LyraItemRegisterBuilder.build(ITEMS, "valhalla_knight_boots", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.ValhallaKnightArmorMaterial, ArmorItem.Type.BOOTS)
                            .modifier(Attributes.ARMOR, 4, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(Attributes.MOVEMENT_SPEED, 0.20, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Valhalla Knight Boots", "鑻辩伒娈块獞澹垬闈?)
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(LyraItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 鎻愬熀闈㈠叿 - +1 鍙敜鏍忥紝+10% 鍙敜浼ゅ
     */
    public static final DeferredItem<ArmorItem> TikiHelmet =
            LyraItemRegisterBuilder.build(ITEMS, "tiki_helmet", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.TikiArmorMaterial, ArmorItem.Type.HELMET)
                            .modifier(Attributes.ARMOR, 2, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.MinionMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.SummonDamage, 0.2, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Tiki Mask", "鎻愬熀闈㈠叿")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(LyraItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 鎻愬熀鑳哥敳 - +10% 鍙敜浼ゅ
     */
    public static final DeferredItem<ArmorItem> TikiChestplate =
            LyraItemRegisterBuilder.build(ITEMS, "tiki_chestplate", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.TikiArmorMaterial, ArmorItem.Type.CHESTPLATE)
                            .modifier(Attributes.ARMOR, 6, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.MinionMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.SummonDamage, 0.10, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Tiki Chestplate", "鎻愬熀鑳哥敳")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(LyraItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 鎻愬熀鎶よ吙 - +7% 鍙敜浼ゅ
     */
    public static final DeferredItem<ArmorItem> TikiLeggings =
            LyraItemRegisterBuilder.build(ITEMS, "tiki_leggings", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.TikiArmorMaterial, ArmorItem.Type.LEGGINGS)
                            .modifier(Attributes.ARMOR, 4, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.MinionMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Tiki Leggings", "鎻愬熀鎶よ吙")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(LyraItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 鎻愬熀鎴橀澊 - +3% 鍙敜浼ゅ
     */
    public static final DeferredItem<ArmorItem> TikiBoots =
            LyraItemRegisterBuilder.build(ITEMS, "tiki_boots", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.TikiArmorMaterial, ArmorItem.Type.BOOTS)
                            .modifier(Attributes.ARMOR, 3, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.SummonDamage, 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Tiki Boots", "鎻愬熀鎴橀澊")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(LyraItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 闃存．澶寸洈 - +11% 鍙敜浼ゅ
     */
    public static final DeferredItem<ArmorItem> SpookyHelmet =
            LyraItemRegisterBuilder.build(ITEMS, "spooky_helmet", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.SpookyArmorMaterial, ArmorItem.Type.HELMET)
                            .modifier(Attributes.ARMOR, 3, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.MinionMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.SummonDamage, 0.11, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Spooky Helmet", "闃存．澶寸洈")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(LyraItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 闃存．鑳哥敳 - +11% 鍙敜浼ゅ
     */
    public static final DeferredItem<ArmorItem> SpookyChestplate =
            LyraItemRegisterBuilder.build(ITEMS, "spooky_chestplate", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.SpookyArmorMaterial, ArmorItem.Type.CHESTPLATE)
                            .modifier(Attributes.ARMOR, 4, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.MinionMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.SummonDamage, 0.11, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Spooky Chestplate", "闃存．鑳哥敳")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(LyraItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 闃存．鎶よ吙 - +8% 鍙敜浼ゅ锛?1 鍙敜鏍?     */
    public static final DeferredItem<ArmorItem> SpookyLeggings =
            LyraItemRegisterBuilder.build(ITEMS, "spooky_leggings", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.SpookyArmorMaterial, ArmorItem.Type.LEGGINGS)
                            .modifier(Attributes.ARMOR, 4, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.MinionMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.SummonDamage, 0.11, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Spooky Leggings", "闃存．鎶よ吙")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(LyraItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 闃存．鎴橀澊 - +4% 鍙敜浼ゅ
     */
    public static final DeferredItem<ArmorItem> SpookyBoots =
            LyraItemRegisterBuilder.build(ITEMS, "spooky_boots", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.SpookyArmorMaterial, ArmorItem.Type.BOOTS)
                            .modifier(Attributes.ARMOR, 2, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(Attributes.MOVEMENT_SPEED, 0.2, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.RARE))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Spooky Boots", "闃存．鎴橀澊")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(LyraItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 鏄熷皹澶寸洈 - +1 鍙敜鏍忥紝+16% 鍙敜浼ゅ
     */
    public static final DeferredItem<ArmorItem> StardustHelmet =
            LyraItemRegisterBuilder.build(ITEMS, "stardust_helmet", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.StardustArmorMaterial, ArmorItem.Type.HELMET)
                            .modifier(Attributes.ARMOR, 4, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.MinionMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.SentryMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.SummonDamage, 0.22, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .modifier(LyraAttributeRegister.SummonSearchRange, 0.08, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.EPIC))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Stardust Helmet", "鏄熷皹澶寸洈")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(LyraItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 鏄熷皹鏉跨敳 - +22% 鍙敜浼ゅ
     */
    public static final DeferredItem<ArmorItem> StardustChestplate =
            LyraItemRegisterBuilder.build(ITEMS, "stardust_chestplate", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.StardustArmorMaterial, ArmorItem.Type.CHESTPLATE)
                            .modifier(Attributes.ARMOR, 6, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.MinionMaxCount, 2, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.SummonDamage, 0.22, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .modifier(LyraAttributeRegister.SummonSearchRange, 0.08, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.EPIC))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Stardust Chestplate", "鏄熷皹鏉跨敳")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(LyraItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 鏄熷皹鎶よ吙 - +15% 鍙敜浼ゅ
     */
    public static final DeferredItem<ArmorItem> StardustLeggings =
            LyraItemRegisterBuilder.build(ITEMS, "stardust_leggings", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.StardustArmorMaterial, ArmorItem.Type.LEGGINGS)
                            .modifier(Attributes.ARMOR, 4, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.MinionMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.SummonDamage, 0.22, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .modifier(LyraAttributeRegister.SummonSearchRange, 0.08, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.EPIC))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Stardust Leggings", "鏄熷皹鎶よ吙")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(LyraItemRegisterBuilder::basicModel)
                    .build();
    /**
     * 鏄熷皹鎴橀澊 - +7% 鍙敜浼ゅ
     */
    public static final DeferredItem<ArmorItem> StardustBoots =
            LyraItemRegisterBuilder.build(ITEMS, "stardust_boots", () -> AttributeArmorItemBuilder.builder(SummonerArmorMaterialRegister.StardustArmorMaterial, ArmorItem.Type.BOOTS)
                            .modifier(Attributes.ARMOR, 3, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.MinionMaxCount, 1, AttributeModifier.Operation.ADD_VALUE)
                            .modifier(LyraAttributeRegister.SummonSearchRange, 0.08, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                            .properties(p -> p.rarity(Rarity.EPIC))
                            .build())
                    .itemTag(SummonerItemTagsRegister.SectionArmor)
                    .itemLanguage("Stardust Boots", "鏄熷皹鎴橀澊")
                    .itemTag(Tags.Items.ENCHANTABLES)
                    .itemTag(Tags.Items.ARMORS)
                    .itemTag(ItemTags.ARMOR_ENCHANTABLE)
                    .itemModel(LyraItemRegisterBuilder::basicModel)
                    .build();

    public static void register() {
    }
}
