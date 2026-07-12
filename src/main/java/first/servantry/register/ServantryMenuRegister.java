package first.servantry.register;

import first.servantry.Servantry;
import first.servantry.client.screen.MithrilAnvilGui;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ServantryMenuRegister {

    private static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, Servantry.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<MithrilAnvilGui.MithrilAnvilMenu>> MITHRIL_ANVIL =
            MENUS.register("mithril_anvil", () ->
                    IMenuTypeExtension.create((id, inv, buf) -> new MithrilAnvilGui.MithrilAnvilMenu(id, inv)));

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
