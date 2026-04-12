package first.servantry.mixin;

import first.servantry.api.item.IWhipWeapon;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class PlayerMixin {

    @Inject(
            method = "getAttackStrengthScale",
            at = @At("RETURN"),
            cancellable = true
    )
    public void getAttackStrengthScale(float adjustTicks, CallbackInfoReturnable<Float> cir) {
        Player player = (Player) (Object) this;
        if (player.getMainHandItem().getItem() instanceof IWhipWeapon) {
            cir.setReturnValue(1f);
        }
    }

}
