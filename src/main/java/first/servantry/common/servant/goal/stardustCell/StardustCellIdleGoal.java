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

    private Vec3 wanderPos = Vec3.ZERO;

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
        wanderPos = servant.getWanderPos(wanderPos, ownerPos, 4, 1);
        float distance = (float) servant.getPos().distanceTo(wanderPos);
        servant.applyForce(wanderPos, Math.min(distance * 0.01f, 0.08f));
        if (servant.getPos().distanceToSqr(ownerPos) > 48 * 48) {
            servant.teleportTo(ownerPos.add(servant.getVelocity().scale(10)));
        }
    }
}