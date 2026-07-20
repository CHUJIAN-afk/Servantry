package first.servantry.register;

import first.servantry.Servantry;
import first.servantry.common.dataComponent.ScabbardContainer;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ServantryDataComponentRegister {

    private static final DeferredRegister.DataComponents Register =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Servantry.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ScabbardContainer>> SCABBARD =
            Register.registerComponentType("scabbard_container", builder -> builder.persistent(ScabbardContainer.CODEC).networkSynchronized(ScabbardContainer.STREAM_CODEC));

    public static void register(IEventBus eventBus) {
        Register.register(eventBus);
    }
}