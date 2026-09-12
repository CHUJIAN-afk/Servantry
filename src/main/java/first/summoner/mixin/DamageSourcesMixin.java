package first.summoner.mixin;

import first.lyra.common.entity.AttachmentEntityDamageSource;
import first.summoner.common.minion.minion.InfiniteShadow;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DamageSources.class)
public class DamageSourcesMixin {

    @Inject(
            method = "playerAttack",
            at = @At("RETURN"),
            cancellable = true
    )
    private void playerAttack(Player player, CallbackInfoReturnable<DamageSource> cir) {
        if (player instanceof InfiniteShadow.InfiniteShadowFakePlayer infiniteShadowFakePlayer) {
            if (infiniteShadowFakePlayer.getInfiniteShadow().getDamageSource() instanceof AttachmentEntityDamageSource damageSource) {
                cir.setReturnValue(new AttachmentEntityDamageSource(damageSource.typeHolder(), infiniteShadowFakePlayer, damageSource.getEntity(), damageSource.getSourcePosition(), damageSource.getAttachmentEntity()));
            }
        }
    }
}
