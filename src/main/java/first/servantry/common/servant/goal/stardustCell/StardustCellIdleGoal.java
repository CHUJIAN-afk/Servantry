package first.servantry.common.servant.goal.stardustCell;

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

    private Vec3 wanderTarget = Vec3.ZERO;

    public StardustCellIdleGoal(StardustCell servant) {
        super(servant);
    }

    @Override
    public boolean canUse() {
        return !servant.isTarget(servant.getTarget());
    }

    @Override
    public void tick() {
        Player owner = servant.getOwner();
        Vec3 ownerPos = owner.getBoundingBox().getCenter();
        if (wanderTarget.equals(Vec3.ZERO) || owner.getRandom().nextDouble() < 0.025 || wanderTarget.distanceToSqr(servant.getPos()) < 1 || wanderTarget.distanceToSqr(owner.position()) > 8 * 8) {
            wanderTarget = ownerPos.offsetRandom(owner.getRandom(), (float) owner.getBoundingBox().getSize() * 4);
        }
        Vec3 dir = wanderTarget.subtract(servant.getPos());
        if (!dir.equals(Vec3.ZERO)) {
            double dist = dir.length();
            double force = Math.min(dist * 0.01, 0.1);
            servant.applyForce(dir.normalize().scale(force));
        }
        if (servant.getPos().distanceToSqr(ownerPos) > 48 * 48) {
            servant.teleportTo(ownerPos);
        }
    }

}