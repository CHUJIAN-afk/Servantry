package first.servantry.common.servant.goal.stardustDragon;

import first.servantry.api.servant.ai.ServantGoal;
import first.servantry.common.servant.StardustDragon;
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
        return servant.isHead() && !servant.isTarget(servant.getTarget());
    }

    @Override
    public void tick() {
        Player owner = servant.getOwner();
        if (owner.distanceToSqr(servant.getPos()) > 128 * 128) {
            servant.teleportTo(owner.getBoundingBox().getCenter());
            wanderTarget = Vec3.ZERO;
        }
        // 选择新目标
        if (wanderTarget.equals(Vec3.ZERO) || servant.getPos().distanceToSqr(wanderTarget) < 4 * servant.getScale() || owner.getRandom().nextDouble() < 0.01 || wanderTarget.distanceToSqr(owner.position()) > 8 * 8 * servant.getScale()) {
            wanderTarget = owner.position().offsetRandom(owner.getRandom(), 8 * servant.getScale());
        }

        // 螺旋游动向目标
        servant.spiralToward(wanderTarget, (wanderTarget.distanceToSqr(servant.getPos()) + 20) * 0.0005);
    }

}