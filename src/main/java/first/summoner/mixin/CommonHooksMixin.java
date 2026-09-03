package first.summoner.mixin;

import first.lyra.common.minion.Minion;
import first.lyra.common.minion.MinionDamageSource;
import first.summoner.common.attachment.SummonMarkData;
import first.summoner.common.summonMark.SummonMarkType;
import first.summoner.register.SummonerAttachmentRegister;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.CommonHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(CommonHooks.class)
public class CommonHooksMixin {

    @Inject(method = "onLivingDeath", at = @At("RETURN"))
    private static void onLivingDeath(LivingEntity entity, DamageSource src, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) {
            if (!entity.level().isClientSide() && src instanceof MinionDamageSource minionDamageSource) {
                Minion minion = minionDamageSource.getMinion();
                Player owner = minion.getOwner();
                SummonMarkData summonMarkData = owner.getData(SummonerAttachmentRegister.SummonMarkData);
                if (summonMarkData.isSummonMarkTarget(entity)) {
                    Set<SummonMarkType> markTypes = summonMarkData.getMarkTypes();
                    for (SummonMarkType markType : markTypes) {
                        markType.kill(entity, minionDamageSource);
                    }
                }
            }
        }
    }
}
