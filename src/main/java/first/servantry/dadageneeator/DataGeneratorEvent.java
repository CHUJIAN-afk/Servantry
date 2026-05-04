package first.servantry.dadageneeator;

import first.servantry.Servantry;
import first.servantry.api.item.IServantWeapon;
import first.servantry.dadageneeator.provider.ServantryLangProvider;
import first.servantry.dadageneeator.provider.ServantryRecipeProvider;
import first.servantry.register.ItemRegister;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

@EventBusSubscriber(modid = Servantry.MODID)
public class DataGeneratorEvent {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        // 语言
        generator.addProvider(event.includeServer(), new ServantryLangProvider(packOutput, Servantry.MODID, "en_us"));
        generator.addProvider(event.includeServer(), new ServantryLangProvider(packOutput, Servantry.MODID, "zh_cn"));
        // 物品模型
        generator.addProvider(event.includeClient(), new ItemModelProvider(packOutput, Servantry.MODID, existingFileHelper) {
            @Override
            protected void registerModels() {
                basicItem(ItemRegister.HallowedHelmet.get());
                basicItem(ItemRegister.HallowedChestplate.get());
                basicItem(ItemRegister.HallowedLeggings.get());
                basicItem(ItemRegister.HallowedBoots.get());
                BuiltInRegistries.ITEM.stream()
                        .filter(item -> item instanceof IServantWeapon<?>)
                        .forEach(this::handheldItem);
            }
        });
        // 战利品掉落表
        generator.addProvider(event.includeServer(), new LootTableProvider(packOutput, Collections.emptySet(), List.of(new LootTableProvider.SubProviderEntry(provider -> new EntityLootSubProvider(FeatureFlags.REGISTRY.allFlags(), provider) {
            @Override
            public void generate() {
                this.add(EntityType.ALLAY, LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(0.01F))
                                .add(LootItem.lootTableItem(ItemRegister.TerraPrism.get())
                                        .when(LootItemRandomChanceCondition.randomChance(1))
                                )
                        )
                );
            }

            @Override
            protected @NotNull Stream<EntityType<?>> getKnownEntityTypes() {
                return Stream.of(EntityType.ALLAY);
            }
        }, LootContextParamSets.ENTITY)), lookupProvider));
        // 合成表
        generator.addProvider(event.includeServer(), new ServantryRecipeProvider(packOutput, lookupProvider));
    }

}