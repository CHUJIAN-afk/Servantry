package first.servantry.register;

import first.servantry.Servantry;
import first.servantry.api.common.attachment.InvincibleData;
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

public class MobEffectRegister {

    private static final DeferredRegister<MobEffect> Register = DeferredRegister.create(Registries.MOB_EFFECT, Servantry.MODID);

    /**
     * 着魔
     */
    public static final DeferredHolder<MobEffect, MobEffect> Obsession = Register.register("obsession", () ->
            builder(MobEffectCategory.BENEFICIAL, 0xb565ff)
                    .addAttributeModifier(AttributeRegister.ServantMaxCount, Servantry.rl("obsession"), 1, AttributeModifier.Operation.ADD_VALUE)
                    .build());

    /**
     * 细胞寄生
     */
    public static final DeferredHolder<MobEffect, MobEffect> CellParasitism = Register.register("cell_parasitism", () ->
            builder(MobEffectCategory.BENEFICIAL, 0x8AE0FF)
                    .shouldApplyEffectTickThisTick((duration, amplifier) -> duration % 20 == 0)
                    .applyEffectTick((entity, amplifier) ->
                            InvincibleData.criteriaAttack(
                                    entity,
                                    null,
                                    0,
                                    DamageRegister.getDamageSource(DamageRegister.Servant, entity.level()),
                                    2 * (amplifier + 1),
                                    InvincibleData.Type.Global
                            )
                    )
                    .build());

    /**
     * 诅咒焰
     */
    public static final DeferredHolder<MobEffect, MobEffect> CursedFlame =
            Register.register("cursed_flame", () -> builder(MobEffectCategory.HARMFUL, 0x1AFF05)
                    .shouldApplyEffectTickThisTick((duration, amplifier) -> duration % 10 == 0)
                    .applyEffectTick((entity, amplifier) -> {
                        InvincibleData.criteriaAttack(
                                entity,
                                null,
                                0,
                                DamageRegister.getDamageSource(DamageRegister.Servant, entity.level()),
                                1.2f * (amplifier + 1),
                                InvincibleData.Type.Global
                        );
                    })
                    .build()
            );

    // ===================== Builder =====================

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
        public Builder addAttributeModifier(Holder<Attribute> attribute, ResourceLocation id, AttributeModifier.Operation operation, Int2DoubleFunction curve) {
            this.attributeCurveModifiers.add(new AttributeCurveEntry(attribute, id, operation, curve));
            return this;
        }

        /**
         * 构建MobEffect实例
         */
        public MobEffect build() {
            return new MobEffect(category, color) {
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
                public net.minecraft.core.particles.@NotNull ParticleOptions createParticleOptions(@NotNull MobEffectInstance effect) {
                    if (particleFactory != null) {
                        return particleFactory.apply(effect);
                    }
                    return super.createParticleOptions(effect);
                }

                @Override
                public @NotNull FeatureFlagSet requiredFeatures() {
                    return requiredFeatures;
                }

                @Override
                public void createModifiers(int amplifier, @NotNull BiConsumer<Holder<Attribute>, AttributeModifier> output) {
                    super.createModifiers(amplifier, output);
                    for (AttributeModifierEntry entry : attributeModifiers) {
                        output.accept(entry.attribute, new AttributeModifier(entry.id, entry.amount * (amplifier + 1), entry.operation));
                    }
                    for (AttributeCurveEntry entry : attributeCurveModifiers) {
                        output.accept(entry.attribute, new AttributeModifier(entry.id, entry.curve.apply(amplifier), entry.operation));
                    }
                }
            };
        }
    }

    // ===================== 数据记录 =====================

    /**
     * 属性修改器条目
     */
    private record AttributeModifierEntry(Holder<Attribute> attribute, ResourceLocation id, double amount, AttributeModifier.Operation operation) {}

    /** 曲线属性修改器条目 */
    private record AttributeCurveEntry(Holder<Attribute> attribute, ResourceLocation id, AttributeModifier.Operation operation, Int2DoubleFunction curve) {}

    public static void register(IEventBus eventBus) {
        Register.register(eventBus);
    }

}
