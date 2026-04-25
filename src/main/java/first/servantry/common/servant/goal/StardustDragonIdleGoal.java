package first.servantry.common.servant.goal;

import first.servantry.api.servant.ai.ServantGoal;
import first.servantry.common.servant.StardustDragon;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * 星尘龙待机目标。
 */
public class StardustDragonIdleGoal extends ServantGoal<StardustDragon> {

    private Vec3 wanderTarget = Vec3.ZERO;

    public StardustDragonIdleGoal(StardustDragon servant) {
        super(servant);
    }

    @Override
    public boolean canUse() {
        return servant.isHead() && servant.getTarget() == null;
    }

    @Override
    public boolean canContinueToUse() {
        return servant.isHead() && servant.getTarget() == null;
    }

    @Override
    public void tick() {
        Player owner = servant.getOwner();
        if (owner == null) return;

        // 选择新目标
        if (wanderTarget.equals(Vec3.ZERO) || servant.getPos().distanceToSqr(wanderTarget) < 1 || owner.getRandom().nextDouble() < 0.01) {

            double theta = owner.getRandom().nextDouble() * Math.PI * 2;
            double phi = Math.acos(2 * owner.getRandom().nextDouble() - 1);
            double radius = 3 + owner.getRandom().nextDouble() * 3;

            wanderTarget = owner.position().add(Math.sin(phi) * Math.cos(theta) * radius, 2 + Math.cos(phi) * radius * 0.5, Math.sin(phi) * Math.sin(theta) * radius);
        }

        // 移动向目标
        Vec3 dir = wanderTarget.subtract(servant.getPos());
        double dist = dir.length();
        if (dist > 0.1) {
            servant.applyForce(dir.normalize().scale(0.1));
        }

        // 朝向运动方向
        Vec3 velocity = servant.getVelocity();
        if (velocity.lengthSqr() > 0.001) {
            Vec3 motionDir = velocity.normalize();
            float targetYaw = (float) Math.toDegrees(Math.atan2(-motionDir.x, motionDir.z));
            float targetPitch = (float) Math.toDegrees(Math.asin(-motionDir.y));
            float rollWave = Mth.sin(owner.tickCount * 0.1f) * 30;
            servant.setDesiredRotation(targetYaw, targetPitch, rollWave);
        }
    }
}