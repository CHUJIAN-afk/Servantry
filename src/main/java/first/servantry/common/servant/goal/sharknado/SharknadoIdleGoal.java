package first.servantry.common.servant.goal.sharknado;

import first.servantry.api.servant.ai.ServantGoal;
import first.servantry.common.servant.Sharknado;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * 鲨鱼龙卷空闲Goal - 向玩家另一侧靠近。
 */
public class SharknadoIdleGoal extends ServantGoal<Sharknado> {

    private Vec3 wanderPos = Vec3.ZERO;

    public SharknadoIdleGoal(Sharknado servant) {
        super(servant);
    }

    @Override
    public boolean canUse() {
        return !servant.isTarget(servant.getTarget());
    }

    @Override
    public void tick() {
        Player owner = servant.getOwner();
        Vec3 targetPos = owner.getBoundingBox().getCenter();
        wanderPos = servant.getWanderPos(wanderPos, targetPos, 5, 0);
        double distance = servant.getPos().distanceTo(wanderPos);
        servant.applyForce(wanderPos.subtract(servant.getPos()).normalize().scale(Math.min(distance * 0.01, 0.1)));
        if (servant.getPos().distanceToSqr(wanderPos) > 48 * 48) {
            servant.teleportTo(wanderPos);
        }
    }
}