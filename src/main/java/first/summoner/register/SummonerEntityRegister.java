package first.summoner.register;

import first.summoner.Summoner;
import first.summoner.common.entity.Astrageldon;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class SummonerEntityRegister {

    private static final DeferredRegister<EntityType<?>> Register = DeferredRegister.create(Registries.ENTITY_TYPE, Summoner.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<Astrageldon>> ASTRAGELDON =
            Register.register("astrageldon", location -> EntityType.Builder.of(Astrageldon::new, MobCategory.MONSTER)
                    .sized(3F, 3F)
                    .clientTrackingRange(128)
                    .updateInterval(1)
                    .build(location.getPath())
            );

    public static void register(IEventBus eventBus) {
        Register.register(eventBus);
    }
}
