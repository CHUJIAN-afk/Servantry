package first.servantry.api.common.attachment;

import first.servantry.register.AttachmentRegister;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class InvincibleData {

    private final Map<UUID, AtomicInteger> hurtHistory = new HashMap<>();
    private final Map<UUID, AtomicInteger> partialInvincibleFrames = new HashMap<>();
    private final AtomicInteger globalInvincibleFrames = new AtomicInteger(0);

    public static boolean recentlyAttacked(LivingEntity living, UUID uuid) {
        return living.getData(AttachmentRegister.InvincibleData).hurtHistory.containsKey(uuid);
    }

    public static void criteriaAttack(LivingEntity target, @Nullable UUID uuid, int invincibleTime, @NotNull DamageSource damageSource, float damage, Type type) {
        target.getData(AttachmentRegister.InvincibleData).attack(target, uuid, invincibleTime, damageSource, damage, type);
    }

    public void tick() {
        hurtHistory.values().removeIf(integer -> integer.addAndGet(-1) < 0);
        partialInvincibleFrames.values().removeIf(integer -> integer.addAndGet(-1) < 0);
        globalInvincibleFrames.addAndGet(-1);
    }

    public Map<UUID, AtomicInteger> getHurtHistory() {
        return hurtHistory;
    }

    public void attack(LivingEntity target, @Nullable UUID uuid, int invincibleTime, @NotNull DamageSource damageSource, float damage, Type type) {
        if (canDamage(uuid, type)) {
            if (uuid != null) {
                hurtHistory.put(uuid, new AtomicInteger(100));
            }
            int invulnerableTime = target.invulnerableTime;
            target.invulnerableTime = 0;
            target.hurt(damageSource, damage);
            target.invulnerableTime = invulnerableTime;
            if (invincibleTime > 0) {
                if (uuid != null) {
                    if (type == Type.PARTIAL) {
                        partialInvincibleFrames.put(uuid, new AtomicInteger(invincibleTime));
                    }
                    if (type == Type.Global) {
                        globalInvincibleFrames.set(invincibleTime);
                    }
                } else {
                    globalInvincibleFrames.set(invincibleTime);
                }
            }
        }
    }

    public boolean canDamage(UUID uuid, Type type) {
        return switch (type) {
            case PARTIAL -> uuid == null || partialInvincibleFrames.getOrDefault(uuid, new AtomicInteger(0)).get() <= 0;
            case Global -> globalInvincibleFrames.get() <= 0;
            case null -> false;
        };
    }

    public enum Type {
        PARTIAL, Global
    }

}
