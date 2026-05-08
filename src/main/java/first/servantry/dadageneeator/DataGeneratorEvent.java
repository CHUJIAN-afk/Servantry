package first.servantry.dadageneeator;

import first.servantry.Servantry;
import first.servantry.dadageneeator.provider.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.LootTableProvider.SubProviderEntry;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = Servantry.MODID)
public class DataGeneratorEvent {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        // 语言
        generator.addProvider(event.includeServer(), new ServantryLanguageProvider(packOutput, Servantry.MODID, "en_us"));
        generator.addProvider(event.includeServer(), new ServantryLanguageProvider(packOutput, Servantry.MODID, "zh_cn"));
        // 物品模型
        generator.addProvider(event.includeClient(), new ServantryItemModelProvider(packOutput, existingFileHelper));
        // 战利品掉落表
        generator.addProvider(event.includeServer(), new LootTableProvider(packOutput, Set.of(), List.of(
                new SubProviderEntry(ServantryEntityLootProvider::new, LootContextParamSets.ENTITY)
        ), lookupProvider));
        // 合成表
        generator.addProvider(event.includeServer(), new ServantryRecipeProvider(packOutput, lookupProvider));
        // 饰品栏
        generator.addProvider(event.includeServer(), new ServantryCuriosDataProvider(packOutput, existingFileHelper, lookupProvider));
        // 方块标签
        ServantryBlockTagsProvider blockTagsProvider = new ServantryBlockTagsProvider(packOutput, lookupProvider, existingFileHelper);
        generator.addProvider(event.includeServer(), blockTagsProvider);
        // 物品标签
        generator.addProvider(event.includeServer(), new ServantryItemTagsProvider(packOutput, lookupProvider, blockTagsProvider.contentsGetter(), existingFileHelper));
    }

}
