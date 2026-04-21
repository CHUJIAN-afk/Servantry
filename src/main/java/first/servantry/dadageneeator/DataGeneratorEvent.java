package first.servantry.dadageneeator;

import first.servantry.Servantry;
import first.servantry.api.item.IServantWeapon;
import first.servantry.dadageneeator.provider.SoulEntityLootProvider;
import first.servantry.dadageneeator.provider.SoulLangProvider;
import first.servantry.dadageneeator.provider.SoulRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Collections;
import java.util.List;
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
        generator.addProvider(event.includeServer(), new SoulLangProvider(packOutput, Servantry.MODID, "en_us"));
        generator.addProvider(event.includeServer(), new SoulLangProvider(packOutput, Servantry.MODID, "zh_cn"));

        // 物品模型
        generator.addProvider(event.includeClient(), new ItemModelProvider(packOutput, Servantry.MODID, existingFileHelper) {
            @Override
            protected void registerModels() {
                //handheldItem(ItemRegister.EnchantedThrowingKnives.get());
                BuiltInRegistries.ITEM.stream()
                        .filter(item -> item instanceof IServantWeapon<?>)
                        .forEach(this::handheldItem);
            }
        });

        // 战利品掉落表 (使用完美兼容的原版 LootTable 接口)
        generator.addProvider(event.includeServer(), new LootTableProvider(packOutput, Collections.emptySet(), List.of(new LootTableProvider.SubProviderEntry(SoulEntityLootProvider::new, LootContextParamSets.ENTITY)), lookupProvider));
        // 合成表
        generator.addProvider(event.includeServer(), new SoulRecipeProvider(packOutput, lookupProvider));
    }
}