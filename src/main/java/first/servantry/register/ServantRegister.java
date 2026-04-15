package first.servantry.register;

import first.servantry.Servantry;
import first.servantry.api.register.Registries;
import first.servantry.api.register.ServantType;
import first.servantry.common.servent.EnchantedThrowingKnives;
import first.servantry.common.servent.SlimeBaby;
import first.servantry.common.servent.Terraprism;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ServantRegister {

    public static final DeferredRegister<ServantType<?>> Register = DeferredRegister.create(Registries.SERVANT_TYPES, Servantry.MODID);

    public static final DeferredHolder<ServantType<?>, ServantType<Terraprism>> TerraPrism = Register.register("terraprism", () -> new ServantType<>(Terraprism::new));

    public static final DeferredHolder<ServantType<?>, ServantType<EnchantedThrowingKnives>> EnchantedThrowingKnives = Register.register("enchanted_throwing_knives", () -> new ServantType<>(EnchantedThrowingKnives::new));

    public static final DeferredHolder<ServantType<?>, ServantType<SlimeBaby>> SlimeBaby = Register.register("slime_baby", () -> new ServantType<>(SlimeBaby::new));

    public static void register(IEventBus eventBus) {
        Register.register(eventBus);
    }

}