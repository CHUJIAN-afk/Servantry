package first.servantry.mixin;

import first.servantry.api.item.IWhipWeapon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Shadow
    @Nullable
    public LocalPlayer player;

    @Inject(
            method = "continueAttack",
            at = @At("HEAD"),
            cancellable = true
    )
    private void continueDestroyBlock(boolean leftClick, CallbackInfo ci) {
        if (player != null && player.getMainHandItem().getItem() instanceof IWhipWeapon) {
            ci.cancel();
        }
    }

}
