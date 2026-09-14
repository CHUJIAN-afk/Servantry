package first.summoner.common.minion.minion.goal;


import first.lyra.common.attachmentEntity.AttachmentEntityGoal;
import first.lyra.common.minion.MomentumMinion;
import first.summoner.utils.Utils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class MomentumMinionIdleGoal extends AttachmentEntityGoal<MomentumMinion> {

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
        return minion.getTarget() == null;
    }

    @Override
    public void tick() {
        Player owner = minion.getOwner() instanceof Player player ? player : null;
        Vec3 ownerPos = minion.getOwner().position();
        wanderPos = Utils.getWanderPos(minion, wanderPos, ownerPos, distance, 0);
        double distance = minion.getPos().distanceTo(wanderPos);
        minion.addVelocity(wanderPos.subtract(minion.getPos()).normalize().scale(Math.min(distance * force, force * 10)));
        if (lookAtDirection) {
            minion.lookAtDirection(minion.getVelocity().normalize());
        }
        if (minion.getPos().distanceToSqr(ownerPos) > maxDistance * maxDistance) {
            Utils.teleportTo(minion, ownerPos.add(minion.getVelocity().scale(10)));
        }
    }
}
