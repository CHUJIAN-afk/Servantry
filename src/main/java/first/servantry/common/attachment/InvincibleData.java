package first.servantry.common.attachment;

import first.servantry.api.servant.Servant;
import first.servantry.register.AttachmentRegister;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class InvincibleData {

    private final Map<Servant, AtomicInteger> partialInvincibleFrames = new HashMap<>();
    private final AtomicInteger globalInvincibleFrames = new AtomicInteger(0);

    public void tick() {
        partialInvincibleFrames.values().removeIf(integer -> integer.addAndGet(-1) < 0);
        globalInvincibleFrames.addAndGet(-1);
    }

    public static void servantAttack(LivingEntity target, Servant servant, int invincibleTime, DamageSource damageSource, float damage, Type type) {
        target.getData(AttachmentRegister.InvincibleData).attack(target, servant, invincibleTime, damageSource, damage, type);
    }

    public static boolean canAttack(LivingEntity target, Servant servant, Type type) {
        return target.getData(AttachmentRegister.InvincibleData).canDamage(servant, type);
    }

    public void attack(LivingEntity target, Servant servant, int invincibleTime, DamageSource damageSource, float damage, Type type) {
        if (canDamage(servant, type)) {
            int invulnerableTime = target.invulnerableTime;
            target.invulnerableTime = 0;
            target.hurt(damageSource, damage);
            target.invulnerableTime = invulnerableTime;
            partialInvincibleFrames.put(servant, new AtomicInteger(invincibleTime));
        }
    }

    public boolean canDamage(Servant servant, Type type) {
        return switch (type) {
            case PARTIAL -> partialInvincibleFrames.getOrDefault(servant, new AtomicInteger(0)).get() <= 0;
            case Global -> globalInvincibleFrames.get() <= 0;
            case null -> false;
        };
    }

    public enum Type {
        PARTIAL, Global
    }

}
