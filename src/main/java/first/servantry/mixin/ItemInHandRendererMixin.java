package first.servantry.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import first.servantry.api.item.IWhipWeapon;
import first.servantry.register.AttachmentRegister;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;


@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @ModifyExpressionValue(
            method = "renderItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z"
            )
    )
    private boolean ItemRender(boolean original, @Local(argsOnly = true) LivingEntity entity) {
        if (entity instanceof Player player && player.getMainHandItem().getItem() instanceof IWhipWeapon) {
            if (player.getData(AttachmentRegister.WhipData).isAttacking() || minecraft.options.keyAttack.isDown()) {
                return true;
            }
        }
        return original;
    }

}

