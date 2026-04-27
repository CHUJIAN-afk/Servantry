package first.servantry.api.common.attachment;

import first.servantry.api.servant.Servant;
import first.servantry.register.AttachmentRegister;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class InvincibleData {

    private final Map<UUID, AtomicInteger> partialInvincibleFrames = new HashMap<>();
    private final AtomicInteger globalInvincibleFrames = new AtomicInteger(0);

    public void tick() {
        partialInvincibleFrames.values().removeIf(integer -> integer.addAndGet(-1) < 0);
        globalInvincibleFrames.addAndGet(-1);
    }

    public static void servantAttack(LivingEntity target, Servant servant, int invincibleTime, DamageSource damageSource, float damage, Type type) {
        target.getData(AttachmentRegister.InvincibleData).attack(target, servant.getUuid(), invincibleTime, damageSource, damage, type);
    }

    public static boolean canAttack(LivingEntity target, Servant servant, Type type) {
        return target.getData(AttachmentRegister.InvincibleData).canDamage(servant.getUuid(), type);
    }

    public void attack(LivingEntity target, UUID uuid, int invincibleTime, DamageSource damageSource, float damage, Type type) {
        if (canDamage(uuid, type)) {
            int invulnerableTime = target.invulnerableTime;
            target.invulnerableTime = 0;
            target.hurt(damageSource, damage);
            target.invulnerableTime = invulnerableTime;
            if (invincibleTime > 0) {
                if (type == Type.PARTIAL) {
                    partialInvincibleFrames.put(uuid, new AtomicInteger(invincibleTime));
                }
                if (type == Type.Global) {
                    globalInvincibleFrames.set(invincibleTime);
                }
            }
        }
    }

    public boolean canDamage(UUID uuid, Type type) {
        return switch (type) {
            case PARTIAL -> partialInvincibleFrames.getOrDefault(uuid, new AtomicInteger(0)).get() <= 0;
            case Global -> globalInvincibleFrames.get() <= 0;
            case null -> false;
        };
    }

    public enum Type {
        PARTIAL, Global
    }

}
