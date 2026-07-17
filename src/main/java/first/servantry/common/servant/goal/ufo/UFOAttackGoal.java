package first.servantry.common.servant.goal.ufo;

import first.servantry.api.servant.ai.ServantGoal;
import first.servantry.common.servant.UFO;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * UFO攻击Goal - 悬浮在目标上方，发射蓝色激光瞬间命中。
 * 目标在6格范围以外时瞬移到目标上方。
 */
public class UFOAttackGoal extends ServantGoal<UFO> {

    private int cooldown = 0;
    private Vec3 wanderPos = Vec3.ZERO;

    public UFOAttackGoal(UFO servant) {
        super(servant);
    }

    @Override
    public boolean canUse() {
        return servant.isTarget(servant.getTarget());
    }

    @Override
    public void tick() {
        LivingEntity target = servant.getTarget();

        Vec3 servantPos = servant.getPos();
        Vec3 targetPos = target.getBoundingBox().getCenter();

        wanderPos = servant.getWanderPos(wanderPos, targetPos, 4, 3);
        double distanceToTarget = servantPos.distanceTo(wanderPos);

        if (servant.getTrailTimer() == 0) {
            if (distanceToTarget > 16) {
                servant.teleportTo(wanderPos);
            }
        }

        double distance = servantPos.distanceTo(wanderPos);
        servant.applyForce(wanderPos.subtract(servantPos)
                                   .normalize()
                                   .scale(Math.min(distance * 0.02, 0.2)));

        // 激光攻击
        if (--cooldown <= 0 && distanceToTarget < 16) {
            cooldown = servant.getLaserCooldown();
            servant.shootLaserAt(target);
        }
    }
}
