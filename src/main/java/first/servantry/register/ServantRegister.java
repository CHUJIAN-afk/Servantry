package first.servantry.register;

import first.servantry.Servantry;
import first.servantry.api.register.ServantType;
import first.servantry.api.register.ServantryRegistries;
import first.servantry.common.servant.EnchantedThrowingKnives;
import first.servantry.common.servant.StardustCell;
import first.servantry.common.servant.StardustDragon;
import first.servantry.common.servant.Terraprism;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ServantRegister {

    public static final DeferredRegister<ServantType<?>> Register = DeferredRegister.create(ServantryRegistries.SERVANT_TYPES, Servantry.MODID);

    public static final DeferredHolder<ServantType<?>, ServantType<Terraprism>> TerraPrism = Register.register("terraprism", () -> new ServantType<>(Terraprism::new));

    public static final DeferredHolder<ServantType<?>, ServantType<StardustCell>> StardustCell = Register.register("stardust_cell", () -> new ServantType<>(StardustCell::new));

    public static final DeferredHolder<ServantType<?>, ServantType<EnchantedThrowingKnives>> EnchantedThrowingKnives = Register.register("enchanted_throwing_knives", () -> new ServantType<>(EnchantedThrowingKnives::new));

    public static final DeferredHolder<ServantType<?>, ServantType<StardustDragon>> StardustDragon = Register.register("stardust_dragon", () -> new ServantType<>(StardustDragon::new));

    public static void register(IEventBus eventBus) {
        Register.register(eventBus);
    }

}