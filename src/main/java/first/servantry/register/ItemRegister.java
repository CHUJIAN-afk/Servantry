package first.servantry.register;

import first.servantry.Servantry;
import first.servantry.common.item.TerraPrismItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ItemRegister {

    private static final DeferredRegister.Items Register = DeferredRegister.createItems(Servantry.MODID);

    public static final DeferredItem<TerraPrismItem> TerraPrism = Register.registerItem("terraprism", TerraPrismItem::new);

    public static void register(IEventBus eventBus) {
        Register.register(eventBus);
    }

}
