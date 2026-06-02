package first.servantry.register;

import first.servantry.Servantry;
import first.servantry.client.creativeTab.AnimInfo;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Consumer;


public class CreativeTabRegister {

    public static final DeferredRegister<CreativeModeTab> Register = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Servantry.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> Tab =
            Register.register("tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("modid.servantry"))
                    .icon(ItemRegister.PygmyNecklace.get()::getDefaultInstance)
                    .build()
            );

    public static void renderBanners(final CreativeModeInventoryScreen screen, final GuiGraphics graphics, int mouseX, int mouseY, float scrollOffs) {
        List<TabGroup> sections = ItemRegister.Register.sortedEntries();
        LinkedHashMap<TabGroup, List<DeferredItem<Item>>> map = ItemRegister.Register.getMap();

        int totalRows = 0;
        for (TabGroup section : sections) {
            totalRows += 1;
            totalRows += (map.get(section).size() + 8) / 9;
        }

        int scrollRow = Math.round(scrollOffs * Math.max(0, totalRows - 5));
        int left = screen.getGuiLeft() + 8;
        int top = screen.getGuiTop() + 17;

        int currentRow = 0;
        for (TabGroup section : sections) {
            int bannerRow = currentRow;
            int itemRows = (map.get(section).size() + 8) / 9;
            currentRow += 1 + itemRows;
            ResourceLocation texture = section.texture();
            AnimInfo animInfo = section.animInfo();
            int visibleRow = bannerRow - scrollRow;
            if (visibleRow < 0 || visibleRow >= 5) continue;
            int bannerY = top + visibleRow * 18;
            AnimInfo.blitAnimated(graphics, texture, animInfo, left, bannerY, 162, mouseX, mouseY, true);
        }
    }

    public static void processItems(Consumer<ItemStack> displayItems, Consumer<ItemStack> searchItems) {
        LinkedHashMap<TabGroup, List<DeferredItem<Item>>> map = ItemRegister.Register.getMap();
        List<TabGroup> sortedKeys = ItemRegister.Register.sortedEntries();
        for (TabGroup key : sortedKeys) {
            List<DeferredItem<Item>> items = map.get(key);
            List<ItemStack> stacks = new ArrayList<>(items.stream()
                    .map(item -> item.get().getDefaultInstance())
                    .toList());
            for (int i = 0; i < 9; i++) {
                stacks.addFirst(ItemStack.EMPTY);
            }
            while (stacks.size() % 9 != 0) {
                stacks.add(ItemStack.EMPTY);
            }
            for (ItemStack stack : stacks) {
                displayItems.accept(stack);
                if (!stack.isEmpty()) {
                    searchItems.accept(stack);
                }
            }
        }
    }

    public static void register(IEventBus eventBus) {
        Register.register(eventBus);
    }
}
