package first.servantry.utils;

import first.servantry.Servantry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArmorSetUtil {

    /**
     * 判断玩家是否穿戴了整套指定材质的盔甲
     */
    public static boolean hasFullSet(Player player, Holder<ArmorMaterial> material) {
        if (player == null) return false;
        for (ItemStack stack : player.getArmorSlots()) {
            if (!(stack.getItem() instanceof ArmorItem armor) || !armor.getMaterial().equals(material)) {
                return false;
            }
        }
        return true;
    }

    private static final Map<Holder<ArmorMaterial>, List<MutableComponent>> Cache = new HashMap<>();

    public static void addSetBonusTooltip(Player player, Holder<ArmorMaterial> material, List<Component> tooltip) {
        boolean active = hasFullSet(player, material);
        ChatFormatting titleColor = active ? ChatFormatting.GREEN : ChatFormatting.GRAY;
        ChatFormatting descColor = active ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY;
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.servantry.tooltip.set_bonus_title").withStyle(titleColor));
        List<MutableComponent> cachedLore = Cache.computeIfAbsent(material, k -> {
            List<MutableComponent> lines = new ArrayList<>();
            ResourceLocation location = BuiltInRegistries.ARMOR_MATERIAL.getKey(material.value());
            assert location != null;
            String baseKey = "item." + Servantry.MODID + "." + location.getPath() + ".set.";
            int index = 1;
            while (I18n.exists(baseKey + index)) {
                lines.add(Component.translatable(baseKey + index));
                index++;
            }
            return lines;
        });
        for (MutableComponent component : cachedLore) {
            tooltip.add(component.withStyle(descColor));
        }
    }

}