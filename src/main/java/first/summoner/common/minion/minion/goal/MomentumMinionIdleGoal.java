package first.summoner.common.minion.minion.goal;

import first.lyra.common.minion.MinionGoal;
import first.lyra.common.minion.MomentumMinion;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class MomentumMinionIdleGoal extends MinionGoal<MomentumMinion> {

    private Vec3 wanderPos = Vec3.ZERO;
    private final float distance;
    private final float force;
    private final float maxDistance;
    private final boolean lookAtDirection;

    public MomentumMinionIdleGoal(MomentumMinion minion, float distance, float force, float maxDistance, boolean lookAtDirection) {
        super(minion);
        this.distance = distance;
        this.force = force;
        this.maxDistance = maxDistance;
        this.lookAtDirection = lookAtDirection;
    }

    @Override
    public boolean canUse() {
        return !minion.isTarget(minion.getTarget());
    }

    @Override
    public void tick() {
        Player owner = minion.getOwner();
        Vec3 ownerPos = owner != null ? owner.position() : minion.getPos();
        wanderPos = minion.getWanderPos(wanderPos, ownerPos, distance, 0);
        double distance = minion.getPos().distanceTo(wanderPos);
        minion.applyForce(wanderPos.subtract(minion.getPos()).normalize().scale(Math.min(distance * force, force * 10)));
        if (lookAtDirection) {
            minion.lookAtDirection(minion.getVelocity()
                                            .normalize());
        }
        if (owner != null && minion.getPos().distanceToSqr(ownerPos) > maxDistance * maxDistance) {
            minion.teleportTo(ownerPos.add(minion.getVelocity().scale(10)));
        }
    }
}
