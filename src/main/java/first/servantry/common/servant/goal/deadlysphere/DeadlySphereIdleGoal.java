package first.servantry.common.servant.goal.deadlysphere;

import first.servantry.api.servant.ai.ServantGoal;
import first.servantry.common.servant.DeadlySphere;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class DeadlySphereIdleGoal extends ServantGoal<DeadlySphere> {

    private Vec3 wanderPos = Vec3.ZERO;

    public DeadlySphereIdleGoal(DeadlySphere deadlySphere) {
        super(deadlySphere);
    }

    @Override
    public boolean canUse() {
        return servant.getTarget() == null;
    }

    @Override
    public void tick() {
        Player owner = servant.getOwner();
        wanderPos = servant.getWanderPos(wanderPos, owner.position(), 8, 0);
        double distance = servant.getPos().distanceTo(wanderPos);
        servant.applyForce(wanderPos.subtract(servant.getPos()).normalize().scale(Math.min(distance * 0.02, 0.2)));
        if (servant.getPos().distanceToSqr(owner.position()) > 32 * 32) {
            servant.teleportTo(wanderPos);
        }
    }
}
