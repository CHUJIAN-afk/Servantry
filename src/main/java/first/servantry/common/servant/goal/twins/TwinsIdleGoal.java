package first.servantry.common.servant.goal.twins;

import first.servantry.api.servant.ai.ServantGoal;
import first.servantry.common.servant.Twins;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class TwinsIdleGoal extends ServantGoal<Twins> {

    private Vec3 wanderPos = Vec3.ZERO;

    public TwinsIdleGoal(Twins twins) {
        super(twins);
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
        servant.lookAtDirection(servant.getVelocity().normalize());
        if (servant.getPos().distanceToSqr(owner.position()) > 64 * 64) {
            servant.teleportTo(wanderPos);
        }
    }
}
