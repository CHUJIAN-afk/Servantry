package first.summoner;

import first.summoner.register.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(Summoner.MODID)
public class Summoner {

    public static final String MODID = "summoner";

    public Summoner(IEventBus eventBus, ModContainer container) {
        SummonerCreativeTabRegister.register(eventBus);
        SummonerDataComponentRegister.register(eventBus);
        SummonerAttachmentEntityRegister.register(eventBus);
        SummonerItemRegister.register(eventBus);
        SummonerArmorMaterialRegister.register(eventBus);
        SummonerArmorRegister.register();
        SummonerCurioRegister.register();
        SummonerWeaponRegister.register();
        SummonerEnchantmentRegister.register(eventBus);
        SummonerMobEffectRegister.register(eventBus);
        SummonerPotionRegister.register(eventBus);
        SummonerSoundRegister.register(eventBus);
        SummonerArmorSetRegister.register(eventBus);
        SummonerAttachmentRegister.register(eventBus);
        SummonerSummonMarkRegister.register(eventBus);
    }

    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path.toLowerCase());
    }
}
