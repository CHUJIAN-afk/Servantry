package first.servantry.dadageneeator.provider;

import first.servantry.register.ItemRegister;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public class ServantryEntityLootProvider extends EntityLootSubProvider {

    public ServantryEntityLootProvider(HolderLookup.Provider provider) {
        super(FeatureFlags.REGISTRY.allFlags(), provider);
    }

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
        this.add(EntityType.ZOMBIE, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1f))
                        .add(LootItem.lootTableItem(ItemRegister.BlackLens.get())
                                .when(LootItemRandomChanceCondition.randomChance(0.01f))
                        )
                )
        );
    }

    @Override
    protected @NotNull Stream<EntityType<?>> getKnownEntityTypes() {
        return Stream.of(EntityType.ALLAY, EntityType.EVOKER, EntityType.ZOMBIE);
    }
}
