package first.summoner;

import first.lyra.register.LyraItemRegistries;
import first.summoner.register.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(Summoner.MODID)
public class Summoner {

    public static final String MODID = "summoner";
    public static final LyraItemRegistries REGISTRIES = LyraItemRegistries.create(MODID).languageInit(new SummonerLanguageRegister());

    public Summoner(IEventBus eventBus, ModContainer container) {
        REGISTRIES.register(eventBus, () -> {
            SummonerItemRegister.register();
            SummonerArmorRegister.register();
            SummonerCurioRegister.register();
            SummonerWeaponRegister.register();
        });
        SummonerCreativeTabRegister.register(eventBus);
        SummonerDataComponentRegister.register(eventBus);
        SummonerAttachmentEntityRegister.register(eventBus);
        SummonerArmorMaterialRegister.register(eventBus);
        SummonerEnchantmentRegister.register(eventBus);
        SummonerMobEffectRegister.register(eventBus);
        SummonerPotionRegister.register(eventBus);
        SummonerSoundRegister.register(eventBus);
        SummonerAttachmentRegister.register(eventBus);
        SummonerSummonMarkRegister.register(eventBus);
    }

    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path.toLowerCase());
    }
}
