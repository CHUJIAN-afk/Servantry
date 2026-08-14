package first.summoner.client;

import com.mojang.datafixers.util.Either;
import first.summoner.Summoner;
import first.summoner.client.screen.MithrilAnvilGui;
import first.summoner.client.tooltip.ScabbardTooltipComponent;
import first.summoner.common.dataComponent.ScabbardContainer;
import first.summoner.register.SummonerDataComponentRegister;
import first.summoner.register.SummonerMenuRegister;
import first.summoner.register.SummonerWeaponRegister;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;

import java.util.List;

@EventBusSubscriber(modid = Summoner.MODID, value = Dist.CLIENT)
public class ClientEvent {

    @SubscribeEvent
    public static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(SummonerMenuRegister.MITHRIL_ANVIL.get(), MithrilAnvilGui.MithrilAnvilScreen::new);
    }

    @SubscribeEvent
    public static void registerTooltipFactories(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(ScabbardTooltipComponent.class, tooltipComponent -> tooltipComponent);
    }

    @SubscribeEvent
    public static void renderSoulItemTooltipHandler(RenderTooltipEvent.GatherComponents event) {
        ItemStack itemStack = event.getItemStack();
        if (itemStack.is(SummonerWeaponRegister.InfiniteScabbard.get())) {
            ScabbardContainer scabbard = itemStack.getOrDefault(SummonerDataComponentRegister.SCABBARD.get(), ScabbardContainer.EMPTY);
            if (!scabbard.isEmpty()) {
                List<Either<FormattedText, TooltipComponent>> tooltipElements = event.getTooltipElements();
                tooltipElements.add(1, Either.right(new ScabbardTooltipComponent(scabbard.itemStack())));
            }
        }
    }
}