package first.summoner.utils;

import first.lyra.common.summonMark.SummonMarkType;
import first.summoner.register.SummonerEnchantmentSummonMarkTypeRegister;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class EnchantmentUtils {

    public static boolean isApply(Set<Holder<Enchantment>> holderSet) {
        for (Holder<Enchantment> holder : holderSet) {
            if (isSummonerEnchantment(holder)) {
                return true;
            }
        }
        return false;
    }

    /** 是否为召唤标记附魔（经附魔→标记映射注册）。 */
    public static boolean isSummonerEnchantment(Holder<Enchantment> holder) {
        return SummonerEnchantmentSummonMarkTypeRegister.getSummonMarkType(holder) != null;
    }

    public static List<Component> getAllTooltips(@Nullable Holder<Enchantment> enchantmentHolder) {
        List<Component> list = new ArrayList<>();
        list.addAll(getAttributeTooltips(enchantmentHolder));
        list.addAll(getEffectTooltips(enchantmentHolder));
        return list;
    }

    public static List<Component> getAttributeTooltips(@Nullable Holder<Enchantment> enchantmentHolder) {
        List<Component> list = new ArrayList<>();
        if (enchantmentHolder != null) {
            Holder<SummonMarkType> markType = SummonerEnchantmentSummonMarkTypeRegister.getSummonMarkType(enchantmentHolder);
            if (markType != null) {
                List<Component> tooltips = markType.value().getTooltips();
                for (Component tooltip : tooltips) {
                    list.add(Component.literal(" ◇ ").append(tooltip));
                }
            }
        }
        return list;
    }

    public static List<Component> getEffectTooltips(@Nullable Holder<Enchantment> enchantmentHolder) {
        List<Component> list = new ArrayList<>();
        if (enchantmentHolder != null) {
            Holder<SummonMarkType> markType = SummonerEnchantmentSummonMarkTypeRegister.getSummonMarkType(enchantmentHolder);
            if (markType != null) {
                ResourceLocation identifier = markType.value().location();
                String base = "enchantment." + identifier.getNamespace() + "." + identifier.getPath() + ".desc.";
                int i = 1;
                while (Language.getInstance().has(base + i)) {
                    list.add(Component.literal(" ◆ ").append(Component.translatable(base + i).withStyle(ChatFormatting.DARK_GRAY)));
                    i++;
                }
            }
        }
        return list;
    }
}
