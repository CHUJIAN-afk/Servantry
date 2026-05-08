package first.servantry.dadageneeator;

import first.servantry.Servantry;
import first.servantry.api.item.IServantWeapon;
import first.servantry.common.item.CurioItem;
import first.servantry.dadageneeator.provider.ServantryLangProvider;
import first.servantry.dadageneeator.provider.ServantryRecipeProvider;
import first.servantry.register.ItemRegister;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosDataProvider;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

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
                BuiltInRegistries.ITEM.stream()
                        .filter(item -> BuiltInRegistries.ITEM.getKey(item).getNamespace().equals(Servantry.MODID))
                        .forEach(item -> {
                            switch (item) {
                                case ICurioItem ignored -> basicItem(item);
                                case IServantWeapon<?> ignored -> handheldItem(item);
                                case ArmorItem ignored -> basicItem(item);
                                default -> {
                                }
                            }
                        });
            }
        });
        // 战利品掉落表
        generator.addProvider(event.includeServer(), new LootTableProvider(packOutput, Collections.emptySet(), List.of(new LootTableProvider.SubProviderEntry(provider -> new EntityLootSubProvider(FeatureFlags.REGISTRY.allFlags(), provider) {

            @Override
            public void generate() {
                this.add(EntityType.ALLAY, LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1f))
                                .add(LootItem.lootTableItem(ItemRegister.TerraPrism.get())
                                        .when(LootItemRandomChanceCondition.randomChance(0.01f))
                                )
                        )
                );
                this.add(EntityType.EVOKER, LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1f))
                                .add(LootItem.lootTableItem(ItemRegister.SummonerEmblem.get())
                                        .when(LootItemRandomChanceCondition.randomChance(0.1f))
                                )
                        )
                );
            }

            @Override
            protected @NotNull Stream<EntityType<?>> getKnownEntityTypes() {
                return Stream.of(EntityType.ALLAY, EntityType.EVOKER);
            }

        }, LootContextParamSets.ENTITY)), lookupProvider));
        // 合成表
        generator.addProvider(event.includeServer(), new ServantryRecipeProvider(packOutput, lookupProvider));
        // 饰品栏
        generator.addProvider(event.includeServer(), new CuriosDataProvider(Servantry.MODID, packOutput, existingFileHelper, lookupProvider) {
            @Override
            public void generate(HolderLookup.Provider registries, ExistingFileHelper fileHelper) {
                this.createEntities("curio")
                        .addPlayer()
                        .addSlots("curio");
            }
        });
        // 方块标签
        BlockTagsProvider blockTagsProvider = new BlockTagsProvider(packOutput, lookupProvider, Servantry.MODID, existingFileHelper) {
            @Override
            protected void addTags(HolderLookup.@NotNull Provider provider) {

            }
        };
        generator.addProvider(event.includeServer(), blockTagsProvider);
        // 物品标签
        generator.addProvider(event.includeServer(), new ItemTagsProvider(packOutput, lookupProvider, blockTagsProvider.contentsGetter(), Servantry.MODID, existingFileHelper) {
            @Override
            protected void addTags(HolderLookup.@NotNull Provider provider) {
                tag(TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(CuriosApi.MODID, "curio")))
                        .add(BuiltInRegistries.ITEM.stream()
                                .filter(item -> BuiltInRegistries.ITEM.getKey(item).getNamespace().equals(Servantry.MODID))
                                .filter(item -> item instanceof CurioItem)
                                .toArray(Item[]::new)
                        );
            }
        });
    }

}