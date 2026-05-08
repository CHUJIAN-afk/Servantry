package first.servantry.common.servant.goal;

import first.servantry.api.servant.ai.ServantGoal;
import first.servantry.common.servant.StardustDragon;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * 星尘龙跟随目标（非头部体节）。
 */
public class StardustDragonFollowGoal extends ServantGoal<StardustDragon> {

    public StardustDragonFollowGoal(StardustDragon servant) {
        super(servant);
    }

    @Override
    public boolean canUse() {
        return !servant.isHead();
    }

    @Override
    public boolean canContinueToUse() {
        return !servant.isHead();
    }

    @Override
    public void tick() {
        StardustDragon preceding = servant.getPrecedingSegment();
        if (preceding == null) return;

        Vec3 precedingPos = preceding.getPos();
        Vec3 currentPos = servant.getPos();
        Vec3 toPreceding = precedingPos.subtract(currentPos);
        double distance = toPreceding.length();

        // 距离太小时不处理，保持当前位置
        if (distance < 0.01) return;

        Vec3 direction = toPreceding.normalize();
        double targetDistance = servant.getSegmentDistance();

        // 只有距离超过目标距离时才移动
        if (distance > targetDistance) {
            // 移到前一体节后方固定距离
            Vec3 targetPos = precedingPos.subtract(direction.scale(targetDistance));
            servant.teleportTo(targetPos);
        }

        // 朝向前一体节
        float targetYaw = (float) Math.toDegrees(Math.atan2(-direction.x, direction.z));
        float targetPitch = (float) Math.toDegrees(Math.asin(-direction.y));

        float newYaw = Mth.rotLerp(Math.min(0.5f * servant.getScale(), 0.9f), servant.getYaw(), targetYaw);
        float newPitch = Mth.rotLerp(Math.min(0.5f * servant.getScale(), 0.9f), servant.getPitch(), targetPitch);
        float newRoll = Mth.rotLerp(Math.min(0.25f * servant.getScale(), 0.45f), servant.getRoll(), preceding.getRoll());

        servant.setDesiredRotation(newYaw, newPitch, newRoll);
    }
}