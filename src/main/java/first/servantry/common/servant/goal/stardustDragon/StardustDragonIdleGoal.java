package first.servantry.common.servant.goal.stardustDragon;

import first.servantry.api.servant.ai.ServantGoal;
import first.servantry.common.servant.StardustDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * 星尘龙待机目标。
 */
public class StardustDragonIdleGoal extends ServantGoal<StardustDragon> {

    private Vec3 wanderPos = Vec3.ZERO;

    public StardustDragonIdleGoal(StardustDragon servant) {
        super(servant);
    }

    @Override
    public boolean canUse() {
        return servant.isHead() && !servant.isTarget(servant.getTarget());
    }

    @Override
    public void tick() {
        Player owner = servant.getOwner();
        Vec3 lastWanderPos = wanderPos;
        do {
            wanderPos = servant.getWanderPos(wanderPos, owner.position(), 8, 0);
        } while (lastWanderPos.distanceTo(wanderPos) < 4);
        servant.orbitToward(wanderPos, 120f, 0.01 + Math.min(servant.getPos().distanceTo(wanderPos) * 0.01, 0.05));
        if (owner.distanceToSqr(servant.getPos()) > 128 * 128) {
            servant.teleportTo(owner.getBoundingBox().getCenter());
            wanderPos = Vec3.ZERO;
        }
    }
}