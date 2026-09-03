package first.summoner.register;

import first.summoner.Summoner;
import first.summoner.common.summonMark.SummonMarkType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

@EventBusSubscriber(modid = Summoner.MODID)
public class SummonerRegistries {

    public static final ResourceKey<Registry<SummonMarkType>> SUMMON_MARK_TYPE_KEY = ResourceKey.createRegistryKey(Summoner.rl("summon_mark"));

    public static final Registry<SummonMarkType> SUMMON_MARK_TYPE = new RegistryBuilder<>(SUMMON_MARK_TYPE_KEY).sync(true).create();

    @SubscribeEvent
    public static void createRegistry(NewRegistryEvent event) {
        event.register(SUMMON_MARK_TYPE);
    }
}
