package first.summoner.mixin;


import first.lyra.common.attachmentEntity.AttachmentEntity;
import first.lyra.common.attachmentEntity.AttachmentEntityDamageSource;
import first.lyra.common.minion.Minion;
import first.summoner.common.attachment.SummonMarkData;
import first.summoner.common.summonMark.SummonMarkType;
import first.summoner.register.SummonerAttachmentRegister;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
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
            if (!entity.level().isClientSide() && src instanceof AttachmentEntityDamageSource attachmentDamageSource) {
                AttachmentEntity minion = attachmentDamageSource.getAttachmentEntity();
                LivingEntity owner = minion instanceof Minion minionOwner ? minionOwner.getOwner() : null;
                if (owner != null) {
                    SummonMarkData summonMarkData = owner.getData(SummonerAttachmentRegister.SummonMarkData);
                    if (summonMarkData.isSummonMarkTarget(entity)) {
                        Set<SummonMarkType> markTypes = summonMarkData.getMarkTypes();
                        for (SummonMarkType markType : markTypes) {
                            markType.kill(entity, attachmentDamageSource);
                        }
                    }
                }
            }
        }
    }
}
