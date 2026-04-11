package first.servantry.register;

import first.servantry.Servantry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;


public class CreativeTabRegister {

    public static final DeferredRegister<CreativeModeTab> Register = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Servantry.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> Tab =
            Register.register("tab", () -> {
                CreativeModeTab.Builder builder = CreativeModeTab.builder();
                builder.title(Component.translatable("modid.servantry"));
                builder.icon(ItemRegister.TerraPrism.get()::getDefaultInstance);
                builder.displayItems((parameters, output) -> output.accept(ItemRegister.TerraPrism));
                return builder.build();
            });

    public static void register(IEventBus eventBus) {
        Register.register(eventBus);
    }


}
