package first.summoner.register;

import first.summoner.Summoner;
import first.summoner.common.block.MithrilAnvilBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class SummonerBlockRegister {

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Summoner.MODID);

    public static final DeferredBlock<MithrilAnvilBlock> MITHRIL_ANVIL = BLOCKS.register("mithril_anvil",
            () -> new MithrilAnvilBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(5.0F, 1200.0F)
                    .sound(SoundType.ANVIL)
                    .requiresCorrectToolForDrops()));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
