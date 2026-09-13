package first.summoner.register;

import first.summoner.Summoner;
import first.summoner.common.master.DoomsdayStar;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class SummonerEntityRegister {

    private static final DeferredRegister<EntityType<?>> Register =
            DeferredRegister.create(Registries.ENTITY_TYPE, Summoner.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<DoomsdayStar>> DOOMSDAY_STAR =
            Register.register("doomsday_star", () -> EntityType.Builder.of(DoomsdayStar::new, MobCategory.MONSTER)
                    .sized(2.0F, 2.0F)
                    .eyeHeight(1.75F)
                    .clientTrackingRange(10)
                    .build(Summoner.rl("doomsday_star").toString()));

    public static void register(IEventBus eventBus) {
        Register.register(eventBus);
    }
}
