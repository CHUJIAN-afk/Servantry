package first.summoner.register;

import first.lyra.register.LyraItemRegisterBuilder;
import first.summoner.Summoner;
import first.summoner.common.item.ZenithItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tiers;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class SummonerItemRegister {

    /** 主 mod 物品注册表（宿主传入 LyraItemRegisterBuilder 使用）。 */
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Summoner.MODID);

    public static final DeferredItem<ZenithItem> Zenith =
            LyraItemRegisterBuilder.build(ITEMS, "zenith", id -> new ZenithItem(Tiers.NETHERITE, new Item.Properties()
                            .rarity(Rarity.EPIC)
                            .stacksTo(1)))
                    .itemLanguage("Zenith", "天顶剑")
                    .itemModel(LyraItemRegisterBuilder::handheldItem)
                    .build();

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
