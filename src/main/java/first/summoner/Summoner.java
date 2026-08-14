package first.summoner;

import first.summoner.config.StartupConfig;
import first.summoner.register.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(Summoner.MODID)
public class Summoner {

    public static final String MODID = "summoner";

    public Summoner(IEventBus eventBus, ModContainer container) {
        SummonerArmorMaterialRegister.register(eventBus);
        SummonerCreativeTabRegister.register(eventBus);
        SummonerDataComponentRegister.register(eventBus);
        SummonerAttachmentEntityRegister.register(eventBus);
        SummonerItemRegister.register(eventBus);
        SummonerArmorRegister.register();
        SummonerCurioRegister.register();
        SummonerWeaponRegister.register();
        SummonerMobEffectRegister.register(eventBus);
        SummonerPotionRegister.register(eventBus);
        SummonerSoundRegister.register(eventBus);
        SummonerArmorSetRegister.register(eventBus);
        SummonerBlockRegister.register(eventBus);
        SummonerMenuRegister.register(eventBus);
        SummonerMithrilAnvilRecipeRegister.register(eventBus);
        SummonerNetworkPacketRegister.register(eventBus);
        container.registerConfig(ModConfig.Type.STARTUP, StartupConfig.Spec);
    }

    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path.toLowerCase());
    }
}
