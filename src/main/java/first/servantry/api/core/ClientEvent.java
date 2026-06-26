package first.servantry.api.core;

import first.servantry.Servantry;
import first.servantry.api.armorSet.ArmorSet;
import first.servantry.api.item.IServantWeapon;
import first.servantry.api.register.ServantryRegistries;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = Servantry.MODID, value = Dist.CLIENT)
public class ClientEvent {

    @SubscribeEvent
    public static void tooltip(ItemTooltipEvent event) {
        Player player = event.getEntity();
        ItemStack itemStack = event.getItemStack();
        List<Component> toolTip = event.getToolTip();
        if (itemStack.getItem() instanceof IServantWeapon<?> iServantWeapon && player != null) {
            toolTip.addAll(iServantWeapon.getTooltips(player));
        }
        Item item = itemStack.getItem();
        ResourceLocation registryName = BuiltInRegistries.ITEM.getKey(item);
        if (registryName.getNamespace().equals(Servantry.MODID)) {
            List<MutableComponent> lore = new ArrayList<>();
            String baseKey = "item" + "." + Servantry.MODID + "." + registryName.getPath() + "." + "tooltip" + ".";
            int index = 1;
            while (I18n.exists(baseKey + index)) {
                lore.add(Component.translatable(baseKey + index));
                index++;
            }
            if (!lore.isEmpty()) {
                if (player != null) {
                    toolTip.add(Component.empty());
                }
                for (MutableComponent component : lore) {
                    toolTip.add(component.withStyle(ChatFormatting.DARK_GRAY));
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void armorSetTooltip(ItemTooltipEvent event) {
        Item item = event.getItemStack().getItem();
        Player player = event.getEntity();
        List<Component> toolTip = event.getToolTip();
        List<Item> armors = new ArrayList<>();
        if (player != null) {
            Iterable<ItemStack> armorSlots = player.getArmorSlots();
            for (ItemStack armorSlot : armorSlots) {
                armors.add(armorSlot.getItem());
            }
        }
        List<ArmorSet> armorSets = getArmorSets(item);
        for (ArmorSet armorSet : armorSets) {
            ResourceLocation id = armorSet.id();
            List<MutableComponent> lines = new ArrayList<>();
            lines.add(Component.empty());
            List<DeferredItem<Item>> items = armorSet.items();
            MutableComponent set = Component.empty();
            boolean full = player != null && armorSet.full(player);
            ChatFormatting descColor = full ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY;
            for (DeferredItem<Item> itemDeferredItem : items) {
                if (items.getFirst() == itemDeferredItem) {
                    set.append(Component.literal("[ ").withStyle(descColor));
                }
                Item piece = itemDeferredItem.get();
                ChatFormatting format = armors.contains(piece) ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY;
                set.append(piece.getDescription().copy().withStyle(format)).append(Component.literal(" "));
                if (items.getLast() == itemDeferredItem) {
                    set.append(Component.literal("] ").withStyle(descColor));
                }
            }
            set.append(Component.translatable("item.servantry.tooltip.set_bonus_title").withStyle(descColor));
            lines.add(set);
            Collection<Map.Entry<Holder<Attribute>, AttributeModifier>> entries = armorSet.modifiers().entries();
            descColor = full ? ChatFormatting.BLUE : ChatFormatting.DARK_GRAY;
            for (Map.Entry<Holder<Attribute>, AttributeModifier> entry : entries) {
                Attribute attr = entry.getKey().value();
                AttributeModifier modifier = entry.getValue();
                lines.add(attr.toComponent(modifier, TooltipFlag.NORMAL).withStyle(descColor));
            }
            String baseKey = Servantry.MODID + "." + id.getNamespace() + "." + id.getPath() + "." + "set" + ".";
            int index = 1;
            while (I18n.exists(baseKey + index)) {
                lines.add(Component.translatable(baseKey + index).withStyle(descColor));
                index++;
            }
            toolTip.addAll(lines);
        }
    }

    private static @NotNull List<ArmorSet> getArmorSets(Item item) {
        List<ArmorSet> list = ServantryRegistries.ARMOR_SETS.stream().toList();
        List<ArmorSet> target = new ArrayList<>();
        for (ArmorSet armorSet : list) {
            List<DeferredItem<Item>> items = armorSet.items();
            for (DeferredItem<Item> itemDeferredItem : items) {
                if (item == itemDeferredItem.get()) {
                    target.add(armorSet);
                    break;
                }
            }
        }
        return target;
    }
}