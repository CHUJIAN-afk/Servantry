package first.servantry.register;

import first.servantry.Servantry;
import it.unimi.dsi.fastutil.ints.Int2DoubleFunction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ServantryMobEffectRegister {

    private static final DeferredRegister<MobEffect> Register = DeferredRegister.create(Registries.MOB_EFFECT, Servantry.MODID);

    /**
     * 着魔
     */
    public static final DeferredHolder<MobEffect, MobEffect> Obsession =
            Register.register("obsession", () -> builder(MobEffectCategory.BENEFICIAL, 0xb565ff)
                    .addAttributeModifier(ServantryAttributeRegister.ServantMaxCount, Servantry.rl("obsession"), 1, AttributeModifier.Operation.ADD_VALUE)
                    .build()
            );

    /**
     * 细胞寄生
     */
    public static final DeferredHolder<MobEffect, MobEffect> CellParasitism =
            Register.register("cell_parasitism", () -> builder(MobEffectCategory.HARMFUL, 0x8AE0FF)
                    .addAttributeModifier(ServantryAttributeRegister.HealthRegen, Servantry.rl("cell_parasitism"), amplifier -> -(amplifier + 1) * 2, AttributeModifier.Operation.ADD_VALUE)
                    .build()
            );

    /**
     * 诅咒焰
     */
    public static final DeferredHolder<MobEffect, MobEffect> CursedFlame =
            Register.register("cursed_flame", () -> builder(MobEffectCategory.HARMFUL, 0x1AFF05)
                    .addAttributeModifier(ServantryAttributeRegister.HealthRegen, Servantry.rl("cursed_flame"), amplifier -> -(amplifier + 1) * 2.4, AttributeModifier.Operation.ADD_VALUE)
                    .build()
            );

    /**
     * 暗影焰
     */
    public static final DeferredHolder<MobEffect, MobEffect> Shadowflame =
            Register.register("shadowflame", () -> builder(MobEffectCategory.HARMFUL, 0x9933FF)
                    .addAttributeModifier(ServantryAttributeRegister.HealthRegen, Servantry.rl("shadowflame"), amplifier -> -(amplifier + 1) * 1.5, AttributeModifier.Operation.ADD_VALUE)
                    .build()
            );

    /**
     * 月噬
     */
    public static final DeferredHolder<MobEffect, MobEffect> MoonBite =
            Register.register("moon_bite", () -> builder(MobEffectCategory.HARMFUL, 0x00d4b0)
                    .addAttributeModifier(Attributes.ARMOR, Servantry.rl("moon_bite"), amplifier -> -(amplifier + 1) * 10, AttributeModifier.Operation.ADD_VALUE)
                    .build()
            );

    /**
     * 噬神怒焰
     */
    public static final DeferredHolder<MobEffect, MobEffect> GodSlayerInferno =
            Register.register("god_slayer_inferno", () -> builder(MobEffectCategory.HARMFUL, 0x6f19d4)
                    .addAttributeModifier(ServantryAttributeRegister.HealthRegen, Servantry.rl("god_slayer_inferno"), amplifier -> -(amplifier + 1) * 25, AttributeModifier.Operation.ADD_VALUE)
                    .build()
            );

    /**
     * 碎甲
     */
    public static final DeferredHolder<MobEffect, MobEffect> ArmorCrunch =
            Register.register("armor_crunch", () -> builder(MobEffectCategory.HARMFUL, 0x303030)
                    .addAttributeModifier(Attributes.ARMOR, Servantry.rl("armor_crunch"), amplifier -> -(amplifier + 1) * 5, AttributeModifier.Operation.ADD_VALUE)
                    .build()
            );

    public static final DeferredHolder<MobEffect, MobEffect> BallistaPanicked =
            Register.register("ballista_panicked", () -> builder(MobEffectCategory.BENEFICIAL, 0xffa219)
                    .build()
            );

    public static final DeferredHolder<MobEffect, MobEffect> SoulMight =
            Register.register("soul_might", () -> builder(MobEffectCategory.BENEFICIAL, 0x9B6BFF)
                    .addAttributeModifier(ServantryAttributeRegister.ServantDamage, Servantry.rl("soul_might"), 0.08, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                    .build()
            );

    public static final DeferredHolder<MobEffect, MobEffect> SoulDefense =
            Register.register("soul_defense", () -> builder(MobEffectCategory.BENEFICIAL, 0x88A8FF)
                    .addAttributeModifier(Attributes.ARMOR, Servantry.rl("soul_defense"), 2, AttributeModifier.Operation.ADD_VALUE)
                    .build()
            );

    public static final DeferredHolder<MobEffect, MobEffect> SoulRecovery =
            Register.register("soul_recovery", () -> builder(MobEffectCategory.BENEFICIAL, 0x83FFB1)
                    .addAttributeModifier(ServantryAttributeRegister.HealthRegen, Servantry.rl("soul_recovery"), 0.05, AttributeModifier.Operation.ADD_VALUE)
                    .build());

    public static final DeferredHolder<MobEffect, MobEffect> HallowedMight =
            Register.register("hallowed_might", () -> builder(MobEffectCategory.BENEFICIAL, 0xFFD875)
                    .addAttributeModifier(ServantryAttributeRegister.ServantDamage, Servantry.rl("hallowed_might"), 0.16, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                    .build());

    public static final DeferredHolder<MobEffect, MobEffect> HallowedGrace =
            Register.register("hallowed_grace", () -> builder(MobEffectCategory.BENEFICIAL, 0xFFF4B0)
                    .addAttributeModifier(Attributes.ARMOR, Servantry.rl("hallowed_grace"), 4, AttributeModifier.Operation.ADD_VALUE)
                    .build());

    public static final DeferredHolder<MobEffect, MobEffect> HallowedRadiance =
            Register.register("hallowed_radiance", () -> builder(MobEffectCategory.BENEFICIAL, 0xFFFFAA)
                    .addAttributeModifier(ServantryAttributeRegister.HealthRegen, Servantry.rl("hallowed_radiance"), 0.1, AttributeModifier.Operation.ADD_VALUE)
                    .build());

    public static final DeferredHolder<MobEffect, MobEffect> PhantasmalMight =
            Register.register("phantasmal_might", () -> builder(MobEffectCategory.BENEFICIAL, 0xD48CFF)
                    .addAttributeModifier(ServantryAttributeRegister.ServantDamage, Servantry.rl("phantasmal_might"), 0.32, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                    .build());

    public static final DeferredHolder<MobEffect, MobEffect> PhantasmalBulwark =
            Register.register("phantasmal_bulwark", () -> builder(MobEffectCategory.BENEFICIAL, 0xB58CFF)
                    .addAttributeModifier(Attributes.ARMOR, Servantry.rl("phantasmal_bulwark"), 8, AttributeModifier.Operation.ADD_VALUE)
                    .build());

    public static final DeferredHolder<MobEffect, MobEffect> PhantasmalRebirth =
            Register.register("phantasmal_rebirth", () -> builder(MobEffectCategory.BENEFICIAL, 0xFF8CDE)
                    .addAttributeModifier(ServantryAttributeRegister.HealthRegen, Servantry.rl("phantasmal_rebirth"), 0.2, AttributeModifier.Operation.ADD_VALUE)
                    .build());


    public static void register(IEventBus eventBus) {
        Register.register(eventBus);
    }

    // ===================== ServantWeaponItemBuilder =====================

    private static Builder builder(MobEffectCategory category, int color) {
        return new Builder(category, color);
    }

    /**
     * 粒子效果工厂
     */
    @FunctionalInterface
    public interface ParticleFunction {
        ParticleOptions apply(MobEffectInstance effect);
    }

    // ===================== 函数式接口 =====================

    /**
     * 双参数函数
     */
    @FunctionalInterface
    public interface BiFunction {
        boolean apply(int duration, int amplifier);
    }

    /**
     * 瞬时效果回调
     */
    @FunctionalInterface
    public interface ApplyInstantenousEffect {
        void accept(@Nullable Entity source, @Nullable Entity indirectSource, LivingEntity entity, int amplifier, double health);
    }

    /**
     * 实体移除回调
     */
    @FunctionalInterface
    public interface OnMobRemoved {
        void accept(LivingEntity entity, int amplifier, Entity.RemovalReason reason);
    }

    /**
     * 实体受伤回调
     */
    @FunctionalInterface
    public interface OnMobHurt {
        void accept(LivingEntity entity, int amplifier, net.minecraft.world.damagesource.DamageSource source, float amount);
    }

    private static class Builder {
        private final MobEffectCategory category;
        private final int color;
        // 属性修改器
        private final List<AttributeModifierEntry> attributeModifiers = new ArrayList<>();
        private final List<AttributeCurveEntry> attributeCurveModifiers = new ArrayList<>();
        private ParticleFunction particleFactory = null;
        private int blendDurationTicks = 0;
        private SoundEvent soundOnAdded = null;
        private FeatureFlagSet requiredFeatures = FeatureFlags.VANILLA_SET;
        // 效果回调
        private BiConsumer<LivingEntity, Integer> applyEffectTick = null;
        private BiFunction shouldApplyEffectTickThisTick = null;
        private ApplyInstantenousEffect applyInstantenousEffect = null;
        private BiConsumer<LivingEntity, Integer> onEffectStarted = null;
        private BiConsumer<LivingEntity, Integer> onEffectAdded = null;
        private OnMobRemoved onMobRemoved = null;
        private OnMobHurt onMobHurt = null;
        private Function<LivingEntity, Boolean> isInstantenous = null;

        public Builder(MobEffectCategory category, int color) {
            this.category = category;
            this.color = color;
        }

        /**
         * 设置粒子效果工厂
         */
        public Builder particleFactory(ParticleFunction factory) {
            this.particleFactory = factory;
            return this;
        }

        /**
         * 设置固定粒子效果
         */
        public Builder particle(ParticleOptions particle) {
            this.particleFactory = effect -> particle;
            return this;
        }

        /**
         * 设置混合持续时间
         */
        public Builder blendDuration(int ticks) {
            this.blendDurationTicks = ticks;
            return this;
        }

        /**
         * 设置添加时的音效
         */
        public Builder soundOnAdded(SoundEvent sound) {
            this.soundOnAdded = sound;
            return this;
        }

        /**
         * 设置所需特性标志
         */
        public Builder requiredFeatures(FeatureFlagSet features) {
            this.requiredFeatures = features;
            return this;
        }

        /**
         * 设置每tick效果回调
         */
        public Builder applyEffectTick(BiConsumer<LivingEntity, Integer> callback) {
            this.applyEffectTick = callback;
            return this;
        }

        /**
         * 设置是否在当前tick触发效果
         */
        public Builder shouldApplyEffectTickThisTick(BiFunction callback) {
            this.shouldApplyEffectTickThisTick = callback;
            return this;
        }

        /**
         * 设置瞬时效果回调
         */
        public Builder applyInstantenousEffect(ApplyInstantenousEffect callback) {
            this.applyInstantenousEffect = callback;
            return this;
        }

        /**
         * 设置效果开始回调
         */
        public Builder onEffectStarted(BiConsumer<LivingEntity, Integer> callback) {
            this.onEffectStarted = callback;
            return this;
        }

        /**
         * 设置效果添加回调
         */
        public Builder onEffectAdded(BiConsumer<LivingEntity, Integer> callback) {
            this.onEffectAdded = callback;
            return this;
        }

        /**
         * 设置实体移除回调
         */
        public Builder onMobRemoved(OnMobRemoved callback) {
            this.onMobRemoved = callback;
            return this;
        }

        /**
         * 设置实体受伤回调
         */
        public Builder onMobHurt(OnMobHurt callback) {
            this.onMobHurt = callback;
            return this;
        }

        /**
         * 设置是否为瞬时效果
         */
        public Builder instantenous(Function<LivingEntity, Boolean> callback) {
            this.isInstantenous = callback;
            return this;
        }

        /**
         * 添加属性修改器
         */
        public Builder addAttributeModifier(Holder<Attribute> attribute, ResourceLocation id, double amount, AttributeModifier.Operation operation) {
            this.attributeModifiers.add(new AttributeModifierEntry(attribute, id, amount, operation));
            return this;
        }

        /**
         * 添加曲线属性修改器
         */
        public Builder addAttributeModifier(Holder<Attribute> attribute, ResourceLocation id, Int2DoubleFunction curve, AttributeModifier.Operation operation) {
            this.attributeCurveModifiers.add(new AttributeCurveEntry(attribute, id, operation, curve));
            return this;
        }

        /**
         * 构建MobEffect实例
         */
        public MobEffect build() {
            MobEffect effect = new MobEffect(category, color) {
                @Override
                public int getBlendDurationTicks() {
                    return blendDurationTicks;
                }

                @Override
                public boolean applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
                    if (applyEffectTick != null) {
                        applyEffectTick.accept(entity, amplifier);
                        return true;
                    }
                    return super.applyEffectTick(entity, amplifier);
                }

                @Override
                public void applyInstantenousEffect(@Nullable Entity source, @Nullable Entity indirectSource, @NotNull LivingEntity entity, int amplifier, double health) {
                    if (applyInstantenousEffect != null) {
                        applyInstantenousEffect.accept(source, indirectSource, entity, amplifier, health);
                    } else {
                        super.applyInstantenousEffect(source, indirectSource, entity, amplifier, health);
                    }
                }

                @Override
                public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
                    if (shouldApplyEffectTickThisTick != null) {
                        return shouldApplyEffectTickThisTick.apply(duration, amplifier);
                    }
                    return super.shouldApplyEffectTickThisTick(duration, amplifier);
                }

                @Override
                public void onEffectStarted(@NotNull LivingEntity entity, int amplifier) {
                    if (onEffectStarted != null) {
                        onEffectStarted.accept(entity, amplifier);
                    }
                }

                @Override
                public void onEffectAdded(@NotNull LivingEntity entity, int amplifier) {
                    if (onEffectAdded != null) {
                        onEffectAdded.accept(entity, amplifier);
                    }
                    if (soundOnAdded != null) {
                        entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), soundOnAdded, entity.getSoundSource(), 1.0F, 1.0F);
                    }
                }

                @Override
                public void onMobRemoved(@NotNull LivingEntity entity, int amplifier, @NotNull Entity.RemovalReason reason) {
                    if (onMobRemoved != null) {
                        onMobRemoved.accept(entity, amplifier, reason);
                    }
                }

                @Override
                public void onMobHurt(@NotNull LivingEntity entity, int amplifier, @NotNull DamageSource source, float amount) {
                    if (onMobHurt != null) {
                        onMobHurt.accept(entity, amplifier, source, amount);
                    }
                }

                @Override
                public boolean isInstantenous() {
                    if (isInstantenous != null) {
                        return isInstantenous.apply(null);
                    }
                    return super.isInstantenous();
                }

                @Override
                public @NotNull ParticleOptions createParticleOptions(@NotNull MobEffectInstance effect) {
                    if (particleFactory != null) {
                        return particleFactory.apply(effect);
                    }
                    return super.createParticleOptions(effect);
                }

                @Override
                public @NotNull FeatureFlagSet requiredFeatures() {
                    return requiredFeatures;
                }

            };
            for (AttributeModifierEntry entry : attributeModifiers) {
                effect.addAttributeModifier(entry.attribute, entry.id, entry.amount, entry.operation);
            }
            for (AttributeCurveEntry entry : attributeCurveModifiers) {
                effect.addAttributeModifier(entry.attribute, entry.id, entry.operation, entry.curve);
            }
            return effect;
        }
    }

    // ===================== 数据记录 =====================

    /**
     * 属性修改器条目
     */
    private record AttributeModifierEntry(Holder<Attribute> attribute, ResourceLocation id, double amount, AttributeModifier.Operation operation) {
    }

    /**
     * 曲线属性修改器条目
     */
    private record AttributeCurveEntry(Holder<Attribute> attribute, ResourceLocation id, AttributeModifier.Operation operation, Int2DoubleFunction curve) {
    }
}
