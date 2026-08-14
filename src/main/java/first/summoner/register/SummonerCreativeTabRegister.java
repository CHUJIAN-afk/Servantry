package first.summoner.register;

import first.lyra.common.creativeTab.CreativeTabDispatcher;
import first.summoner.Summoner;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;


@EventBusSubscriber(modid = Summoner.MODID)
public class SummonerCreativeTabRegister {

    public static final DeferredRegister<CreativeModeTab> Register = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Summoner.MODID);
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> Tab = Register.register("tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("modid.summoner"))
            .icon(() -> SummonerWeaponRegister.TerraPrism.get().getDefaultInstance())
            .build());

    public static void register(IEventBus eventBus) {
        Register.register(eventBus);
    }

    @SubscribeEvent
    public static void registerTab(FMLCommonSetupEvent event) {
        CreativeTabDispatcher.register(Tab, SummonerWeaponRegister.MINION_WEAPON, SummonerArmorRegister.ARMOR, SummonerCurioRegister.ACCESSORY, SummonerItemRegister.MATERIAL, SummonerItemRegister.BLOCK);
    }
}
