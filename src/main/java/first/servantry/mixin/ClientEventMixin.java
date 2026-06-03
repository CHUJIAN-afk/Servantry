package first.servantry.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import first.servantry.api.core.ClientEvent;
import first.servantry.common.dataComponent.ScabbardContainer;
import first.servantry.register.DataComponentRegister;
import first.servantry.register.ServantWeaponRegister;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(ClientEvent.class)
public class ClientEventMixin {

    @WrapOperation(
            method = "tooltip",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;add(Ljava/lang/Object;)Z",
                    ordinal = 2
            )
    )
    private static boolean tooltip(List<Object> instance, Object object, Operation<Boolean> original, @Local(name = "itemStack") ItemStack itemStack) {
        if (itemStack.is(ServantWeaponRegister.InfiniteScabbard)) {
            ScabbardContainer container = itemStack.getComponents().getOrDefault(DataComponentRegister.Scabbard.get(), ScabbardContainer.EMPTY);
            if (!container.isEmpty()) {
                return instance.add(Component.translatable("item.servantry.tooltip.summon", container.itemStack().getDisplayName()).withStyle(ChatFormatting.GRAY));
            }
        }
        return original.call(instance, object);
    }
}
