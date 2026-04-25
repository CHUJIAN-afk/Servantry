package first.servantry.common.servant.goal;

import first.servantry.api.servant.ai.ServantGoal;
import first.servantry.common.servant.StardustCell;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * 星尘细胞空闲游荡Goal。
 * <p>
 * 当没有攻击目标时，仆从在玩家周围随机游荡。
 * </p>
 */
public class StardustCellIdleGoal extends ServantGoal<StardustCell> {

    private Vec3 wanderOffset = Vec3.ZERO;

    public StardustCellIdleGoal(StardustCell servant) {
        super(servant);
    }

    @Override
    public boolean canUse() {
        return servant.getTarget() == null && servant.getTeleportTimer() <= 0;
    }

    @Override
    public void tick() {
        Player owner = servant.getOwner();
        if (wanderOffset.equals(Vec3.ZERO) || owner.getRandom().nextDouble() < 0.025 || wanderOffset.distanceToSqr(servant.getPos()) < 1) {
            wanderOffset = new Vec3(
                    (owner.getRandom().nextDouble() - 0.5) * 8,
                    owner.getRandom().nextDouble() * 3 + 2,
                    (owner.getRandom().nextDouble() - 0.5) * 8
            );
        }

        Vec3 targetPos = owner.position().add(wanderOffset);
        Vec3 dir = targetPos.subtract(servant.getPos());
        double dist = dir.length();
        if (dist > 0.05) {
            dir = dir.normalize();
            double force = Math.min(dist * 0.01, 0.1);
            servant.applyForce(dir.scale(force));
        }

        // 缓慢朝向运动方向
        if (servant.getVelocity().lengthSqr() > 0.01) {
            Vec3 vel = servant.getVelocity().normalize();
            servant.setDesiredRotation((float) Math.toDegrees(Math.atan2(-vel.x, vel.z)), servant.getDesiredPitch(), servant.getDesiredRoll());
        }
    }
}