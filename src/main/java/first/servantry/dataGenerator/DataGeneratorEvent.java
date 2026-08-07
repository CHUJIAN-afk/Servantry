package first.servantry.dataGenerator;

import first.lyra.dataGenerator.provider.LyraBlockTagsProvider;
import first.lyra.dataGenerator.provider.LyraItemModelProvider;
import first.lyra.dataGenerator.provider.LyraItemTagsProvider;
import first.lyra.dataGenerator.provider.LyraRecipeProvider;
import first.servantry.Servantry;
import first.servantry.dataGenerator.provider.ServantryCuriosDataProvider;
import first.servantry.dataGenerator.provider.ServantryLanguageProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = Servantry.MODID)
public class DataGeneratorEvent {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        // 语言(ServantryLanguageGenerateRegister 主 mod 键 + LyraLanguageRegister 物品键)
        generator.addProvider(event.includeClient(), new ServantryLanguageProvider(packOutput, Servantry.MODID, "en_us"));
        generator.addProvider(event.includeClient(), new ServantryLanguageProvider(packOutput, Servantry.MODID, "zh_cn"));
        // 物品模型(LyraItemRegisterBuilder.itemModel 收集,输出到 servantry 命名空间)
        generator.addProvider(event.includeClient(), new LyraItemModelProvider(packOutput, existingFileHelper, Servantry.MODID));
        // 合成表(LyraItemRegisterBuilder.recipe 收集)
        generator.addProvider(event.includeServer(), new LyraRecipeProvider(packOutput, lookupProvider));
        // 饰品栏
        generator.addProvider(event.includeServer(), new ServantryCuriosDataProvider(packOutput, existingFileHelper, lookupProvider));
        // 方块标签(空,仅作物品标签依赖)
        LyraBlockTagsProvider blockTagsProvider = new LyraBlockTagsProvider(packOutput, lookupProvider, existingFileHelper, Servantry.MODID);
        generator.addProvider(event.includeServer(), blockTagsProvider);
        // 物品标签(LyraItemRegisterBuilder.itemTag 收集,输出到 servantry 命名空间)
        generator.addProvider(event.includeServer(), new LyraItemTagsProvider(packOutput, lookupProvider, blockTagsProvider.contentsGetter(), existingFileHelper, Servantry.MODID));
    }

}
