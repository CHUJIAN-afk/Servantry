package first.servantry.mixin.servantry;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import first.servantry.api.core.ClientEvent;
import first.servantry.common.dataComponent.ScabbardContainer;
import first.servantry.register.DataComponentRegister;
import first.servantry.register.ItemRegister;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientEvent.class)
public class ClientEventMixin {

    @ModifyExpressionValue(
            method = "tooltip",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;",
                    ordinal = 2
            )
    )
    private static MutableComponent tooltip(MutableComponent original, @Local(name = "itemStack") ItemStack itemStack) {
        if (itemStack.is(ItemRegister.InfiniteScabbard)) {
            ScabbardContainer container = itemStack.getComponents().getOrDefault(DataComponentRegister.Scabbard.get(), ScabbardContainer.EMPTY);
            if (!container.isEmpty()) {
                return Component.literal(container.itemStack().getDisplayName().getString());
            }
        }
        return original;
    }
}
