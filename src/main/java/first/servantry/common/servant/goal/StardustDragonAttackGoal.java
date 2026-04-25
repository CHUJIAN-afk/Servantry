package first.servantry.common.servant.goal;

import first.servantry.api.servant.ai.ServantGoal;
import first.servantry.common.servant.StardustDragon;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * 星尘龙攻击目标。
 */
public class StardustDragonAttackGoal extends ServantGoal<StardustDragon> {

    public StardustDragonAttackGoal(StardustDragon servant) {
        super(servant);
    }

    @Override
    public boolean canUse() {
        return servant.isHead() && servant.getTarget() != null;
    }

    @Override
    public boolean canContinueToUse() {
        return servant.isHead() && servant.getTarget() != null && servant.getTarget().isAlive();
    }

    @Override
    public void tick() {
        LivingEntity target = servant.getTarget();
        if (target == null || !target.isAlive()) return;

        Vec3 targetPos = target.position().add(0, target.getBbHeight() * 0.5, 0);
        Vec3 currentPos = servant.getPos();
        Vec3 velocity = servant.getVelocity();

        Vec3 toTarget = targetPos.subtract(currentPos);
        if (toTarget.length() < 0.1) return;

        // 向目标施加推力
        Vec3 thrustDir = toTarget.normalize();
        servant.applyForce(thrustDir.scale(0.25));

        // 朝向运动方向
        double speed = velocity.length();
        if (speed > 0.01) {
            Vec3 motionDir = velocity.normalize();
            float targetYaw = (float) Math.toDegrees(Math.atan2(-motionDir.x, motionDir.z));
            float targetPitch = (float) Math.toDegrees(Math.asin(-motionDir.y));
            float rollWave = Mth.sin(servant.getOwner().tickCount * 0.1f) * 30;
            servant.setDesiredRotation(targetYaw, targetPitch, rollWave);
        }
    }

}