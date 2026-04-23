package first.servantry.register;

import first.servantry.Servantry;
import first.servantry.api.register.ServantryRegistries;
import first.servantry.api.register.ServantType;
import first.servantry.common.servent.EnchantedThrowingKnives;
import first.servantry.common.servent.StardustCell;
import first.servantry.common.servent.Terraprism;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ServantRegister {

    public static final DeferredRegister<ServantType<?>> Register = DeferredRegister.create(ServantryRegistries.SERVANT_TYPES, Servantry.MODID);

    public static final DeferredHolder<ServantType<?>, ServantType<Terraprism>> TerraPrism = Register.register("terraprism", () -> new ServantType<>(Terraprism::new));

    public static final DeferredHolder<ServantType<?>, ServantType<StardustCell>> StardustCell = Register.register("stardust_cell", () -> new ServantType<>(StardustCell::new));

    public static final DeferredHolder<ServantType<?>, ServantType<EnchantedThrowingKnives>> EnchantedThrowingKnives = Register.register("enchanted_throwing_knives", () -> new ServantType<>(EnchantedThrowingKnives::new));

    public static void register(IEventBus eventBus) {
        Register.register(eventBus);
    }

}