package first.servantry.register;

import first.servantry.Servantry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.*;
import java.util.function.Consumer;


public class CreativeTabRegister {

    public static final DeferredRegister<CreativeModeTab> Register = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Servantry.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> Tab =
            Register.register("tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("modid.servantry"))
                    .icon(ItemRegister.PygmyNecklace.get()::getDefaultInstance)
                    .build()
            );

    public static void processItems(Consumer<ItemStack> displayItems, Consumer<ItemStack> searchItems) {
        List<Item> list = BuiltInRegistries.ITEM.stream()
                .filter(item -> BuiltInRegistries.ITEM.getKey(item).getNamespace().equals(Servantry.MODID))
                .toList();
        Map<Class<?>, List<ItemStack>> classListMap = new HashMap<>();
        for (Item item : list) {
            classListMap.computeIfAbsent(item.getClass(), k -> new ArrayList<>()).add(item.getDefaultInstance());
        }
        Collection<List<ItemStack>> values = classListMap.values();
        for (List<ItemStack> value : values) {
            while (value.size() % 9 != 0) {
                value.add(ItemStack.EMPTY);
            }
        }
        for (List<ItemStack> value : values) {
            for (ItemStack itemStack : value) {
                displayItems.accept(itemStack);
                if (!itemStack.isEmpty()) {
                    searchItems.accept(itemStack);
                }
            }
        }
    }

    public static void register(IEventBus eventBus) {
        Register.register(eventBus);
    }
}
