package first.servantry.register;

import first.servantry.Servantry;
import first.servantry.api.common.attachment.InvincibleData;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

public class MobEffectRegister {

    private static final DeferredRegister<MobEffect> Register = DeferredRegister.create(Registries.MOB_EFFECT, Servantry.MODID);

    public static final DeferredHolder<MobEffect, MobEffect> Obsession = Register.register("obsession", () -> {
        MobEffect mobEffect = new MobEffect(MobEffectCategory.BENEFICIAL, 5578058) {
        };
        mobEffect.addAttributeModifier(AttributeRegister.ServantMaxCount, Servantry.rl("obsession"), 1, AttributeModifier.Operation.ADD_VALUE);
        return mobEffect;
    });

    public static final DeferredHolder<MobEffect, MobEffect> CellParasitism = Register.register("cell_parasitism", () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0x8AE0FF) {

        @Override
        public boolean applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
            entity.getData(AttachmentRegister.InvincibleData).attack(
                    entity,
                    null,
                    0,
                    DamageRegister.getDamageSource(DamageRegister.Servant, entity.level()),
                    2.0f * (amplifier + 1),
                    InvincibleData.Type.Global
            );
            return true;
        }

        @Override
        public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
            return duration % 20 == 0;
        }

    });

    public static void register(IEventBus eventBus) {
        Register.register(eventBus);
    }

}
