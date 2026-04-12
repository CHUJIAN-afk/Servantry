package first.servantry.api.event;

import first.servantry.api.ServantDamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;

/**
 * 发生在仆从标记判定之后
 */
public class ServantAttackEvent extends Event {

    private final ServantDamageSource damageSource;
    private final LivingEntity target;
    private float amount;

    public ServantAttackEvent(ServantDamageSource damageSource, LivingEntity target, float amount) {
        this.damageSource = damageSource;
        this.target = target;
        this.amount = amount;
    }

    public ServantDamageSource getDamageSource() {
        return damageSource;
    }

    public LivingEntity getTarget() {
        return target;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public float getAmount() {
        return amount;
    }

}
