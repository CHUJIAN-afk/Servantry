package first.summoner.common.minion.minion.goal.deadlysphere;


import first.lyra.common.attachmentEntity.AttachmentEntityGoal;
import first.summoner.common.minion.minion.DeadlySphere;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * 致命球冲刺攻击Goal，AI与魔焰眼完全一致。
 */
public class DeadlySphereAttackGoal extends AttachmentEntityGoal<DeadlySphere> {

    private int cooldown;
    private int combat = 0;

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
        Vec3 toTarget = targetPos.subtract(minion.getPos());
        Vec3 direction = toTarget.offsetRandom(minion.getRandom(), (float) target.getBoundingBox().getSize()).normalize();
        if (--cooldown <= 0) {
            if (++combat == 3) {
                combat = 0;
                cooldown = 20;
                minion.nextAppearance();
            } else {
                cooldown = 8 + minion.getRandom().nextInt(-2, 2);
            }
            minion.addVelocity(direction.scale(3));
            minion.setTrailTimer(10);
        }
    }
}
