package first.summoner.common.minion.minion.goal.deadlysphere;


import first.lyra.common.attachmentEntity.AttachmentEntityGoal;
import first.summoner.common.minion.minion.DeadlySphere;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * 致命球直线冲撞攻击Goal。
 */
public class DeadlySphereAttackGoal extends AttachmentEntityGoal<DeadlySphere> {

    private int cooldown = 0;
    private int attacks = 0;

    public DeadlySphereAttackGoal(DeadlySphere deadlySphere) {
        super(deadlySphere);
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
            Vec3 direction = targetPos.subtract(minion.getPos()).normalize();
            minion.addVelocity(direction.scale(2));
            attacks++;
            cooldown = attacks % 3 == 0 ? 20 : 10 + minion.getRandom().nextIntBetweenInclusive(-2, 2);
        } else {
            minion.setVelocity(minion.getVelocity().scale(0.8));
        }
    }
}
