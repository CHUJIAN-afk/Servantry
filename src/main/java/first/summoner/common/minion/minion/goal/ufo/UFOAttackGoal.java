package first.summoner.common.minion.minion.goal.ufo;


import first.lyra.common.attachmentEntity.AttachmentEntityGoal;
import first.summoner.common.minion.minion.UFO;
import first.summoner.utils.Utils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * UFO攻击Goal - 悬浮在目标上方，发射蓝色激光瞬间命中。
 * 目标在6格范围以外时瞬移到目标上方。
 */
public class UFOAttackGoal extends AttachmentEntityGoal<UFO> {

    private int cooldown = 0;
    private Vec3 wanderPos = Vec3.ZERO;

    public UFOAttackGoal(UFO minion) {
        super(minion);
    }

    @Override
    public boolean canUse() {
        return minion.getTarget() != null;
    }

    @Override
    public void tick() {
        LivingEntity target = minion.getTarget();
        assert target != null;
        Vec3 minionPos = minion.getPos();
        Vec3 targetPos = target.getBoundingBox().getCenter();

        wanderPos = Utils.getWanderPos(minion, wanderPos, targetPos, 4, 3);
        double distanceToTarget = minionPos.distanceTo(wanderPos);

        if (minion.getTrailTimer() == 0) {
            if (distanceToTarget > 16) {
                minion.teleportTo(wanderPos);
            }
        }

        double distance = minionPos.distanceTo(wanderPos);
        minion.addVelocity(wanderPos.subtract(minionPos).normalize().scale(Math.min(distance * 0.02, 0.2)));

        // 激光攻击
        if (--cooldown <= 0 && distanceToTarget < 16) {
            cooldown = minion.getLaserCooldown();
            minion.shootLaserAt(target);
        }
    }
}
