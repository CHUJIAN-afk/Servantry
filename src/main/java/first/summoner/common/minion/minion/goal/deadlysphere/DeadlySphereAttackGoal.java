package first.summoner.common.minion.minion.goal.deadlysphere;

import first.lyra.common.attachmentEntity.AttachmentEntityGoal;
import first.summoner.common.minion.minion.DeadlySphere;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * 致命球直线冲撞攻击Goal：每三次冲撞后切换形态并进入长冷却。
 */
public class DeadlySphereAttackGoal extends AttachmentEntityGoal<DeadlySphere> {

    private int cooldown;
    private int attacks;

    public DeadlySphereAttackGoal(DeadlySphere minion) {
        super(minion);
    }

    @Override
    public boolean canUse() {
        return minion.getTarget() != null;
    }

    @Override
    public void tick() {
        LivingEntity target = minion.getTarget();
        Vec3 targetPos = target.getBoundingBox().getCenter();
        if (--cooldown < 0) {
            Vec3 direction = targetPos.subtract(minion.getPos());
            if (direction.lengthSqr() > 1.0E-6) {
                minion.addVelocity(direction.normalize().scale(2.0));
            }
            attacks++;
            if (attacks % 3 == 0) {
                minion.setForm(minion.getForm() + 1);
                cooldown = 25;
            } else {
                cooldown = 10 + minion.getRandom().nextIntBetweenInclusive(-2, 2);
            }
        } else {
            minion.setVelocity(minion.getVelocity().scale(0.8));
        }
    }
}
