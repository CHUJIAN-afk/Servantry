package first.servantry.dadageneeator.provider;

import first.servantry.register.ItemRegister;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public class SoulEntityLootProvider extends EntityLootSubProvider {

    public SoulEntityLootProvider(HolderLookup.Provider registries) {
        super(FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    public void generate() {
        // 1. 生成并覆盖悦灵 (Allay) 的掉落表
        this.add(EntityType.ALLAY, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(ItemRegister.TerraPrism.get())
                                .when(LootItemRandomChanceCondition.randomChance(1)) // 1% 几率 (JEI/EMI 完美识别)
                                .when(LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.ATTACKER, EntityPredicate.Builder.entity().of(EntityType.PLAYER))) // 必须是玩家击杀
                        )
                )
        );

        // 2. 生成并覆盖蝙蝠 (Bat) 的掉落表
        this.add(EntityType.BAT, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(ItemRegister.SanguineStaff.get())
                                .when(LootItemRandomChanceCondition.randomChance(1F)) // 1% 几率
                        )
                )
        );
    }

    @Override
    protected @NotNull Stream<EntityType<?>> getKnownEntityTypes() {
        return Stream.of(EntityType.ALLAY, EntityType.BAT);
    }

}