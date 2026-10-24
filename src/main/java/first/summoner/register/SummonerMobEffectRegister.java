package first.summoner.register;

import first.lyra.register.LyraAttributeRegister;
import first.lyra.register.SimpleMobEffectBuilder;
import first.summoner.Summoner;
import first.summoner.common.effect.InterstellarCorruption;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class SummonerMobEffectRegister {

    private static final DeferredRegister<MobEffect> Register = DeferredRegister.create(Registries.MOB_EFFECT, Summoner.MODID);

    /**
     * 着魔
     */
    public static final DeferredHolder<MobEffect, MobEffect> Obsession =
            Register.register("obsession", () -> new SimpleMobEffectBuilder(MobEffectCategory.BENEFICIAL, 0xb565ff)
                    .addAttributeModifier(LyraAttributeRegister.MinionMaxCount, Summoner.rl("obsession"), 1, AttributeModifier.Operation.ADD_VALUE)
                    .build()
            );

    /**
     * 细胞寄生
     */
    public static final DeferredHolder<MobEffect, MobEffect> CellParasitism =
            Register.register("cell_parasitism", () -> new SimpleMobEffectBuilder(MobEffectCategory.HARMFUL, 0x8AE0FF)
                    .addAttributeModifier(LyraAttributeRegister.HealthRegen, Summoner.rl("cell_parasitism"), amplifier -> -(amplifier + 1) * 2, AttributeModifier.Operation.ADD_VALUE)
                    .build()
            );

    /**
     * 诅咒焰
     */
    public static final DeferredHolder<MobEffect, MobEffect> CursedFlame =
            Register.register("cursed_flame", () -> new SimpleMobEffectBuilder(MobEffectCategory.HARMFUL, 0x1AFF05)
                    .addAttributeModifier(LyraAttributeRegister.HealthRegen, Summoner.rl("cursed_flame"), amplifier -> -(amplifier + 1) * 2.4, AttributeModifier.Operation.ADD_VALUE)
                    .build()
            );

    /**
     * 暗影焰
     */
    public static final DeferredHolder<MobEffect, MobEffect> Shadowflame =
            Register.register("shadowflame", () -> new SimpleMobEffectBuilder(MobEffectCategory.HARMFUL, 0x9933FF)
                    .addAttributeModifier(LyraAttributeRegister.HealthRegen, Summoner.rl("shadowflame"), amplifier -> -(amplifier + 1) * 1.5, AttributeModifier.Operation.ADD_VALUE)
                    .build()
            );

    /**
     * 酸性毒液 - 每秒损失 1 点生命（经 Lyra 生命恢复属性结算，与其它伤害性效果一致）
     */
    public static final DeferredHolder<MobEffect, MobEffect> AcidVenom =
            Register.register("acid_venom", () -> new SimpleMobEffectBuilder(MobEffectCategory.HARMFUL, 0x228B22)
                    .addAttributeModifier(LyraAttributeRegister.HealthRegen, Summoner.rl("acid_venom"), amplifier -> -(amplifier + 1) * 1, AttributeModifier.Operation.ADD_VALUE)
                    .build()
            );

    /**
     * 月噬
     */
    public static final DeferredHolder<MobEffect, MobEffect> MoonBite =
            Register.register("moon_bite", () -> new SimpleMobEffectBuilder(MobEffectCategory.HARMFUL, 0x00d4b0)
                    .addAttributeModifier(Attributes.ARMOR, Summoner.rl("moon_bite"), amplifier -> -(amplifier + 1) * 10, AttributeModifier.Operation.ADD_VALUE)
                    .build()
            );

    /**
     * 噬神怒焰
     */
    public static final DeferredHolder<MobEffect, MobEffect> GodSlayerInferno =
            Register.register("god_slayer_inferno", () -> new SimpleMobEffectBuilder(MobEffectCategory.HARMFUL, 0x6f19d4)
                    .addAttributeModifier(LyraAttributeRegister.HealthRegen, Summoner.rl("god_slayer_inferno"), amplifier -> -(amplifier + 1) * 25, AttributeModifier.Operation.ADD_VALUE)
                    .build()
            );

    /**
     * 碎甲
     */
    public static final DeferredHolder<MobEffect, MobEffect> ArmorCrunch =
            Register.register("armor_crunch", () -> new SimpleMobEffectBuilder(MobEffectCategory.HARMFUL, 0x303030)
                    .addAttributeModifier(Attributes.ARMOR, Summoner.rl("armor_crunch"), amplifier -> -(amplifier + 1) * 5, AttributeModifier.Operation.ADD_VALUE)
                    .build()
            );

    public static final DeferredHolder<MobEffect, MobEffect> AstralBlight =
            Register.register("astral_blight", () -> new SimpleMobEffectBuilder(MobEffectCategory.HARMFUL, 0x833EC4)
                    .addAttributeModifier(Attributes.ARMOR, Summoner.rl("astral_blight"), -24, AttributeModifier.Operation.ADD_VALUE)
                    .addAttributeModifier(LyraAttributeRegister.HealthRegen, Summoner.rl("astral_blight"), -2, AttributeModifier.Operation.ADD_VALUE)
                    .build());

    public static final DeferredHolder<MobEffect, MobEffect> InterstellarCorruption =
            Register.register("interstellar_corruption", InterstellarCorruption::new);

    public static final DeferredHolder<MobEffect, MobEffect> BallistaPanicked =
            Register.register("ballista_panicked", () -> new SimpleMobEffectBuilder(MobEffectCategory.BENEFICIAL, 0xffa219)
                    .build()
            );

    public static final DeferredHolder<MobEffect, MobEffect> SoulMight =
            Register.register("soul_might", () -> new SimpleMobEffectBuilder(MobEffectCategory.BENEFICIAL, 0x9B6BFF)
                    .addAttributeModifier(LyraAttributeRegister.SummonDamage, Summoner.rl("soul_might"), 0.08, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                    .build()
            );

    public static final DeferredHolder<MobEffect, MobEffect> SoulDefense =
            Register.register("soul_defense", () -> new SimpleMobEffectBuilder(MobEffectCategory.BENEFICIAL, 0x88A8FF)
                    .addAttributeModifier(Attributes.ARMOR, Summoner.rl("soul_defense"), 2, AttributeModifier.Operation.ADD_VALUE)
                    .build()
            );

    public static final DeferredHolder<MobEffect, MobEffect> SoulRecovery =
            Register.register("soul_recovery", () -> new SimpleMobEffectBuilder(MobEffectCategory.BENEFICIAL, 0x83FFB1)
                    .addAttributeModifier(LyraAttributeRegister.HealthRegen, Summoner.rl("soul_recovery"), 0.05, AttributeModifier.Operation.ADD_VALUE)
                    .build());

    public static final DeferredHolder<MobEffect, MobEffect> HallowedMight =
            Register.register("hallowed_might", () -> new SimpleMobEffectBuilder(MobEffectCategory.BENEFICIAL, 0xFFD875)
                    .addAttributeModifier(LyraAttributeRegister.SummonDamage, Summoner.rl("hallowed_might"), 0.16, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                    .build());

    public static final DeferredHolder<MobEffect, MobEffect> HallowedGrace =
            Register.register("hallowed_grace", () -> new SimpleMobEffectBuilder(MobEffectCategory.BENEFICIAL, 0xFFF4B0)
                    .addAttributeModifier(Attributes.ARMOR, Summoner.rl("hallowed_grace"), 4, AttributeModifier.Operation.ADD_VALUE)
                    .build());

    public static final DeferredHolder<MobEffect, MobEffect> HallowedRadiance =
            Register.register("hallowed_radiance", () -> new SimpleMobEffectBuilder(MobEffectCategory.BENEFICIAL, 0xFFFFAA)
                    .addAttributeModifier(LyraAttributeRegister.HealthRegen, Summoner.rl("hallowed_radiance"), 0.1, AttributeModifier.Operation.ADD_VALUE)
                    .build());

    public static final DeferredHolder<MobEffect, MobEffect> PhantasmalMight =
            Register.register("phantasmal_might", () -> new SimpleMobEffectBuilder(MobEffectCategory.BENEFICIAL, 0xD48CFF)
                    .addAttributeModifier(LyraAttributeRegister.SummonDamage, Summoner.rl("phantasmal_might"), 0.32, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                    .build());

    public static final DeferredHolder<MobEffect, MobEffect> PhantasmalBulwark =
            Register.register("phantasmal_bulwark", () -> new SimpleMobEffectBuilder(MobEffectCategory.BENEFICIAL, 0xB58CFF)
                    .addAttributeModifier(Attributes.ARMOR, Summoner.rl("phantasmal_bulwark"), 8, AttributeModifier.Operation.ADD_VALUE)
                    .build());

    public static final DeferredHolder<MobEffect, MobEffect> PhantasmalRebirth =
            Register.register("phantasmal_rebirth", () -> new SimpleMobEffectBuilder(MobEffectCategory.BENEFICIAL, 0xFF8CDE)
                    .addAttributeModifier(LyraAttributeRegister.HealthRegen, Summoner.rl("phantasmal_rebirth"), 0.2, AttributeModifier.Operation.ADD_VALUE)
                    .build());


    public static void register(IEventBus eventBus) {
        Register.register(eventBus);
    }
}
