package first.summoner.register;

import first.lyra.register.LyraItemRegistries;
import first.summoner.Summoner;
import first.summoner.common.item.ZenithItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tiers;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.registries.DeferredItem;

public class SummonerItemRegister {

    public static final LyraItemRegistries REGISTRIES = Summoner.REGISTRIES;

    public static final DeferredItem<ZenithItem> Zenith = REGISTRIES.build("zenith", () -> new ZenithItem(Tiers.NETHERITE, new Item.Properties()
                    .rarity(Rarity.EPIC)
                    .stacksTo(1)))
            .itemLanguage("Zenith", "天顶剑")
            .itemModel(ItemModelProvider::handheldItem)
            .build();

    public static void register() {
    }
}
