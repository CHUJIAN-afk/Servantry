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

    private static final ServantryRegisters Register = ServantryRegisters.getInstance();
    public static final TabGroup MATERIAL = new TabGroup(3, Servantry.rl("textures/item/banner/default_banner.png"), new AnimInfo(18, 1, 1));
    public static final TabGroup SWORD = new TabGroup(4, Servantry.rl("textures/item/banner/default_banner.png"), new AnimInfo(18, 1, 1));
    public static final TabGroup BLOCK = new TabGroup(5, Servantry.rl("textures/item/banner/default_banner.png"), new AnimInfo(18, 1, 1));

    public static final DeferredItem<Item> Zenith =
            Register.register(SWORD, "zenith", () -> new Zenith(Tiers.NETHERITE, new Item.Properties()
                            .rarity(Rarity.EPIC)
                            .stacksTo(1)))
                    .language("Zenith", "天顶剑")
                    .build();
    public static final DeferredItem<Item> MithrilAnvil =
            Register.register(BLOCK, "mithril_anvil", () -> new BlockItem(ServantryBlockRegister.MITHRIL_ANVIL.get(), new Item.Properties()))
                    .language("Mithril Anvil", "秘银砧")
                    .blockLanguage("Mithril Anvil", "秘银砧")
                    .build();
    public static final DeferredItem<Item> Silk =
            Register.register(MATERIAL, "silk")
                    .language("Silk", "丝绸")
                    .build();
    public static final DeferredItem<Item> BlackLens =
            Register.register(MATERIAL, "black_lens")
                    .language("Black Lens", "黑色晶状体")
                    .build();
    public static final DeferredItem<Item> BeeWax =
            Register.register(MATERIAL, "bee_wax")
                    .language("Bee Wax", "蜂蜡")
                    .build();
    public static final DeferredItem<Item> ForbiddenFragment =
            Register.register(MATERIAL, "forbidden_fragment")
                    .language("Forbidden Fragment", "禁戒碎片")
                    .build();
    public static final DeferredItem<Item> AdamantiteIngot =
            Register.register(MATERIAL, "adamantite_ingot")
                    .language("Adamantite Ingot", "精金锭")
                    .build();
    public static final DeferredItem<Item> SpiderFang =
            Register.register(MATERIAL, "spider_fang")
                    .language("Spider Fang", "蜘蛛牙")
                    .build();
    public static final DeferredItem<Item> HallowedIngot =
            Register.register(MATERIAL, "hallowed_ingot")
                    .language("Hallowed Ingot", "神圣锭")
                    .build();
    public static final DeferredItem<Item> ChlorophyteIngot =
            Register.register(MATERIAL, "chlorophyte_ingot")
                    .language("Chlorophyte Ingot", "叶绿锭")
                    .build();
    public static final DeferredItem<Item> Stardust =
            Register.register(MATERIAL, "stardust")
                    .language("Stardust", "星尘")
                    .build();
    public static final DeferredItem<Item> LuminiteIngot =
            Register.register(MATERIAL, "luminite_ingot")
                    .language("Luminite Ingot", "夜明锭")
                    .build();

    public static void register() {
    }
}
