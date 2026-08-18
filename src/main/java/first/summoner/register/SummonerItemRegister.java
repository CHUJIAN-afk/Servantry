package first.summoner.register;

import first.lyra.client.creativeTab.AnimBanner;
import first.lyra.common.creativeTab.Section;
import first.lyra.register.LyraItemRegisterBuilder;
import first.summoner.Summoner;
import first.summoner.common.item.ZenithItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tiers;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class SummonerItemRegister {

    /** 主 mod 物品注册表（宿主传入 LyraItemRegisterBuilder 使用）。 */
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Summoner.MODID);

    public static final Section MATERIAL = new Section(3, Summoner.rl("textures/item/banner/default_banner.png"), new AnimBanner(18, 1, 1), SummonerItemTagsRegister.SectionMaterial);

    public static final DeferredItem<ZenithItem> Zenith =
            LyraItemRegisterBuilder.build(ITEMS, "zenith", () -> new ZenithItem(Tiers.NETHERITE, new Item.Properties()
                            .rarity(Rarity.EPIC)
                            .stacksTo(1)))
                    .itemLanguage("Zenith", "天顶剑")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .build();

    public static final DeferredItem<Item> Silk =
            LyraItemRegisterBuilder.build(ITEMS, "silk")
                    .itemTag(SummonerItemTagsRegister.SectionMaterial)
                    .itemLanguage("Silk", "丝绸")
                    .itemModel(LyraItemRegisterBuilder::basicModel)
                    .build();
    public static final DeferredItem<Item> BlackLens =
            LyraItemRegisterBuilder.build(ITEMS, "black_lens")
                    .itemTag(SummonerItemTagsRegister.SectionMaterial)
                    .itemLanguage("Black Lens", "黑色晶状体")
                    .itemModel(LyraItemRegisterBuilder::basicModel)
                    .build();
    public static final DeferredItem<Item> BeeWax =
            LyraItemRegisterBuilder.build(ITEMS, "bee_wax")
                    .itemTag(SummonerItemTagsRegister.SectionMaterial)
                    .itemLanguage("Bee Wax", "蜂蜡")
                    .itemModel(LyraItemRegisterBuilder::basicModel)
                    .build();
    public static final DeferredItem<Item> ForbiddenFragment =
            LyraItemRegisterBuilder.build(ITEMS, "forbidden_fragment")
                    .itemTag(SummonerItemTagsRegister.SectionMaterial)
                    .itemLanguage("Forbidden Fragment", "禁戒碎片")
                    .itemModel(LyraItemRegisterBuilder::basicModel)
                    .build();
    public static final DeferredItem<Item> AdamantiteIngot =
            LyraItemRegisterBuilder.build(ITEMS, "adamantite_ingot")
                    .itemTag(SummonerItemTagsRegister.SectionMaterial)
                    .itemLanguage("Adamantite Ingot", "精金锭")
                    .itemTag(Tags.Items.INGOTS)
                    .itemModel(LyraItemRegisterBuilder::basicModel)
                    .build();
    public static final DeferredItem<Item> SpiderFang =
            LyraItemRegisterBuilder.build(ITEMS, "spider_fang")
                    .itemTag(SummonerItemTagsRegister.SectionMaterial)
                    .itemLanguage("Spider Fang", "蜘蛛牙")
                    .itemModel(LyraItemRegisterBuilder::basicModel)
                    .build();
    public static final DeferredItem<Item> HallowedIngot =
            LyraItemRegisterBuilder.build(ITEMS, "hallowed_ingot")
                    .itemTag(SummonerItemTagsRegister.SectionMaterial)
                    .itemLanguage("Hallowed Ingot", "神圣锭")
                    .itemTag(Tags.Items.INGOTS)
                    .itemModel(LyraItemRegisterBuilder::basicModel)
                    .build();
    public static final DeferredItem<Item> ChlorophyteIngot =
            LyraItemRegisterBuilder.build(ITEMS, "chlorophyte_ingot")
                    .itemTag(SummonerItemTagsRegister.SectionMaterial)
                    .itemLanguage("Chlorophyte Ingot", "叶绿锭")
                    .itemTag(Tags.Items.INGOTS)
                    .itemModel(LyraItemRegisterBuilder::basicModel)
                    .build();
    public static final DeferredItem<Item> Stardust =
            LyraItemRegisterBuilder.build(ITEMS, "stardust")
                    .itemTag(SummonerItemTagsRegister.SectionMaterial)
                    .itemLanguage("Stardust", "星尘")
                    .itemModel(LyraItemRegisterBuilder::basicModel)
                    .build();
    public static final DeferredItem<Item> LuminiteIngot =
            LyraItemRegisterBuilder.build(ITEMS, "luminite_ingot")
                    .itemTag(SummonerItemTagsRegister.SectionMaterial)
                    .itemLanguage("Luminite Ingot", "夜明锭")
                    .itemModel(LyraItemRegisterBuilder::basicModel)
                    .itemTag(Tags.Items.INGOTS)
                    .build();

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
