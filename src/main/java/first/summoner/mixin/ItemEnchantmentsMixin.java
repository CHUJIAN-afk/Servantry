package first.summoner.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import first.summoner.utils.EnchantmentUtils;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Set;
import java.util.function.Consumer;

@Mixin(ItemEnchantments.class)
public class ItemEnchantmentsMixin {

    @WrapWithCondition(
            method = "addToTooltip",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V",
                    ordinal = 1
            )
    )
    private boolean accept(Consumer<Component> consumer, Object component, @Local(argsOnly = true) TooltipFlag flag, @Local ObjectIterator<Holder<Enchantment>> iterator, @Local Holder<Enchantment> enchantment) {
        boolean shift = flag.hasShiftDown();
        if (shift) {
            consumer.accept((Component) component);
            for (Component tooltip : EnchantmentUtils.getAllTooltips(enchantment)) {
                consumer.accept(tooltip);
            }
        } else {
            MutableComponent line = ((Component) component).copy();
            int attributeSize = EnchantmentUtils.getAttributeTooltips(enchantment).size();
            int effectSize = EnchantmentUtils.getEffectTooltips(enchantment).size();
            for (int i = 0; i < attributeSize; i++) {
                line.append(Component.literal("◇"));
            }
            for (int i = 0; i < effectSize; i++) {
                line.append(Component.literal("◆"));
            }
            consumer.accept(line);
        }
        if (!iterator.hasNext()) {
            ItemEnchantments self = ItemEnchantments.class.cast(this);
            Set<Holder<Enchantment>> holders = self.keySet();
            if (EnchantmentUtils.isApply(holders)) {
                consumer.accept(Component.empty());
                if (!shift) {
                    consumer.accept(Component.translatable("enchantment.summoner.shift_detail").withStyle(ChatFormatting.GOLD));
                }
            }
        }
        return false;
    }
}
