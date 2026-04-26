package first.servantry.common.servant.goal;

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

            wanderTarget = owner.position().add(
                Math.sin(phi) * Math.cos(theta) * radius,
                2 + Math.cos(phi) * radius * 0.5,
                Math.sin(phi) * Math.sin(theta) * radius
            );
        }

        // 螺旋游动向目标
        servant.spiralToward(wanderTarget, 0.1);
    }
}