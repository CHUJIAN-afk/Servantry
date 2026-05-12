package first.servantry.common.servant.goal.twins;

import first.servantry.api.servant.ai.ServantGoal;
import first.servantry.common.servant.Twins;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class TwinsIdleGoal extends ServantGoal<Twins> {

    private Vec3 wanderOffset = Vec3.ZERO;

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
        if (wanderOffset.equals(Vec3.ZERO) || owner.getRandom().nextDouble() < 0.025 || wanderOffset.distanceToSqr(servant.getPos()) < 1 || wanderOffset.distanceToSqr(owner.position()) > 8 * 8) {
            wanderOffset = owner.getBoundingBox().getCenter().offsetRandom(owner.getRandom(), (float) owner.getBoundingBox().getSize() * 6);
        }
        if (servant.getPos().distanceToSqr(owner.position()) > 32 * 32) {
            servant.teleportTo(wanderOffset);
        }
        Vec3 dir = wanderOffset.subtract(servant.getPos());
        double dist = dir.length();
        if (dist > 0.05) {
            dir = dir.normalize();
            double force = Math.min(dist * 0.02, 0.2);
            servant.applyForce(dir.scale(force));
        }
        servant.lookAtDirection(servant.getVelocity().normalize());
    }

}
