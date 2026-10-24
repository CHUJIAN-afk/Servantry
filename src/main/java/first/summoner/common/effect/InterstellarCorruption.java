package first.summoner.common.effect;

import first.lyra.register.LyraAttributeRegister;
import first.summoner.Summoner;
import first.summoner.common.entity.astrageldon.AstrageldonCombat;
import first.summoner.register.SummonerMobEffectRegister;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.jetbrains.annotations.NotNull;

public class InterstellarCorruption extends MobEffect {

    private static final ResourceLocation REGEN = Summoner.rl("interstellar_corruption");

    public InterstellarCorruption() {
        super(MobEffectCategory.HARMFUL, 0x7033A6);
        addAttributeModifier(LyraAttributeRegister.HealthRegen, REGEN, 0, AttributeModifier.Operation.ADD_VALUE);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        MobEffectInstance effect = entity.getEffect(SummonerMobEffectRegister.InterstellarCorruption);
        int stacks = effect.getDuration() / 400;
        if (stacks >= 4) {
            entity.hurt(AstrageldonCombat.source(entity, entity), 1000);
            return false;
        }
        AttributeInstance regen = entity.getAttribute(LyraAttributeRegister.HealthRegen);
        if (regen != null) {
            double amount = -0.05 * stacks;
            AttributeModifier current = regen.getModifier(REGEN);
            if (current == null || current.amount() != amount) {
                regen.removeModifier(REGEN);
                regen.addTransientModifier(new AttributeModifier(REGEN, amount, AttributeModifier.Operation.ADD_VALUE));
            }
        }
        return true;
    }
}
