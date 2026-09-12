package first.summoner.common.minion.goal.stardustDragon;

import first.lyra.common.minion.MinionGoal;
import first.summoner.common.minion.StardustDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * 星尘龙待机目标。
 */
public class StardustDragonIdleGoal extends MinionGoal<StardustDragon> {

    private Vec3 wanderPos = Vec3.ZERO;

    public StardustDragonIdleGoal(StardustDragon minion) {
        super(minion);
    }

    @Override
    public boolean canUse() {
        return minion.isHead() && !minion.isTarget(minion.getTarget());
    }

    @Override
    public void tick() {
        Player owner = minion.getOwner();
        Vec3 anchor = owner != null ? owner.position() : minion.getPos();
        if (owner != null) {
            do {
                wanderPos = minion.getWanderPos(wanderPos, anchor, 8, 0);
            } while (wanderPos.distanceToSqr(minion.getPos()) < 2);
        } else {
            wanderPos = minion.getWanderPos(wanderPos, anchor, 8, 0);
        }
        minion.orbitToward(wanderPos, 120f, 0.05 + minion.getPos().distanceTo(wanderPos) * 0.005);
        if (owner != null && owner.distanceToSqr(minion.getPos()) > 128 * 128) {
            minion.teleportTo(owner.getBoundingBox().getCenter());
            wanderPos = Vec3.ZERO;
        }
    }
}
