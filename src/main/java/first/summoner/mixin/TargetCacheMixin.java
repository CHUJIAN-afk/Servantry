package first.summoner.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import first.lyra.common.attachment.TargetCache;
import first.lyra.common.minion.Minion;
import first.summoner.common.attachment.SummonMarkData;
import first.summoner.register.SummonerAttachmentRegister;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(TargetCache.class)
public class TargetCacheMixin {

    @Unique
    private boolean summoner$hasSummonMark = false;

    @Inject(
            method = "getNewTarget",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private void getNewTarget(Minion minion, List<LivingEntity> targets, float ownerWarningDistance, boolean selfCenter, CallbackInfoReturnable<LivingEntity> cir) {
        SummonMarkData data = minion.getOwner().getData(SummonerAttachmentRegister.SummonMarkData);
        LivingEntity markTarget = data.getMarkTarget();
        if (markTarget != null && summoner$hasSummonMark) {
            cir.setReturnValue(markTarget);
        }
    }

    @Inject(
            method = "tick",
            at = @At(value = "HEAD")
    )
    private void tick(Player player, CallbackInfo ci) {
        summoner$hasSummonMark = false;
    }

    @WrapOperation(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;add(Ljava/lang/Object;)Z"
            )
    )
    private boolean add(List<LivingEntity> instance, Object object, Operation<Boolean> original, @Local(argsOnly = true) Player player) {
        SummonMarkData data = player.getData(SummonerAttachmentRegister.SummonMarkData);
        LivingEntity markTarget = data.getMarkTarget();
        if (object == markTarget) {
            summoner$hasSummonMark = true;
        }
        return original.call(instance, object);
    }
}
