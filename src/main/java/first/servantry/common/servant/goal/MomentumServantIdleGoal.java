package first.servantry.common.servant.goal;

import first.lyra.common.servant.MomentumServant;
import first.lyra.common.servant.ServantGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class MomentumServantIdleGoal extends ServantGoal<MomentumServant> {

    private Vec3 wanderPos = Vec3.ZERO;
    private final float distance;
    private final float force;
    private final float maxDistance;
    private final boolean lookAtDirection;

    public MomentumServantIdleGoal(MomentumServant servant, float distance, float force, float maxDistance, boolean lookAtDirection) {
        super(servant);
        this.distance = distance;
        this.force = force;
        this.maxDistance = maxDistance;
        this.lookAtDirection = lookAtDirection;
    }

    @Override
    public boolean canUse() {
        return !servant.isTarget(servant.getTarget());
    }

    @Override
    public void tick() {
        Player owner = servant.getOwner();
        Vec3 ownerPos = owner.position();
        wanderPos = servant.getWanderPos(wanderPos, ownerPos, distance, 0);
        double distance = servant.getPos().distanceTo(wanderPos);
        servant.applyForce(wanderPos.subtract(servant.getPos()).normalize().scale(Math.min(distance * force, force * 10)));
        if (lookAtDirection) {
            servant.lookAtDirection(servant.getVelocity()
                                            .normalize());
        }
        if (servant.getPos().distanceToSqr(ownerPos) > maxDistance * maxDistance) {
            servant.teleportTo(ownerPos.add(servant.getVelocity().scale(10)));
        }
    }
}
