package first.servantry.register;

import first.servantry.Servantry;
import first.servantry.client.creativeTab.AnimInfo;
import first.servantry.common.item.Zenith;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tiers;
import net.neoforged.neoforge.registries.DeferredItem;

public class ServantryItemRegister {

    public static final TabGroup MATERIAL = new TabGroup(3, Servantry.rl("textures/item/banner/default_banner.png"), new AnimInfo(18, 1, 1));
    public static final TabGroup SWORD = new TabGroup(4, Servantry.rl("textures/item/banner/default_banner.png"), new AnimInfo(18, 1, 1));
    public static final TabGroup BLOCK = new TabGroup(5, Servantry.rl("textures/item/banner/default_banner.png"), new AnimInfo(18, 1, 1));

    public static final DeferredItem<Zenith> Zenith =
            ServantryItemRegisterBuilder.build(SWORD, "zenith", () -> new Zenith(Tiers.NETHERITE, new Item.Properties()
                            .rarity(Rarity.EPIC)
                            .stacksTo(1)))
                    .itemLanguage("Zenith", "天顶剑")
                    .build();
    public static final DeferredItem<BlockItem> MithrilAnvil =
            ServantryItemRegisterBuilder.build(BLOCK, "mithril_anvil", () -> new BlockItem(ServantryBlockRegister.MITHRIL_ANVIL.get(), new Item.Properties()))
                    .itemLanguage("Mithril Anvil", "秘银砧")
                    .blockLanguage("Mithril Anvil", "秘银砧")
                    .build();
    public static final DeferredItem<Item> Silk =
            ServantryItemRegisterBuilder.build(MATERIAL, "silk")
                    .itemLanguage("Silk", "丝绸")
                    .build();
    public static final DeferredItem<Item> BlackLens =
            ServantryItemRegisterBuilder.build(MATERIAL, "black_lens")
                    .itemLanguage("Black Lens", "黑色晶状体")
                    .build();
    public static final DeferredItem<Item> BeeWax =
            ServantryItemRegisterBuilder.build(MATERIAL, "bee_wax")
                    .itemLanguage("Bee Wax", "蜂蜡")
                    .build();
    public static final DeferredItem<Item> ForbiddenFragment =
            ServantryItemRegisterBuilder.build(MATERIAL, "forbidden_fragment")
                    .itemLanguage("Forbidden Fragment", "禁戒碎片")
                    .build();
    public static final DeferredItem<Item> AdamantiteIngot =
            ServantryItemRegisterBuilder.build(MATERIAL, "adamantite_ingot")
                    .itemLanguage("Adamantite Ingot", "精金锭")
                    .build();
    public static final DeferredItem<Item> SpiderFang =
            ServantryItemRegisterBuilder.build(MATERIAL, "spider_fang")
                    .itemLanguage("Spider Fang", "蜘蛛牙")
                    .build();
    public static final DeferredItem<Item> HallowedIngot =
            ServantryItemRegisterBuilder.build(MATERIAL, "hallowed_ingot")
                    .itemLanguage("Hallowed Ingot", "神圣锭")
                    .build();
    public static final DeferredItem<Item> ChlorophyteIngot =
            ServantryItemRegisterBuilder.build(MATERIAL, "chlorophyte_ingot")
                    .itemLanguage("Chlorophyte Ingot", "叶绿锭")
                    .build();
    public static final DeferredItem<Item> Stardust =
            ServantryItemRegisterBuilder.build(MATERIAL, "stardust")
                    .itemLanguage("Stardust", "星尘")
                    .build();
    public static final DeferredItem<Item> LuminiteIngot =
            ServantryItemRegisterBuilder.build(MATERIAL, "luminite_ingot")
                    .itemLanguage("Luminite Ingot", "夜明锭")
                    .build();

    public static void register() {
    }
}
