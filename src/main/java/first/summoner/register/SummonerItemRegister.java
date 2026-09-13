package first.summoner.register;

import first.lyra.register.LyraItemRegistries;
import first.summoner.Summoner;
import first.summoner.common.item.DoomsdayStarDebugItem;
import first.summoner.common.item.ZenithItem;
import net.minecraft.resources.ResourceLocation;
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

    public static final DeferredItem<DoomsdayStarDebugItem> DoomsdayStarDebug =
            REGISTRIES.build("doomsday_star_debug", () -> new DoomsdayStarDebugItem(new Item.Properties()
                            .stacksTo(1)))
                    .itemLanguage("Doomsday Star Debugger", "末世星调试器")
                    .itemLanguageTooltip(1, "Right-click a block to summon a Doomsday Star", "右键方块召唤末世星")
                    .itemLanguageTooltip(2, "Sneak-right-click a block to remove all Doomsday Stars", "潜行右键方块清除所有末世星")
                    .itemTag(SummonerItemTagsRegister.SectionMinionWeaponTool)
                    .itemModel((provider, item) -> provider
                            .withExistingParent(item.getPath(), ResourceLocation.withDefaultNamespace("item/handheld"))
                            .texture("layer0", Summoner.rl("item/entity/test_boss")))
                    .build();

    public static void register() {
    }
}
