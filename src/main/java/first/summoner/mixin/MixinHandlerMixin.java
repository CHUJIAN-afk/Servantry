package first.summoner.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import first.lyra.common.attachmentEntity.AttachmentEntity;
import first.lyra.common.attachmentEntity.AttachmentEntityDamageSource;
import first.lyra.mixinHandler.MixinHandler;
import first.summoner.common.attachment.SummonMarkData;
import first.summoner.common.summonMark.SummonMarkType;
import first.summoner.register.SummonerAttachmentRegister;
import first.summoner.register.SummonerSummonMarkRegister;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Set;

@Mixin(MixinHandler.class)
public class MixinHandlerMixin {

    @WrapMethod(method = "getModifyDamage")
    private static float getModifyDamage(LivingEntity living, LivingEntity summoner, AttachmentEntity minion, float damage, AttachmentEntityDamageSource source, Operation<Float> original) {
        if (summoner instanceof Player) {
            SummonMarkData data = summoner.getData(SummonerAttachmentRegister.SummonMarkData);
            if (data.isSummonMarkTarget(living)) {
                Set<SummonMarkType> summonMarks = data.getMarkTypes();
                damage += data.getCriticalDamage();
                if (data.isDamageCritical(source)) {
                    float base = 2f;
                    // 水蓝色黎明：暴击伤害 +50%
                    if (summonMarks.contains(SummonerSummonMarkRegister.CeruleanDawn.get())) {
                        base += 0.5f;
                    }
                    // 秘仪：暴击伤害 +15%，每 2% 标记暴击率额外 +1% 暴击伤害
                    if (summonMarks.contains(SummonerSummonMarkRegister.ArcaneRite.get())) {
                        base += 0.15f + data.getCriticalHitRate() * 0.5f;
                    }
                    damage *= base;
                }
            }
        }
        return original.call(living, summoner, minion, damage, source);
    }

    @WrapMethod(method = "getModifyArmorPierce")
    private static float getModifyArmorPierce(LivingEntity living, LivingEntity summoner, AttachmentEntity minion, float totalArmor, AttachmentEntityDamageSource source, Operation<Float> original) {
        if (summoner instanceof Player) {
            SummonMarkData data = summoner.getData(SummonerAttachmentRegister.SummonMarkData);
            if (data.isSummonMarkTarget(living)) {
                totalArmor -= data.getCriticalArmorPierce();
            }
        }
        return original.call(living, summoner, minion, totalArmor, source);
    }
}
