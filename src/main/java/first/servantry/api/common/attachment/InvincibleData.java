package first.servantry.api.common.attachment;

import first.servantry.register.ServantryAttachmentRegister;
import first.servantry.register.ServantryDamageRegister;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class InvincibleData {

    public static void handler(LivingDamageEvent.Post event) {
        DamageSource damageSource = event.getSource();
        LivingEntity entity = event.getEntity();
        Level level = entity.level();
        if (!level.isClientSide() && damageSource.getEntity() instanceof Player attacker) {
            InvincibleData.get(entity).recordHit(attacker.getUUID(), 100);
        }
    }

    private final Map<UUID, AtomicInteger> hurtHistory;
    private final Map<UUID, AtomicInteger> partialInvincibleFrames;
    private final AtomicInteger globalInvincibleFrames;

    public InvincibleData() {
        this.hurtHistory = new HashMap<>();
        this.partialInvincibleFrames = new HashMap<>();
        this.globalInvincibleFrames = new AtomicInteger(0);
    }

    public void tick() {
        hurtHistory.values().removeIf(time -> time.addAndGet(-1) < 0);
        partialInvincibleFrames.values().removeIf(time -> time.addAndGet(-1) < 0);
        globalInvincibleFrames.addAndGet(-1);
    }

    public static InvincibleData get(LivingEntity living) {
        return living.getData(ServantryAttachmentRegister.InvincibleData);
    }

    /**
     * 是否被指定 UUID 攻击过（hurtHistory 中存在记录）
     */
    public boolean hasAttack(UUID uuid) {
        return hurtHistory.containsKey(uuid);
    }

    /**
     * 直接写入一条攻击历史记录（不造成伤害），用于同步“曾攻击过”标记
     */
    public void recordHit(@NotNull UUID uuid, int ticks) {
        hurtHistory.put(uuid, new AtomicInteger(ticks));
    }

    /**
     * 开启一次链式攻击构建
     */
    public static AttackBuilder attack(LivingEntity target) {
        return new AttackBuilder(target);
    }

    public enum Type {
        PARTIAL, GLOBAL
    }

    /**
     * 链式攻击构建器：必填 damageSource/damageAmount，可省略 attacker/invincibleTime/global
     */
    public static final class AttackBuilder {
        private final @NotNull LivingEntity target;
        private @Nullable UUID uuid = null;
        private int invincibleTime = 0;
        private @Nullable DamageSource damageSource = null;
        private float damageAmount = 0;
        private @NotNull Type type = Type.PARTIAL;
        private @Nullable MobEffectInstance mobEffectInstance = null;

        private AttackBuilder(@NotNull LivingEntity target) {
            this.target = target;
        }

        /**
         * 攻击者 UUID；省略表示未被跟踪的命中（null + PARTIAL 总是允许伤害且不记历史）
         */
        public AttackBuilder attacker(@Nullable UUID uuid) {
            this.uuid = uuid;
            return this;
        }

        public AttackBuilder damageSource(@Nullable DamageSource damageSource) {
            this.damageSource = damageSource;
            return this;
        }

        public AttackBuilder damageAmount(float damageAmount) {
            this.damageAmount = damageAmount;
            return this;
        }

        /**
         * 无敌帧 tick；省略为 0（不设无敌帧）
         */
        public AttackBuilder invincibleTime(int ticks) {
            this.invincibleTime = ticks;
            return this;
        }

        /**
         * 切换为全局无敌；省略则保持 PARTIAL
         */
        public AttackBuilder global() {
            this.type = Type.GLOBAL;
            return this;
        }

        /**
         * 在完成攻击后为目标添加此效果
         */
        public AttackBuilder effect(@Nullable MobEffectInstance mobEffectInstance) {
            this.mobEffectInstance = mobEffectInstance;
            return this;
        }

        public boolean apply() {
            if (target.isAlive() && damageAmount > 0) {
                InvincibleData invincibleData = InvincibleData.get(target);
                boolean canDamage = uuid == null;
                if (!canDamage) {
                    canDamage = switch (type) {
                        case PARTIAL -> !invincibleData.partialInvincibleFrames.containsKey(uuid);
                        case GLOBAL -> invincibleData.globalInvincibleFrames.get() <= 0;
                    };
                }
                if (canDamage) {
                    Level level = target.level();
                    if (damageSource == null) {
                        damageSource = ServantryDamageRegister.getDamageSource(DamageTypes.GENERIC, level);
                    }
                    int invulnerableTime = target.invulnerableTime;
                    target.invulnerableTime = 0;
                    boolean hurt = target.hurt(damageSource, damageAmount);
                    target.invulnerableTime = invulnerableTime;
                    if (hurt) {
                        if (mobEffectInstance != null) {
                            target.addEffect(mobEffectInstance);
                        }
                        if (invincibleTime > 0) {
                            if (type == Type.PARTIAL && uuid != null) {
                                invincibleData.partialInvincibleFrames.put(uuid, new AtomicInteger(invincibleTime));
                            } else {
                                invincibleData.globalInvincibleFrames.set(invincibleTime);
                            }
                        }
                    }
                    return hurt;
                }
            }
            return false;
        }
    }
}
