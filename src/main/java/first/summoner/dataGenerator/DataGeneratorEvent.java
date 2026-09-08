package first.summoner.dataGenerator;

import first.summoner.Summoner;
import first.summoner.dataGenerator.provider.SummonerCuriosDataProvider;
import first.summoner.dataGenerator.provider.SummonerEnchantmentTagsProvider;
import first.summoner.register.SummonerEnchantmentRegister;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = Summoner.MODID)
public class DataGeneratorEvent {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        DatapackBuiltinEntriesProvider enchantmentEntriesProvider = new DatapackBuiltinEntriesProvider(packOutput, lookupProvider, new RegistrySetBuilder().add(Registries.ENCHANTMENT, SummonerEnchantmentRegister::bootstrap), Set.of(Summoner.MODID));
        generator.addProvider(event.includeServer(), enchantmentEntriesProvider);
        CompletableFuture<HolderLookup.Provider> registryProvider = enchantmentEntriesProvider.getRegistryProvider();
        generator.addProvider(event.includeServer(), new SummonerEnchantmentTagsProvider(packOutput, registryProvider, Summoner.MODID, existingFileHelper));
        generator.addProvider(event.includeServer(), new SummonerCuriosDataProvider(packOutput, existingFileHelper, lookupProvider));
    }
}
