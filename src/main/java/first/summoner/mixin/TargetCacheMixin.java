package first.summoner.mixin;


import first.lyra.common.attachment.TargetCache;
import first.lyra.common.minion.Minion;
import first.summoner.common.attachment.SummonMarkData;
import first.summoner.register.SummonerAttachmentRegister;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(TargetCache.class)
public class TargetCacheMixin {

    @Inject(
            method = "getNewTarget",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private void getNewTarget(Minion minion, List<LivingEntity> targets, float ownerWarningDistance, boolean selfCenter, CallbackInfoReturnable<LivingEntity> cir) {
        Player owner = minion.getOwner() instanceof Player player ? player : null;
        if (owner != null) {
            SummonMarkData data = owner.getData(SummonerAttachmentRegister.SummonMarkData);
            LivingEntity markTarget = data.getMarkTarget();
            if (markTarget != null && targets.contains(markTarget)) {
                cir.setReturnValue(markTarget);
            }
        }
    }
}
